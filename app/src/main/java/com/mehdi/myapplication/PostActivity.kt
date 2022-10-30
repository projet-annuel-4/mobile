package com.mehdi.myapplication;

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.adapter.RecyclerAdapter
import com.mehdi.myapplication.adapter.RecyclerViewItemDecoration
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.answer_activity.*
import kotlinx.android.synthetic.main.answer_card.view.*
import kotlinx.android.synthetic.main.create_post_activity.*
import kotlinx.android.synthetic.main.post_activity.*
import kotlinx.android.synthetic.main.post_answer_card.*
import kotlinx.android.synthetic.main.post_answer_card.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class PostActivity : AppCompatActivity() {
        private var idPost : Long? = null
        private lateinit var post: DataModel.Post
        private lateinit var answers: MutableList<DataModel.PostAnswer>
        private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()
        private lateinit var userConnectedPreferences : SharedPreferences

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_activity)

                loadProperties()
                loadData()

                post_activity_back_arrow_imageButton.setOnClickListener{
                        finish()
                }


        }

        private val getResult =
                registerForActivityResult(
                        ActivityResultContracts.StartActivityForResult()) {
                        if(it.resultCode == Activity.RESULT_OK){
                                val result = it.data?.getStringExtra("answerCreated")
                                val answerCreated = Gson().fromJson(result, ResponseModel.PostResponse::class.java)
                                addAnswerToAnswerList(answerCreated)
                        }
                }

        private fun addAnswerToAnswerList(answer: ResponseModel.PostResponse){
                GlobalScope.launch(Dispatchers.Default) {
                        val answer = convertToAnswerModel(answer)
                        post.nbAnswers = post.nbAnswers +1
                        withContext(Dispatchers.Main) {
                                answers.add(answer)
                                post_activity_post_answers.text = getString(R.string.nb_answers, post.nbAnswers.toString())
                                print("eeeeeeeee")
                                recyclerAdapter.setData(answers)
                                post_activity_post_answers_recyclerview.scrollToPosition(answers.size - 1)
                        }
                }
        }


        private fun setActions(){
                post_activity_post_answers_action.setOnClickListener {
                        val intent = Intent(this, AnswerActivity::class.java)
                        intent.putExtra("post", Gson().toJson(post))
                        getResult.launch(intent)
                }
        }

        private fun loadProperties(){
                val intent = intent
                val extras = intent.extras
                idPost =(extras ?: return).getLong("idPost")
                userConnectedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
        }

        private fun loadData(){
                loadPost()
        }

        private fun loadPost(){
                GlobalScope.launch(Dispatchers.Default) {
                        val response: ResponseModel.PostResponse
                        try {
                                response = NetworkManager.getPostByIdPost(
                                        idPost!!,
                                        userConnectedPreferences!!.getString("accessToken", null)!!
                                )
                                post = convertPostResponseToModel(response)!!


                                setUserImage(post.creator.id)


                                withContext(Dispatchers.Main) {
                                        loadPostAnswers()
                                }
                        } catch (t: Throwable) {
                                print(t)
                        }
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
                                        print(userProfilImage)
                                }
                        }
                        withContext(Dispatchers.Main) {
                                if (imageToBitmap != null) {
                                        post_activity_post_user_avatar_image.setImageBitmap(imageToBitmap)
                                }

                        }
                }
        }

        fun convertPostResponseToModel(data:ResponseModel.PostResponse): DataModel.Post? {
                try {
                        val postLikedRequest = NetworkManager.getPostLikedByUser(userConnectedPreferences.getLong("idUser", -1),  userConnectedPreferences!!.getString("accessToken", null)!!)
                        val postLiked = postLikedRequest.execute().body()

                        val getUserInfo : Call<ResponseModel.UserInfoResponse> =
                                NetworkManager.getUserInfoByIdUser(
                                        data.user.id!!,
                                        userConnectedPreferences!!.getString("accessToken", null)!!
                                )
                        var userInfoResponse = getUserInfo.execute().body()
                        val img = if (userInfoResponse!!.imgUrl == null) "" else userInfoResponse!!.imgUrl
                        val user = DataModel.UserInPost(
                                userInfoResponse!!.id,
                                userInfoResponse.firstName,
                                userInfoResponse.lastName,
                                img!!,
                        )
                        val postModel = DataModel.Post(
                                data.id,
                                user,
                                data.title,
                                data.creationDate,
                                data.content,
                                data.nbLike,
                                -1,
                                ArrayList(),
                                ArrayList(),
                                ArrayList(),
                                postLiked!!.any {
                                                pl -> pl.id == data.id
                                }
                        )

                        return postModel
                }
                catch (t: Throwable){
                        print(t)
                }
                return null
        }

        private fun convertToTagModel(data: MutableList<ResponseModel.TagResponse>) : MutableList<DataModel.Tags>{
                var tags: MutableList<DataModel.Tags> = kotlin.collections.ArrayList()
                for ( i in data){
                        tags.add(DataModel.Tags(i.name))
                }
                return tags
        }
