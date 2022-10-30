package com.mehdi.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.models.RequestModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.create_group_activity.*
import kotlinx.android.synthetic.main.create_post_activity.*
import kotlinx.android.synthetic.main.create_post_activity.create_post_back_arrow
import kotlinx.android.synthetic.main.create_project_activity.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateProjectActivity : AppCompatActivity() {

    private var visibility: Boolean = true
    private var idGroup: Long = -1
    private lateinit var userConnectedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_project_activity)


        loadProperties()


        create_post_back_arrow.setOnClickListener {
            finish()
        }


        create_project_create_action.setOnClickListener{
            createProject()
        }
    }

    private fun loadProperties(){
        val intent = intent
        val extras = intent.extras
        idGroup =(extras ?: return).getLong("idGroup")
        userConnectedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
    }

    private fun createProject(){
        val request : RequestModel.CreateProjectRequest = RequestModel.CreateProjectRequest(
            create_project_projectname_value.text.toString(),
            visibility,
            idGroup,
        )
        NetworkManager.createProject(request, userConnectedPreferences.getString("accessToken", null)!!)
            .enqueue(object: Callback<ResponseModel.ProjectResponse> {
                override fun onFailure(call: Call<ResponseModel.ProjectResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.ProjectResponse>,
                    response: Response<ResponseModel.ProjectResponse>
                ) {
                    if(response.code() != 201){
                        Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, "The project has been created", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
            )
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_yes ->
                    if (checked) {
                        visibility = true
                    }
                R.id.radio_no ->
                    if (checked) {
                        visibility = false
                    }
            }
        }
    }

}