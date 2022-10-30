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
import kotlinx.android.synthetic.main.subscription_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubscriptionActivity: AppCompatActivity() {
    private var idUser: Long? = null
    private lateinit var user: DataModel.UserProfileInfo
    private lateinit var userConnectedPreferences: SharedPreferences
    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.subscription_activity)


        subscription_activity_back_arrow_imageButton.setOnClickListener{
            finish()
        }

        loadProperties()
        loadSubscriptions()

    }

    private fun loadSubscriptions(){
        val response = NetworkManager.getUserSubscriptionsByIdUser(
            userConnectedPreferences.getLong("idUser", -1)!!,
            userConnectedPreferences.getString("accessToken", null)!! )
            .enqueue(object: Callback<MutableList<ResponseModel.FollowerResponse>> {
                override fun onFailure(call: Call<MutableList<ResponseModel.FollowerResponse>>, t: Throwable) {
                    Toast.makeText(applicationContext, "Something went wrong ", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<MutableList<ResponseModel.FollowerResponse>>,
                    response: Response<MutableList<ResponseModel.FollowerResponse>>
                ) {
                    if(response.code() == 200){
                        GlobalScope.launch {
                            val subscription = convertResponseToSubscriberModel(response.body()!!)
                            withContext(Dispatchers.Main) {
                                recyclerAdapter.setData(subscription)
                                subscription_activity_recycler_view.adapter = recyclerAdapter
                            }
                        }
                    }
                }
            }
            )
    }

    private fun convertResponseToSubscriberModel(response: MutableList<ResponseModel.FollowerResponse>):
            MutableList<DataModel.Subscription>{

        val subscriptions: MutableList<DataModel.Subscription> = ArrayList()
        for (subscriptionResponse in response){

            val getUserInfo : Call<ResponseModel.UserInfoResponse> =
                NetworkManager.getUserInfoByIdUser(
                    subscriptionResponse.id,
                    userConnectedPreferences!!.getString("accessToken", null)!!
                )
            var userInfoResponse = getUserInfo.execute().body()
            val subscription = DataModel.Subscription(
                userInfoResponse!!.id,
                userInfoResponse!!.firstName + " " + userInfoResponse.lastName,
                "",
                userInfoResponse.city
            )

            subscriptions.add(subscription)
        }
        return subscriptions
    }

    private fun loadProperties(){
        userConnectedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
    }
}