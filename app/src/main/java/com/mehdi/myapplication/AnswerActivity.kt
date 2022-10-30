package com.mehdi.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import com.google.gson.Gson
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.adapter.RecyclerAdapter
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.RequestModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.answer_activity.*
import kotlinx.android.synthetic.main.create_post_activity.*
import kotlinx.android.synthetic.main.post_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AnswerActivity : AppCompatActivity() {
    private lateinit var post: DataModel.Post
    private lateinit var userConnectedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.answer_activity)

        answer_activity_back_arrow_imageButton.setOnClickListener{
            setResult(Activity.RESULT_CANCELED, null)
            finish()
        }

        loadProperties()
        setDataOnLayout()
        setAction()
        setUserImage(post.creator.id)
    }

    private fun setAction(){
        answer_activity_send_action.setOnClickListener {
            createPost()
        }
    }

    private fun setUserImage(userId: Long){
        GlobalScope.launch {
            val getUserProfilImage: Call<ResponseModel.ImageResponse> =
                NetworkManager.getUserProfilImage(
                    userId,
                    userConnectedPreferences!!.getString("accessToken", null)!!
                )
            var userProfilImage = getUserProfilImage.execute().body()
            var imageToBitmap: Bitmap? = null
            if (userProfilImage != null) {
                if ( userProfilImage.file != null) {
                    val imageToByte = Base64.getDecoder().decode(userProfilImage!!.file)
                    imageToBitmap =
                        BitmapFactory.decodeByteArray(imageToByte, 0, imageToByte.size)
                    //print(userProfilImage)
                }
            }
            withContext(Dispatchers.Main) {
                if (imageToBitmap != null) {
                    answer_activity_post_user_avatar_image.setImageBitmap(imageToBitmap)
                }

            }
        }
    }

    private fun setDataOnLayout(){
        //todo add user avatar
        answer_activity_post_username.text = post.creator!!.firstname + " " + post.creator!!.lastname
        answer_activity_post_content.text = post.content
        answer_activity_post_date.text = getDateTime(post.date)
    }

    private fun loadProperties(){
        val intent = intent
        val extras = intent.extras
        post = Gson().fromJson((extras ?: return).getString("post"), DataModel.Post::class.java)
        userConnectedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
    }

    private fun createPost(){
        if ( answer_activity_user_answer.text.toString().isEmpty() ){
            Toast.makeText(applicationContext, "You can't answer with nothing", Toast.LENGTH_LONG).show()
            return
        }
        val preference : SharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
        val userId : Long = preference.getLong("idUser", -1)!!
        val request : RequestModel.CreatePostRequest = RequestModel.CreatePostRequest(
            "",
            answer_activity_user_answer.text.toString(),
            0,
            userId,
            listOf<String>(),
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
                        linkAnswerToPost(response.body()!!.id, response.body()!!)
                    }
                }
            }
            )
    }

    private fun linkAnswerToPost(answerId: Long, answerCreated: ResponseModel.PostResponse ){
        val request : RequestModel.CommentRequest =
            RequestModel.CommentRequest(
                post.id,
                answerId,
                userConnectedPreferences.getLong("idUser", -1)!!
            )
        NetworkManager.createLinkAnswerToPost(request, userConnectedPreferences.getString("accessToken", null)!! )
            .enqueue(object: Callback<ResponseModel.CommentResponse> {
                override fun onFailure(call: Call<ResponseModel.CommentResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.CommentResponse>,
                    response: Response<ResponseModel.CommentResponse>
                ) {
                    if(response.code() != 202){
                        Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                    } else {
                        print(response.body())
                        Toast.makeText(applicationContext, "The answer has been posted", Toast.LENGTH_LONG).show()
                        val resultIntent = Intent()
                        resultIntent.putExtra("answerCreated", Gson().toJson(answerCreated))
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()

                    }
                }
            })
    }

    private fun getDateTime(timeStamp: String): String? {
        var timeStampSplit: kotlin.collections.List<String> = timeStamp.split('T')
        var date: String = timeStampSplit[0]
        date = date.getDateInAnotherFormat("yyyy-MM-dd","dd/MM/YYYY")
        date = date.replace('-', '/')
        var time = timeStampSplit[1]
        var timeSplit: kotlin.collections.List<String> = time.split(":")
        time = timeSplit[0] + ':' + timeSplit[1]
        return "$date,$time"
    }

    fun String.getDateInAnotherFormat(inputFormat: String,outputFormat: String):String = SimpleDateFormat(inputFormat, Locale.getDefault()).parse(this)?.let { SimpleDateFormat(outputFormat,
        Locale.getDefault()).format(it) }?:""
}