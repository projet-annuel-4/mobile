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
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.profile_friends_fragment.*
import kotlinx.android.synthetic.main.profile_post_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call

class ProfileFollowerFragment : Fragment() {

    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()
    private var idUser: Long? = null
    private lateinit var userConnectedPreferences : SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_friends_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProperties()
        loadFollowers()

    }

    private fun loadProperties(){
        userConnectedPreferences = this.activity?.getSharedPreferences("Login", Context.MODE_PRIVATE)!!
        idUser = arguments!!.getLong("idUser")
    }

    private fun loadFollowers(){
        GlobalScope.launch(Dispatchers.Default) {
            val response: MutableList<ResponseModel.FollowerResponse>
            try {
                response = NetworkManager.getUserFollowersByIdUser(
                    idUser!!,
                    userConnectedPreferences!!.getString("accessToken", null)!!
                )

                var followersInfo : MutableList<DataModel.UserProfileInfo> =
                    getFollowersInfo(response)
                var followers = convertFollowerInfoIntoFollower(followersInfo)
                withContext(Dispatchers.Main) {
                    print(response)
                    recyclerAdapter.setData(followers)
                    profil_follower_recyclerview.adapter = recyclerAdapter
                }
            } catch (t: Throwable) {
                print(t)
            }
        }
    }

    private fun getFollowersInfo(followersResponse : MutableList<ResponseModel.FollowerResponse>): MutableList<DataModel.UserProfileInfo> {
        var followers : MutableList<DataModel.UserProfileInfo> = ArrayList()

        for (i in followersResponse){
            val getUserInfo : Call<ResponseModel.UserInfoResponse> =
                NetworkManager.getUserInfoByIdUser(
                    i.id,
                    userConnectedPreferences!!.getString("accessToken", null)!!
                )
            var userInfoResponse = getUserInfo.execute().body()

            var follower = DataModel.UserProfileInfo(
                userInfoResponse!!.id,
                userInfoResponse!!.firstName,
                userInfoResponse!!.lastName,
                userInfoResponse!!.email,
                userInfoResponse!!.city,
                0,
                0,
                userInfoResponse!!.imgUrl
            )

            followers.add(follower)
        }

        return followers
    }

    private fun convertFollowerInfoIntoFollower(followersInfo: MutableList<DataModel.UserProfileInfo>) :
            MutableList<DataModel.UserFollow>{
        var followers: MutableList<DataModel.UserFollow> = ArrayList()
        for (i in followersInfo){
            val follower = DataModel.UserFollow(
                i.id,
                i.firstname + " " + i.lastname,
                "",
                "secret place"
            )

            followers.add( follower)
        }
        return followers
    }
}