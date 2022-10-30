package com.mehdi.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.adapter.RecyclerAdapter
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.RequestModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.create_group_activity.*
import kotlinx.android.synthetic.main.create_post_activity.*
import kotlinx.android.synthetic.main.create_project_activity.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateGroupActivity : AppCompatActivity() {

    private var members: MutableList<DataModel.EmailInAddMember> = ArrayList()
    private lateinit var userConnectedPreferences : SharedPreferences
    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group_activity)

        loadProperties()

        recyclerAdapter.setData(members)
        create_group_member_recyclerview.adapter = recyclerAdapter

        create_group_back_arrow.setOnClickListener {
            finish()
        }

        create_group_add_member_button.setOnClickListener{
            addMember(create_group_add_member_value.text.toString())
        }

        create_group_create_action.setOnClickListener {
            createGroup()
        }
    }

    private fun loadProperties(){
        userConnectedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
    }

    private fun addMember(email: String){
        copyRecyclerDataInMembers()
        if (!alreadyAdded(email)){
            getUserByEmail(email)
        }

    }

    private fun copyRecyclerDataInMembers(){
        members = ArrayList()
        val data = recyclerAdapter.getData()
        for( i in data ){
            members.add(i as DataModel.EmailInAddMember)
        }
    }

    private fun alreadyAdded(email: String): Boolean {
        for ( i in members ){
            if ( i.email == email){
                return true
            }
        }
        return false
    }

    private fun createGroup(){
        val preference : SharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
        val request : RequestModel.CreateGroupRequest = RequestModel.CreateGroupRequest(
            create_group_groupname_value.text.toString(),
            preference.getLong("idUser", -1)!!,
            getListOfMembers(),
        )
        NetworkManager.createGroup(request, preference.getString("accessToken", null)!!)
            .enqueue(object: Callback<ResponseModel.GroupResponse> {
                override fun onFailure(call: Call<ResponseModel.GroupResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.GroupResponse>,
                    response: Response<ResponseModel.GroupResponse>
                ) {
                    if(response.code() != 201){
                        Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, "The group has been created", Toast.LENGTH_LONG).show()
                        val response = response.body()
                        val resultIntent = Intent()
                        resultIntent.putExtra("groupCreated", Gson().toJson(response))
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }
            )
    }

    private fun getListOfMembers(): List<String>{
        val membersList: MutableList<String> = ArrayList()
        for( i in members){
            membersList.add(i.id.toString())
        }
        membersList.add(userConnectedPreferences.getLong("idUser", -1)!!.toString())
        return membersList
    }

    private fun getUserByEmail(email: String){

        NetworkManager.getUserInfoByEmail(email, userConnectedPreferences.getString("accessToken", null)!!)
            .enqueue(object: Callback<ResponseModel.UserInfoResponse> {
                override fun onFailure(call: Call<ResponseModel.UserInfoResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "The user doesn't exist", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.UserInfoResponse>,
                    response: Response<ResponseModel.UserInfoResponse>
                ) {
                    if(response.code() != 200){
                        Toast.makeText(applicationContext, "The user doesn't exist", Toast.LENGTH_LONG).show()
                    } else {
                        members.add(
                            convertUserInfoResponseToEmailInAddMember(response.body()!!))
                        recyclerAdapter.setData(members)
                        create_group_add_member_value.text = Editable.Factory.getInstance().newEditable("")
                        Toast.makeText(applicationContext, "user added", Toast.LENGTH_LONG).show()
                    }
                }
            }
            )
    }

    private fun convertUserInfoResponseToEmailInAddMember(userInfoResponse: ResponseModel.UserInfoResponse):
            DataModel.EmailInAddMember {
        return DataModel.EmailInAddMember(
            userInfoResponse.id,
            userInfoResponse.email
        )
    }
}
