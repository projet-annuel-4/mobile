package com.mehdi.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.renderscript.Sampler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.RequestModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.connection_activity.*
import kotlinx.android.synthetic.main.header_search.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ConnectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connection_activity)


        val preference : SharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
//        if ( preference.getString("email", null) != null){
//            finish()
//        }

        connection_button.setOnClickListener{
            val request: RequestModel.AuthRequest = RequestModel.AuthRequest(
                                                    email_connection.text.toString(),
                                                    password_connection.text.toString()
            )

            NetworkManager.login(request)
                .enqueue(object: Callback<DataModel.UserAuthenticate>{
                    override fun onFailure(call: Call<DataModel.UserAuthenticate>, t: Throwable) {
                        Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(
                        call: Call<DataModel.UserAuthenticate>,
                        response: Response<DataModel.UserAuthenticate>
                    ) {
                        if(response.code() != 200){
                            Toast.makeText(applicationContext, "Email or password incorrect", Toast.LENGTH_LONG).show()
                        } else {
                            val sp = getSharedPreferences("Login", Context.MODE_PRIVATE)
                            val Ed = sp.edit()
                            Ed.putString("email", response.body()?.email)
                            Ed.putString("firstname", response.body()?.firstName)
                            Ed.putString("lastname", response.body()?.lastName)
                            Ed.putString("imageUrl", response.body()?.imageUrl)
                            Ed.putLong("idUser", response.body()?.id!!.toLong())
                            Ed.putString("userRole", response.body()?.userRole)
                            Ed.putString("tokenType", response.body()?.tokenType)
                            Ed.putString("accessToken", response.body()?.accessToken)
                            Ed.commit()
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }

                )
        }





    }
}