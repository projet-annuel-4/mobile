package com.mehdi.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.adapter.RecyclerAdapter
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.RequestModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.search_post_result_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.time.LocalDate

class SearchPostResultActivity: AppCompatActivity() {
    private var filterRequest: RequestModel.FilterRequest? = null
    private lateinit var userConnectedPreferences: SharedPreferences
    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_post_result_activity)

        loadProperties()
        getResult()

        search_post_result_activity_back_arrow.setOnClickListener {
            finish()
        }
    }

    private fun loadProperties(){
        userConnectedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
        val intent = intent
        val extras = intent.extras
        filterRequest = Gson().fromJson((extras ?: return).getString("request"), RequestModel.FilterRequest::class.java)
    }

    private fun getResult(){
        GlobalScope.launch(Dispatchers.Default) {
            val response: MutableList<ResponseModel.PostResponse>
            try {
                response = NetworkManager.searchPostWithFilter(
                    userConnectedPreferences.getString("accessToken", null)!!,
                    filterRequest!!)
                var result = convertResponseToModel(response)
                result = orderByDescDate(result)
                withContext(Dispatchers.Main) {
                    recyclerAdapter.setData(result)
                    recycler_view_search_post.adapter = recyclerAdapter
                }
            } catch (t: Throwable) {
                print(t)
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
            }
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
                    img!!,
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