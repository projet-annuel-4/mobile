package com.mehdi.myapplication.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.DiscussionActivity
import com.mehdi.myapplication.R
import com.mehdi.myapplication.adapter.RecyclerAdapter
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.create_group_activity.*
import kotlinx.android.synthetic.main.message_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatFragment: Fragment() {

    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()
    private lateinit var userConnectedPreferences : SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.message_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        chat_fragment_start_discussion.setOnClickListener {
            chat_fragment_form_start_discussion.visibility = View.VISIBLE
        }

        chat_fragment_form_start_disccussion_close.setOnClickListener {
            chat_fragment_form_start_discussion.visibility = View.GONE
        }

        chat_fragment_form_start_discussion_button.setOnClickListener {
            startDiscussion()
        }


        userConnectedPreferences = this.activity!!.getSharedPreferences("Login", Context.MODE_PRIVATE);
        GlobalScope.launch(Dispatchers.Default) {
            val response: MutableList<ResponseModel.FriendProfileResponse>
            try {
                response = NetworkManager.getFriendsChat(userConnectedPreferences.getString("email", null)!!, userConnectedPreferences.getString("accessToken", null)!!)
                recyclerAdapter.setData(convertResponseTo(response))
                withContext(Dispatchers.Main) {
                    recycler_view_message.adapter = recyclerAdapter
                }
            } catch (t: Throwable) {
                print(t)
            }
        }
    }

    fun convertResponseTo(data: MutableList<ResponseModel.FriendProfileResponse>): List<DataModel.Conversation> {
        val conversationList : MutableList<DataModel.Conversation> = ArrayList()
        for (i in data) {

            val getLastMessage: Call<ResponseModel.MessageResponse> =
                NetworkManager.getLastMessageByConversationId(i.id.toLong(), userConnectedPreferences!!.getString("accessToken", null)!!)
            val lastMessage = getLastMessage.execute().body()

            var lastMessageModel = if (lastMessage == null) null else DataModel.Message(
                lastMessage.id,
                lastMessage.senderId,
                lastMessage.content,
                lastMessage.createdAt,
                userConnectedPreferences.getLong("idUser", -1)
            )

            val userCard = DataModel.Conversation(
                i.id.toLong(),
                i.friendId.toLong(),
                i.firstName + ' ' + i.lastName,
                i.imgUrl,
                i.email,
                lastMessageModel
            )
            conversationList.add(userCard)
        }
        return conversationList;
    }

    private fun startDiscussion(){
        var email = chat_fragment_form_email_member.text.toString()
        getUserByEmail(email)
//        if (user != null) {
//            val intent = Intent(context, DiscussionActivity::class.java)
//            intent.putExtra("user", Gson().toJson(user))
//            context!!.startActivity(intent)
//        }
    }

    private fun getUserByEmail(email: String)  {
        NetworkManager.getUserInfoByEmail(email, userConnectedPreferences.getString("accessToken", null)!!)
            .enqueue(object: Callback<ResponseModel.UserInfoResponse> {
                override fun onFailure(call: Call<ResponseModel.UserInfoResponse>, t: Throwable) {
                    Toast.makeText(context, "The user doesn't exist", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.UserInfoResponse>,
                    response: Response<ResponseModel.UserInfoResponse>
                ) {
                    if(response.code() != 200){
                        Toast.makeText(context, "The user doesn't exist", Toast.LENGTH_LONG).show()
                    } else {
                        var user = response!!.body()
                        val intent = Intent(context, DiscussionActivity::class.java)
                        val interlocutor  = DataModel.Conversation(
                            null,
                            user!!.id,
                            user!!.firstName + " " + user!!.lastName,
                            user!!.imgUrl,
                            user!!.email!!,
                            null
                        )
                        intent.putExtra("user", Gson().toJson(interlocutor))
                        startActivity(intent)
                    }
                }
            }
            )
    }

    private fun convertResponseToUserCardChat(response: ResponseModel.UserInfoResponse): DataModel.UserCardChat {
        return DataModel.UserCardChat(
            response.id,
            response.firstName + " " + response.lastName,
            response.email,
            response.imgUrl
        )
    }




}