//        private fun getPostCreator()

        private fun loadPostAnswers(){
                GlobalScope.launch(Dispatchers.Default) {
                        val response: MutableList<ResponseModel.PostResponse>
                        try {
                                response = NetworkManager.getPostAnswersByIdPost(
                                        idPost!!,
                                        userConnectedPreferences!!.getString("accessToken", null)!!
                                )
                                answers = convertToListAnswerModel(response)
                                answers = orderByAscDate(answers)
                                withContext(Dispatchers.Main) {
                                        post_activity_post_answers_recyclerview.addItemDecoration(
                                                RecyclerViewItemDecoration(applicationContext, R.drawable.recyclerview_divider)
                                        )
                                        recyclerAdapter.setData(answers)
                                        post.nbAnswers = response.size
                                        post_activity_post_answers_recyclerview.adapter = recyclerAdapter
                                        setDataOnLayout()
                                        setActions()
                                }
                        } catch (t: Throwable) {
                                print(t)
                        }
                }
        }

        private fun convertToListAnswerModel(response : MutableList<ResponseModel.PostResponse>): MutableList<DataModel.PostAnswer> {
                var answers : MutableList<DataModel.PostAnswer> = ArrayList()
                val postLikedRequest = NetworkManager.getPostLikedByUser(userConnectedPreferences.getLong("idUser", -1),  userConnectedPreferences!!.getString("accessToken", null)!!)
                val postLiked = postLikedRequest.execute().body()

                for (i in response){

                        var answerCreator = getAnswerCreator(i.user.id!!)

                        var answer = DataModel.PostAnswer(
                                i.id,
                                answerCreator!!,
                                i.creationDate,
                                i.content,
                                i.nbLike,
                                ArrayList(),
                                if (postLiked == null) false else postLiked!!.any {
                                                pl -> pl.id == i.id
                                }
                        )
                        answers.add(answer)
                }
                return answers
        }

        private fun convertToAnswerModel(response : ResponseModel.PostResponse): DataModel.PostAnswer {

                var answerCreator = getAnswerCreator(response.user.id!!)

                var answer = DataModel.PostAnswer(
                        response.id,
                        answerCreator!!,
                        response.creationDate,
                        response.content,
                        response.nbLike,
                        ArrayList(),
                        false
                )

                return answer
        }

        private fun getAnswerCreator(creatorId: Long): DataModel.UserInAnswer? {

                try {
                        val getUserInfo : Call<ResponseModel.UserInfoResponse> =
                                NetworkManager.getUserInfoByIdUser(
                                        creatorId,
                                        userConnectedPreferences!!.getString("accessToken", null)!!
                                )
                        val userInfoResponse = getUserInfo.execute().body()

                        return DataModel.UserInAnswer(
                                userInfoResponse!!.id,
                                userInfoResponse.firstName,
                                userInfoResponse.lastName,
                                userInfoResponse.email,
                        )
                } catch (t: Throwable){
                        print(t)
                }
                return null
        }

        private fun setDataOnLayout(){
                post_activity_post_username.text = post.creator!!.firstname + " " + post.creator!!.lastname
                post_activity_post_title.text = post.title
                post_activity_post_content.text = post.content
                post_activity_post_date.text = getDateTime(post.date)
                post_activity_post_answers.text = getString(R.string.nb_answers, post.nbAnswers.toString())
                post_activity_post_user_avatar.setOnClickListener{
                        val intent = Intent(applicationContext, ProfileActivity::class.java)
                        intent.putExtra("idUser", post.creator.id)
                        startActivity(intent)
                }
                post_activity_post_username.setOnClickListener{
                        val intent = Intent(applicationContext, ProfileActivity::class.java)
                        intent.putExtra("idUser", post.creator.id)
                        startActivity(intent)
                }
                // todo replace string by values string
                post_activity_post_likes.text = getString(R.string.nb_likes, post.nbLikes.toString())
                if ( post.liked ){
                        post_activity_post_like_icon.setBackgroundResource(R.drawable.ic_like_full)
                } else {
                        post_activity_post_like_icon.setBackgroundResource(R.drawable.ic_like_empty)
                }

                post_activity_post_like_icon.setOnClickListener{
                        manageLikeButtonClick()
                }
        }

        private fun manageLikeButtonClick(){
                if( !post.liked){
                        likePost()
                } else {
                        dislikePost()
                }
        }

        private fun likePost(){
                try {
                        GlobalScope.launch(Dispatchers.Default) {
                                val likeRequest = NetworkManager.likePost(
                                        userConnectedPreferences.getLong("idUser", -1),
                                        post.id,
                                        userConnectedPreferences!!.getString("accessToken", null)!!
                                )
                                val liked = likeRequest.execute()
                                if (liked.isSuccessful) {
                                        withContext(Dispatchers.Main) {
                                                post.nbLikes += 1
                                                post.liked = true
                                                post_activity_post_like_icon.setBackgroundResource(R.drawable.ic_like_full)
                                                post_activity_post_likes.text = getString(R.string.nb_likes, post.nbLikes.toString())
                                        }
                                } else {
                                        withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                        applicationContext,
                                                        "Unable to like, something went wrong",
                                                        Toast.LENGTH_LONG
                                                ).show()
                                        }
                                }
                        }
                } catch (t: Throwable){
                        print(t)
                }
        }

        private fun dislikePost(){
                GlobalScope.launch(Dispatchers.Default) {
                        val dislikeRequest = NetworkManager.dislikePost(
                                userConnectedPreferences.getLong("idUser", -1),
                                post.id,
                                userConnectedPreferences!!.getString("accessToken", null)!!
                        )
                        val disliked = dislikeRequest.execute()
                        if (disliked.isSuccessful) {
                                withContext(Dispatchers.Main) {
                                        post.nbLikes -= 1
                                        post.liked = false
                                        post_activity_post_like_icon.setBackgroundResource(R.drawable.ic_like_empty)
                                        post_activity_post_likes.text = getString(R.string.nb_likes, post.nbLikes.toString())

                                }
                        } else {
                                withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                                applicationContext,
                                                "Unable to dislike, something went wrong",
                                                Toast.LENGTH_LONG
                                        ).show()
                                }
                        }
                }
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

        private fun orderByAscDate(posts: MutableList<DataModel.PostAnswer>) : MutableList<DataModel.PostAnswer>{
                posts.sortedBy {
                        LocalDate.parse(it.date.split('T')[0])
                }

                return posts
        }
}
