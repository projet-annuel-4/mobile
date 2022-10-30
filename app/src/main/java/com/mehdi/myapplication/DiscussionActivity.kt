package com.mehdi.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.adapter.RecyclerAdapter
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.RequestModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.discussion_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class DiscussionActivity: AppCompatActivity() {

    private lateinit var conversation: DataModel.Conversation
    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()
    private lateinit var userConnectedPreferences : SharedPreferences
    private lateinit var message: MutableList<DataModel.Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.discussion_activity)

        discussion_back_arrow.setOnClickListener{
            finish()
        }

        loadProperties()
        setDataOnLayout()
        isExistDiscussion()
        button_chat_send.setOnClickListener {
            sendMessage()
        }

//        val timer = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
//            loadMessages()
//        },0,2, TimeUnit.SECONDS)
    }


    private fun loadProperties(){
        val intent = intent
        val extras = intent.extras
        conversation =
            Gson().fromJson((extras ?: return).getString("user"), DataModel.Conversation::class.java)
        userConnectedPreferences = getSharedPreferences("Login", MODE_PRIVATE);
    }

    private fun setDataOnLayout(){
        discussion_activity_header.text = conversation.userName
    }

    private fun isExistDiscussion(){
        NetworkManager.isExistDiscussion(
            userConnectedPreferences.getLong("idUser", -1)!!,
            conversation.friendId,
            userConnectedPreferences.getString("accessToken", null)!!)
            .enqueue(object: Callback<ResponseModel.FriendProfileResponse> {
                override fun onFailure(call: Call<ResponseModel.FriendProfileResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.FriendProfileResponse>,
                    response: Response<ResponseModel.FriendProfileResponse>
                ) {
                    if(response.code() != 200){
                        startDiscussion()
                    } else {
                        conversation.id = response.body()!!.id.toLong()
                        loadMessages()
                    }
                }
            }
            )
    }

    private fun startDiscussion(){
        NetworkManager.startConversation(
            userConnectedPreferences.getString("email", null)!!,
            conversation.friendEmail,
            userConnectedPreferences.getString("accessToken", null)!!)
            .enqueue(object: Callback<ResponseModel.FriendProfileResponse> {
                override fun onFailure(call: Call<ResponseModel.FriendProfileResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.FriendProfileResponse>,
                    response: Response<ResponseModel.FriendProfileResponse>
                ) {
                    if(response.code() != 200){
                        Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        conversation.id = response.body()!!.id.toLong()
                    }
                }
            }

            )
    }

    private fun sendMessage(){
        if( conversation.id == null){
            startDiscussion()
        }
        NetworkManager.sendMessage(
            conversation.id!!,
            userConnectedPreferences.getString("email", null)!!,
            send_chat_message.text.toString(),
            userConnectedPreferences.getString("accessToken", null)!!)
            .enqueue(object: Callback<ResponseModel.MessageResponse> {
                override fun onFailure(call: Call<ResponseModel.MessageResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.MessageResponse>,
                    response: Response<ResponseModel.MessageResponse>
                ) {
                    if(response.code() != 200){
                        Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                    } else {
                        var messageSended = response.body()!!
                        var messageSendedConverted = convertResponseToModel(
                            messageSended,
                            userConnectedPreferences.getLong("idUser", -1)!! )
                        message.add(messageSendedConverted)
                        recyclerAdapter.setData(message)
                        discussion_recycler_view.scrollToPosition(message.size-1)
                        send_chat_message.text = Editable.Factory.getInstance().newEditable("")
                    }
                }
            }

            )
    }

    private fun loadMessages(){
        GlobalScope.launch(Dispatchers.Default) {
            val request = RequestModel.GetAllMessageRequest(mutableListOf(conversation.id!!))
            val response: MutableList<ResponseModel.MessageResponse>
            try {
                response = NetworkManager.getAllMessage(userConnectedPreferences.getString("email", null)!!, conversation.id!!, userConnectedPreferences.getString("accessToken", null)!!)
                withContext(Dispatchers.Main) {
                    val userId : Long = userConnectedPreferences.getLong("idUser", -1)!!
                    message = convertResponseTo(response, userId) as MutableList<DataModel.Message>
                    recyclerAdapter.setData(message)
                    discussion_recycler_view.adapter = recyclerAdapter
                }
            } catch (t: Throwable) {
                print(t)
            }
        }
    }

    fun convertResponseTo(data: MutableList<ResponseModel.MessageResponse>, userId: Long): List<DataModel.Message> {
        val messageCardList : MutableList<DataModel.Message> = ArrayList()
        for (i in data) {
            val messageCard = DataModel.Message(
                i.id, i.senderId, i.content, getDateTime(i.createdAt)!!, userId)

            messageCardList.add(messageCard)
        }
        return messageCardList;
    }

    fun convertResponseToModel(response: ResponseModel.MessageResponse, userId: Long): DataModel.Message {

            val messageCard = DataModel.Message(
                response.id, response.senderId, response.content, getDateTime(response.createdAt)!!, userId)

        return messageCard;
    }

    private fun getDateTime(timeStamp: String): String? {
        var timeStampSplit: List<String> = timeStamp.split('T')
        var date: String = timeStampSplit[0]
        date = date.getDateInAnotherFormat("yyyy-MM-dd","dd/MM/YYYY")
        date = date.replace('-', '/')
        var time = timeStampSplit[1]
        var timeSplit: List<String> = time.split(":")
        time = timeSplit[0] + ':' + timeSplit[1]
        return "$date,$time"
    }

    fun String.getDateInAnotherFormat(inputFormat: String,outputFormat: String):String = SimpleDateFormat(inputFormat, Locale.getDefault()).parse(this)?.let { SimpleDateFormat(outputFormat,Locale.getDefault()).format(it) }?:""


}