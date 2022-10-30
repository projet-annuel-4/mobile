package com.mehdi.myapplication.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.R
import com.mehdi.myapplication.adapter.RecyclerAdapter
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.RequestModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.create_group_activity.*
import kotlinx.android.synthetic.main.group_activity.*
import kotlinx.android.synthetic.main.group_members_fragment.*
import kotlinx.android.synthetic.main.group_project_fragment.*
import kotlinx.android.synthetic.main.post_activity.*
import kotlinx.android.synthetic.main.profile_post_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupMembersFragment: Fragment() {
    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()
    private var group: DataModel.Group? = null
    private lateinit var userConnectedPreferences : SharedPreferences
    private var members: MutableList<DataModel.UserInGroup> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.group_members_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        group_member_fragment_form_add_member.visibility = View.GONE

        group_member_fragment_add_member_form_member_close.setOnClickListener {
            group_member_fragment_form_add_member.visibility = View.GONE
        }

        group_member_fragment_add_member.setOnClickListener {
            group_member_fragment_form_add_member.visibility = View.VISIBLE
        }

        group_member_fragment_form_add_member_button.setOnClickListener{
            processAddMember()
        }


        loadProperties()
        loadMembers()
    }

    private fun loadProperties(){
        userConnectedPreferences = this.activity?.getSharedPreferences("Login", Context.MODE_PRIVATE)!!
        group = Gson().fromJson(arguments!!.getString("group"), DataModel.Group::class.java)
    }

    private fun loadMembers(){
        GlobalScope.launch(Dispatchers.Default) {
            val response: MutableList<ResponseModel.UserInfoResponse>
            try {
                response = NetworkManager.getGroupMembersByGroupId(group!!.id,userConnectedPreferences.getString("accessToken", null)!! )
                var members = convertResponseToUserInGroupModel(response)
                withContext(Dispatchers.Main) {
                    recyclerAdapter.setGroupId(group!!.id)
                    recyclerAdapter.setData(members)
                    group_member_fragment_recycler_view.adapter = recyclerAdapter
                }
            } catch (t: Throwable) {
                print(t)
            }
        }
    }

    private fun convertResponseToUserInGroupModel(response: MutableList<ResponseModel.UserInfoResponse>):
            MutableList<DataModel.UserInGroup>{
        var members: MutableList<DataModel.UserInGroup> = ArrayList()
        for (i in response){
            var member: DataModel.UserInGroup = DataModel.UserInGroup(
                i.id,
                i.firstName + ' ' + i.lastName,
                i.imgUrl,
                i.city,
                group!!.groupCreator
            )
            members.add(member)
        }
        return members
    }

    private fun convertResponseToUserInGroupModel(response: ResponseModel.UserInfoResponse):
            DataModel.UserInGroup{

        var member: DataModel.UserInGroup = DataModel.UserInGroup(
            response.id,
            response.firstName + ' ' + response.lastName,
            response.imgUrl,
            response.city,
            group!!.groupCreator
        )

        return member

    }

    private fun getAddMemberRequest(memberToAddId: Long, creatorId: Long): RequestModel.GroupRequest {
        val memberToAdd : List<String> = listOf(memberToAddId.toString())
        return RequestModel.GroupRequest(
            group!!.id,
            "",
            memberToAdd,
            creatorId
        )
    }

    private fun copyRecyclerDataInMember(){
        members = ArrayList()
        val data = recyclerAdapter.getData()
        for( i in data ){
            members.add(i as DataModel.UserInGroup)
        }
    }

    private fun processAddMember(){
        val email = group_member_fragment_form_email_member.text.toString()
        getUserByEmail(email)
    }

    private fun getUserByEmail(email: String){

        NetworkManager.getUserInfoByEmail(email, userConnectedPreferences.getString("accessToken", null)!!)
            .enqueue(object: Callback<ResponseModel.UserInfoResponse?> {
                override fun onFailure(call: Call<ResponseModel.UserInfoResponse?>, t: Throwable) {
                    Toast.makeText(context, "The user doesn't exist", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.UserInfoResponse?>,
                    response: Response<ResponseModel.UserInfoResponse?>
                ) {
                    if(response.code() != 200){
                        Toast.makeText(context, "The user doesn't exist", Toast.LENGTH_LONG).show()
                    } else {
                        addMember(response.body()!!.id, response.body()!!)
                    }
                }
            }
            )
    }

    private fun addMember(memberToAddId: Long, memberToAdd: ResponseModel.UserInfoResponse){
        val request : RequestModel.GroupRequest = getAddMemberRequest(memberToAddId, group!!.groupCreator)
        NetworkManager.addMemberToGroup(group!!.id, request, userConnectedPreferences.getString("accessToken", null)!!)
            .enqueue(object: Callback<ResponseModel.GroupResponse> {
                override fun onFailure(call: Call<ResponseModel.GroupResponse>, t: Throwable) {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.GroupResponse>,
                    response: Response<ResponseModel.GroupResponse>
                ) {
                    if(response.code() != 200){
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "The member has been added", Toast.LENGTH_LONG).show()
                        group_member_fragment_recycler_view.scrollToPosition(members.size - 1)
                        copyRecyclerDataInMember()
                        members.add(convertResponseToUserInGroupModel(memberToAdd))
                        recyclerAdapter.setGroupId(group!!.id)
                        recyclerAdapter.setData(members)
                        group_member_fragment_recycler_view.scrollToPosition(members.size - 1)
                        group_member_fragment_form_email_member.text = Editable.Factory.getInstance().newEditable("")
                        group_member_fragment_add_member_form_member_close.performClick()
                        val inputMethodManager =
                            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                        // on below line hiding our keyboard.
                        inputMethodManager.hideSoftInputFromWindow(view!!.getWindowToken(), 0)
                    }
                }
            }
            )
    }
}