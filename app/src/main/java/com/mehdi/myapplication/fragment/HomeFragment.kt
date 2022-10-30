package com.mehdi.myapplication.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.CreatePostActivity
import com.mehdi.myapplication.R
import com.mehdi.myapplication.adapter.RecyclerAdapter
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.time.LocalDate

class HomeFragment: Fragment() {

    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()
    private lateinit var userConnectedPreferences : SharedPreferences
    private var feed: MutableList<DataModel.Post> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        home_fragment_create_post.setOnClickListener {
            val intent = Intent(activity, CreatePostActivity::class.java)
            startActivity(intent)
        }

        loadProperties()
        loadFeed()
    }

    private fun loadProperties(){
        userConnectedPreferences = this.activity?.getSharedPreferences("Login", Context.MODE_PRIVATE)!!
    }

    private fun loadFeed(){
        GlobalScope.launch(Dispatchers.Default) {
            val response: MutableList<ResponseModel.PostResponse>
            try {
                response = NetworkManager.getFeed(userConnectedPreferences.getLong("idUser", -1)!!, userConnectedPreferences.getString("accessToken", null)!!)
                feed = convertResponseToModel(response)
                feed.addAll(loadOtherPost())
                feed = orderByDescDate(feed)
                withContext(Dispatchers.Main) {
                    recyclerAdapter.setData(feed)
                    recycler_view_home.adapter = recyclerAdapter
                }
            } catch (t: Throwable) {
                print(t)
            }
        }
    }

    private fun loadOtherPost(): MutableList<DataModel.Post> {
        try {
            val getOtherPost : Call<MutableList<ResponseModel.PostResponse>> =
                NetworkManager.getAllPost(
                    userConnectedPreferences!!.getString("accessToken", null)!!
                )
            var otherPost = getOtherPost.execute().body()
            if (otherPost!!.size < 15) {
                return convertResponseToModel(otherPost)
            }
            otherPost = otherPost.slice(0..15) as MutableList<ResponseModel.PostResponse>
            return convertResponseToModel(otherPost)
        } catch (t: Throwable){
            return ArrayList()
        }
    }

    private fun convertResponseToModel(data: MutableList<ResponseModel.PostResponse>): MutableList<DataModel.Post> {
        try {
            val postLikedRequest = NetworkManager.getPostLikedByUser(userConnectedPreferences.getLong("idUser", -1),  userConnectedPreferences!!.getString("accessToken", null)!!)
            val postLiked = postLikedRequest.execute().body()
            val postCardList: MutableList<DataModel.Post> = ArrayList()

            for (i in data) {

                if( i.title == ""){
                    continue
                }

                val getUserInfo : Call<ResponseModel.UserInfoResponse> =
                    NetworkManager.getUserInfoByIdUser(
                        i.user.id!!,
                        userConnectedPreferences!!.getString("accessToken", null)!!
                    )
                var userInfoResponse = getUserInfo.execute().body()
                val img = if (userInfoResponse!!.imgUrl == null) "" else userInfoResponse!!.imgUrl
                val user = DataModel.UserInPost(
                    userInfoResponse!!.id,
                    userInfoResponse.firstName,
                    userInfoResponse.lastName,
                    img,
                )
                val postCard = DataModel.Post(
                    i.id,
                    user,
                    i.title,
                    i.creationDate,
                    i.content,
                    i.nbLike,
                    -1,
                    ArrayList(),
                    ArrayList(),
                    ArrayList(),
                    if (postLiked == null) false else postLiked!!.any {
                            pl -> pl.id == i.id
                    }
                )

                postCardList.add(postCard)

            }
            return postCardList
        } catch (t: Throwable){
            print(t)
        }
        return ArrayList()
    }

    private fun orderByDescDate(posts: MutableList<DataModel.Post>) : MutableList<DataModel.Post>{
        posts.sortByDescending {
            LocalDate.parse(it.date.split('T')[0])
        }

        return posts
    }
}