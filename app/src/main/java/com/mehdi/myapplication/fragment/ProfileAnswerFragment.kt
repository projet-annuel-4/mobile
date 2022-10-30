package com.mehdi.myapplication.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.R
import com.mehdi.myapplication.adapter.RecyclerAdapter
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.RequestModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.message_fragment.*
import kotlinx.android.synthetic.main.profile_answer_fragment.*
import kotlinx.android.synthetic.main.profile_post_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.time.LocalDate

class ProfileAnswerFragment: Fragment() {

    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()
    private var idUser: Long? = null
    private lateinit var userConnectedPreferences : SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_answer_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProperties()
        loadAnswers()
    }

    private fun loadProperties(){
        userConnectedPreferences = this.activity?.getSharedPreferences("Login", Context.MODE_PRIVATE)!!
        idUser = arguments!!.getLong("idUser")
    }

    private fun loadAnswers(){

        GlobalScope.launch(Dispatchers.Default) {
            val response: MutableList<ResponseModel.PostResponse>
            try {
                response = NetworkManager.getUserAnswersByIdUser(idUser!!, userConnectedPreferences!!.getString("accessToken", null)!!)
                print(userConnectedPreferences!!.getLong("idUser", -1)!!)
                var convertedResponse = convertPostResponseToModel(response)
                convertedResponse = orderByDescDate(convertedResponse as MutableList<DataModel.Post>)
                withContext(Dispatchers.Main) {
                    recyclerAdapter.setData(convertedResponse)
                    profil_answer_fragment_recycler_view.adapter = recyclerAdapter
                }
            } catch (t: Throwable) {
                print(t)
            }
        }
    }
    fun convertPostResponseToModel(data: MutableList<ResponseModel.PostResponse>): List<DataModel.Post> {
        try {
            val postLikedRequest = NetworkManager.getPostLikedByUser(userConnectedPreferences.getLong("idUser", -1),  userConnectedPreferences!!.getString("accessToken", null)!!)
            val postLiked = postLikedRequest.execute().body()
            val postCardList: MutableList<DataModel.Post> = ArrayList()

            for (i in data) {

                if( i.title != ""){
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