package com.mehdi.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.adapter.RecyclerAdapter
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.search_post_result_activity.*
import kotlinx.android.synthetic.main.search_user_result_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call

class SearchUserResultActivity: AppCompatActivity() {
    private var request : String? = null
    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()
    private lateinit var userConnectedPreferences : SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_user_result_activity)

        loadProperties()
        getResult()

        search_user_result_activity_back_arrow.setOnClickListener{
            finish()
        }
    }

    private fun loadProperties(){
        userConnectedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
        val intent = intent
        val extras = intent.extras
        request = (extras ?: return).getString("request")
    }

    private fun getResult(){
        GlobalScope.launch(Dispatchers.Default) {
            val response: MutableList<ResponseModel.PostUserResponse>
            try {
                response = NetworkManager.searchUser(
                    userConnectedPreferences.getString("accessToken", null)!!,
                    request!!)
                var result = convertResponseToModel(response)
                withContext(Dispatchers.Main) {
                    recyclerAdapter.setData(result)
                    recycler_view_search_user.adapter = recyclerAdapter
                }
            } catch (t: Throwable) {
                print(t)
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun convertResponseToModel(data: MutableList<ResponseModel.PostUserResponse>): MutableList<DataModel.UserPost> {
        try {
            val userfoundList: MutableList<DataModel.UserPost> = ArrayList()
            for (i in data) {

                val getUserInfo : Call<ResponseModel.UserInfoResponse> =
                    NetworkManager.getUserInfoByIdUser(
                        i.id!!,
                        userConnectedPreferences!!.getString("accessToken", null)!!
                    )
                var userInfoResponse = getUserInfo.execute().body()
                val img = if (userInfoResponse!!.imgUrl == null) "" else userInfoResponse!!.imgUrl
                val userFound = DataModel.UserPost(
                    i.id,
                    userInfoResponse!!.firstName,
                    userInfoResponse!!.lastName,
                    i.email,
                    i.nbFollowers,
                    i.nbSubscriptions,
                    img
                )

                userfoundList.add(userFound)

            }
            return userfoundList
        } catch (t: Throwable){
            print(t)
        }
        return ArrayList()
    }
}