package com.mehdi.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64.NO_WRAP
import android.util.Base64.decode
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.adapter.ProfilTableAdapter
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.profile_activity.*
import kotlinx.android.synthetic.main.profile_post_fragment.*
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.Byte.decode
import java.util.*


class ProfileActivity: AppCompatActivity() {
    private var idUser : Long? = null
    private lateinit var user : DataModel.UserProfileInfo
    private lateinit var userConnectedPreferences : SharedPreferences
    private var isFollowing: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        loadProperties()
        loadUser()

        profile_view_pager.adapter = ProfilTableAdapter(this, idUser!!)
        TabLayoutMediator(tab_layout, profile_view_pager){ tab: TabLayout.Tab, i: Int ->
            if (i == 0) {
                tab.text = "Posts"
            } else if (i == 1) {
                tab.text = "Answers"
            } else {
                tab.text = "Followers"
            }
        }.attach()

        profile_user_back_arrow.setOnClickListener{
            finish()
        }
    }

    private fun loadUser(){
        try {
            GlobalScope.launch(Dispatchers.Default) {
                val getUserInfo: Call<ResponseModel.UserInfoResponse> =
                    NetworkManager.getUserInfoByIdUser(
                        idUser!!,
                        userConnectedPreferences!!.getString("accessToken", null)!!
                    )
                var userInfoResponse = getUserInfo.execute().body()

                val getUserProfilImage: Call<ResponseModel.ImageResponse> =
                    NetworkManager.getUserProfilImage(
                        idUser!!,
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

                var responsePost : List<ResponseModel.PostResponse> =
                    NetworkManager.getUserPostsByIdUser(
                    idUser!!,
                    userConnectedPreferences!!.getString("accessToken", null)!!
                )
                responsePost = responsePost.filter { it.title != "" }
                val responseFollower : List<ResponseModel.FollowerResponse> =
                    NetworkManager.getUserFollowersByIdUser(
                        idUser!!,
                        userConnectedPreferences!!.getString("accessToken", null)!!
                    )
                setUserProfileInfo(
                    userInfoResponse!!,responsePost,responseFollower
                )
                if (idUser != userConnectedPreferences.getLong("idUser", -1)) {
                    checkIsFollowing()
                }
                withContext(Dispatchers.Main) {
                    setDataOnLayout(user)
                    manageButtons()
                    if (imageToBitmap != null) {
                        profile_user_avatar_image.setImageBitmap(imageToBitmap)
                    }

                }
            }
        } catch (t: Throwable) {
            print(t)
        }
    }

    private fun checkIsFollowing(){
        NetworkManager.isFollowing(
            userConnectedPreferences.getLong("idUser", -1)!!,
            user.id,
            userConnectedPreferences.getString("accessToken", null)!! )
            .enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(applicationContext, "The user doesn't exist", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    isFollowing = response.code() == 200
                    manageFollowState()

                }
            }
            )
    }

    private fun follow(){
        NetworkManager.follow(
            userConnectedPreferences.getLong("idUser", -1)!!,
            user.id,
            userConnectedPreferences.getString("accessToken", null)!! )
            .enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(applicationContext, "The user doesn't exist", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    if(response.code() == 202){
                        isFollowing = true
                        user.nbFollow = user.nbFollow?.plus(1)
                        profile_user_nb_friends.text = getString(R.string.nb_followers, user.nbFollow.toString())
                        manageFollowState()
                    }
                }
            }
            )
    }

    private fun unfollow(){
        NetworkManager.unfollow(
            userConnectedPreferences.getLong("idUser", -1)!!,
            user.id,
            userConnectedPreferences.getString("accessToken", null)!! )
            .enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(applicationContext, "The user doesn't exist", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    if(response.code() == 202){
                        isFollowing = false
                        user.nbFollow = user.nbFollow?.minus(1)
                        profile_user_nb_friends.text = getString(R.string.nb_followers, user.nbFollow.toString())
                        manageFollowState()
                    }
                }
            }
            )
    }

    private fun setUserProfileInfo(user : ResponseModel.UserInfoResponse,
        posts: List<ResponseModel.PostResponse>,
        followers: List<ResponseModel.FollowerResponse>){
        this.user = DataModel.UserProfileInfo(
            user.id,
            user.firstName,
            user.lastName,
            user.email,
            user.city  ?: "secret place",
            followers.size,
            posts.size,
            user.imgUrl ?: "url "
        )
    }

    private fun setDataOnLayout(user : DataModel.UserProfileInfo){
        print(user)
        profile_user_username.text = user.firstname + ' ' + user.lastname
        profile_user_description.text = user.city
        profile_user_nb_friends.text = getString(R.string.nb_followers, user.nbFollow.toString())
        profile_user_nb_post.text = getString(R.string.nb_posts, user.nbPosts.toString())
        try {
            if (user.id == userConnectedPreferences!!.getLong("idUser", -1)) {
                follow_button.visibility = View.GONE
                message_logo.visibility = View.GONE
            } else {
                print( user.id)
                print(userConnectedPreferences!!.getLong("idUser", -1)!! )
            }
        } catch (t: Throwable){
            print(t)
        }
    }

    private fun loadProperties(){
        userConnectedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
        val intent = intent
        val extras = intent.extras
        idUser = (extras ?: return).getLong("idUser")
        userConnectedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
    }

    private fun manageButtons(){
        manageFollowButton()
        manageMessageButton()
    }

    private fun manageFollowState(){
        if ( isFollowing == true ){
            follow_button.visibility = View.GONE
            unfollow_button.visibility = View.VISIBLE
        } else {
            follow_button.visibility = View.VISIBLE
            unfollow_button.visibility = View.GONE
        }
    }

    private fun manageFollowButton(){
        if (idUser == userConnectedPreferences.getLong("idUser", -1)){
            follow_button.visibility = View.GONE
            unfollow_button.visibility = View.GONE
        } else {
            follow_button.setOnClickListener {
                follow()
            }
            unfollow_button.setOnClickListener {
                unfollow()
            }


        }
    }

    private fun manageMessageButton(){
        if (idUser == userConnectedPreferences.getLong("idUser", -1)){
            message_logo.visibility = View.GONE
        } else {
            message_logo.setOnClickListener {
                val intent = Intent(this, DiscussionActivity::class.java)
                val interlocutor  = DataModel.Conversation(
                    null,
                    user.id,
                    user.firstname + " " + user.lastname,
                    user.avatarUrl,
                    user.email!!,
                    null
                )
                intent.putExtra("user", Gson().toJson(interlocutor))
                startActivity(intent)
            }
        }
    }
}