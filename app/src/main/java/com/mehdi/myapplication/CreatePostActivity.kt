package com.mehdi.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.models.RequestModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.create_post_activity.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CreatePostActivity : AppCompatActivity() {

    private var tags: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_post_activity)

        create_post_back_arrow.setOnClickListener {
            finish()
        }

        create_post_create_action.setOnClickListener{
            createPost()
        }

        create_post_add_tag_button.setOnClickListener {
            addTag()
        }
    }

    private fun addTag(){
        if( create_post_add_member_value.text.toString().isEmpty()){
            Toast.makeText(applicationContext, "Cannot add empty tag", Toast.LENGTH_LONG).show()
            return
        }
        if(tags.size >= 5){
            Toast.makeText(applicationContext, "Cannot add more than 5 tag", Toast.LENGTH_LONG).show()
            return
        }
        val tag = create_post_add_member_value.text.toString()
        for( i in tags){
            if (i == tag){
                Toast.makeText(applicationContext, "Already in list", Toast.LENGTH_LONG).show()
                return
            }
        }
        addchip(tag)
        create_post_add_member_value.text = Editable.Factory.getInstance().newEditable("")
    }

    private fun addchip(tag: String){
        val chip = Chip(this)
        chip.text = tag
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener{
            removeTagFromList(chip.text.toString())
            create_post_tag_chip_group.removeView(chip)
        }
        tags.add(tag)
        create_post_tag_chip_group.addView(chip)

    }

    private fun removeTagFromList(tag: String){
        for( i in tags){
            if (i == tag){
                tags.remove(i)
                return
            }
        }
    }

    private fun createPost(){
        if ( create_post_post_title.text.toString().isEmpty()){
            Toast.makeText(applicationContext, "Post title cannot be empty", Toast.LENGTH_LONG).show()
            return
        }
        if (create_post_post_content.text.toString().isEmpty()){
            Toast.makeText(applicationContext, "Post content cannot be empty", Toast.LENGTH_LONG).show()
            return
        }
        val preference : SharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
        val userId : Long = preference.getLong("idUser", -1)!!
        val request : RequestModel.CreatePostRequest = RequestModel.CreatePostRequest(
            create_post_post_title.text.toString(),
            create_post_post_content.text.toString(),
            0,
            userId,
            tags,
            "null",
            "null"
        )
        NetworkManager.createPost(request, preference.getString("accessToken", null)!!)
            .enqueue(object: Callback<ResponseModel.PostResponse> {
                override fun onFailure(call: Call<ResponseModel.PostResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.PostResponse>,
                    response: Response<ResponseModel.PostResponse>
                ) {
                    if(response.code() != 201){
                        Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, "The post has been created", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }

            )
    }


}