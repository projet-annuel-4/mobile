package com.mehdi.myapplication.holder;

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.ContactsContract.Data
import android.provider.Settings.Global.getString
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.mehdi.myapplication.*
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.API.NetworkManager.dislikePost
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.ResponseModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.add_member_in_group.view.*
import kotlinx.android.synthetic.main.answer_card.view.*
import kotlinx.android.synthetic.main.group_card.view.*
import kotlinx.android.synthetic.main.header_chat.*
import kotlinx.android.synthetic.main.header_search.*
import kotlinx.android.synthetic.main.left_menu.*
import kotlinx.android.synthetic.main.message_chat_card_right.view.*
import kotlinx.android.synthetic.main.message_chat_card_left.view.*
import kotlinx.android.synthetic.main.post_answer_card.view.*
import kotlinx.android.synthetic.main.post_card.view.*
import kotlinx.android.synthetic.main.post_card.view.post_card_post_content
import kotlinx.android.synthetic.main.project_card.view.*
import kotlinx.android.synthetic.main.subscription_card.view.*
import kotlinx.android.synthetic.main.user_chat_card.view.*
import kotlinx.android.synthetic.main.user_in_group_card.view.*
import kotlinx.android.synthetic.main.user_post_card.view.*
import kotlinx.android.synthetic.main.userfollow_card.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonDisposableHandle.parent
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.*

class DataAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {


    private fun bindPost(item: DataModel.Post) {
        itemView.post_card_post_title.text = item.title
        itemView.post_card_post_content.text = item.content
        itemView.post_card_post_date.text = getDateTime(item.date
        )        //val image = itemView.post_card_user_avatar
        //Picasso.get().load(item.creator.avatarUrl).into(image)
        itemView.setOnClickListener {
            val intent = Intent(itemView.context, PostActivity::class.java)
            intent.putExtra("idPost", item.id)
            itemView.context.startActivity(intent)
        }

        val userConnectedPreferences = itemView.context.getSharedPreferences("Login", MODE_PRIVATE)
        GlobalScope.launch {
            val getUserProfilImage: Call<ResponseModel.ImageResponse> =
                NetworkManager.getUserProfilImage(
                    item.creator.id!!,
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
                    itemView.post_card_user_avatar_image.setImageBitmap(imageToBitmap)
                }

            }
        }
    }

    private fun bindAnswer(item: DataModel.Answer) {
        //itemView.answer_card_answer_to.text = item.postTitle
        itemView.answer_card_answer_content.text = item.content
        val image = itemView.answer_card_user_avatar
        itemView.setOnClickListener {
            val intent = Intent(itemView.context, PostActivity::class.java)
            intent.putExtra("answer", Gson().toJson(item))
            itemView.context.startActivity(intent)
        }

        val userConnectedPreferences = itemView.context.getSharedPreferences("Login", MODE_PRIVATE)
        GlobalScope.launch {
            val getUserProfilImage: Call<ResponseModel.ImageResponse> =
                NetworkManager.getUserProfilImage(
                    item.creator.id!!,
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
                    itemView.answer_card_user_avatar_image.setImageBitmap(imageToBitmap)
                }

            }
        }
    }

    private fun bindPostAnswer(item: DataModel.PostAnswer){

        val userConnectedPreferences = itemView.context.getSharedPreferences("Login", MODE_PRIVATE)

        itemView.post_answer_card_answer_username.text =
            item.creator.firstname + " " + item.creator.lastname
        itemView.post_answer_card_answer_content.text =
            item.content
        itemView.post_answer_card_answer_date.text =
            getDateTime(item.date)
        itemView.post_answer_card_answer_likes.text =
            itemView.context.getString(R.string.nb_likes, item.nbLikes.toString())

        if ( item.liked ){
            itemView.post_answer_card_answer_like_icon.setBackgroundResource(R.drawable.ic_like_full)
        } else {
            itemView.post_answer_card_answer_like_icon.setBackgroundResource(R.drawable.ic_like_empty)
        }

        itemView.post_answer_card_answer_like_icon.setOnClickListener{
            manageLikeButtonClick(
                userConnectedPreferences, itemView, item
            )
        }
    }

    private fun bindUserfollow(item: DataModel.UserFollow) {
        itemView.userfollow_card_userfollow_username.text = item.username
        itemView.userfollow_card_userfollow_city.text = item.city

        itemView.setOnClickListener {
            val intent = Intent(itemView.context, ProfileActivity::class.java)
            intent.putExtra("idUser", item.id)
            itemView.context.startActivity(intent)
        }

        val userConnectedPreferences = itemView.context.getSharedPreferences("Login", MODE_PRIVATE)
        GlobalScope.launch {
            val getUserProfilImage: Call<ResponseModel.ImageResponse> =
                NetworkManager.getUserProfilImage(
                    item.id!!,
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
                    itemView.userfollow_card_user_avatar_image.setImageBitmap(imageToBitmap)
                }

            }
        }
    }

    private fun bindUserInGroup(item: DataModel.UserInGroup) {
        itemView.user_in_goup_card_username.text = item.username
        itemView.user_in_goup_card_city.text = item.city  ?: "secret place"
        val image = itemView.user_in_goup_card_user_avatar
//        Picasso.get().load(item.avatarUrl).into(image)
        itemView.setOnClickListener {
            val intent = Intent(itemView.context, ProfileActivity::class.java)
            intent.putExtra("idUser", item.id)
            itemView.context.startActivity(intent)
        }

        val userConnectedPreferences = itemView.context.getSharedPreferences("Login", MODE_PRIVATE)
        GlobalScope.launch {
            val getUserProfilImage: Call<ResponseModel.ImageResponse> =
                NetworkManager.getUserProfilImage(
                    item.id!!,
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
                    itemView.user_in_goup_card_user_avatar_image.setImageBitmap(imageToBitmap)
                }

            }
        }
    }

    private fun bindGroup(item: DataModel.Group) {
        itemView.group_card_group_name.text = item.name
        itemView.group_card_group_nbMembers.text =
            itemView.context.getString(R.string.nb_members, item.nbMembers.size.toString())
        itemView.setOnClickListener {
            val intent = Intent(itemView.context, GroupActivity::class.java)
            intent.putExtra("idGroup", item.id)
            itemView.context.startActivity(intent)
        }
    }

    private fun bindConversation(item: DataModel.Conversation) {
        itemView.user_chat_name.text = item.userName
        itemView.last_message_date.text = if (item.lastMessage == null) "" else getDateTime(item.lastMessage.sentDate)
        itemView.last_message.text =
            if (item.lastMessage == null) ""
            else (
                    if (item.lastMessage.idSender == item.friendId) item.lastMessage.content
                    else "you: " + item.lastMessage.content
            )
        itemView.setOnClickListener {
            val intent = Intent(itemView.context, DiscussionActivity::class.java)
            intent.putExtra("user", Gson().toJson(item))
            itemView.context.startActivity(intent)
        }

        val userConnectedPreferences = itemView.context.getSharedPreferences("Login", MODE_PRIVATE)
        GlobalScope.launch {
            val getUserProfilImage: Call<ResponseModel.ImageResponse> =
                NetworkManager.getUserProfilImage(
                    item.friendId!!,
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
                    itemView.user_chat_avatar_image.setImageBitmap(imageToBitmap)
                }

            }
        }
    }

    private fun bindMessageChatSender(item: DataModel.Message) {
        itemView.content_message_chat_card.text = item.content
        itemView.date_message_chat_card.text = item.sentDate.toString()
    }

    private fun bindMessageChatReceiver(item: DataModel.Message) {
        itemView.content_message_chat_card_left.text = item.content
        itemView.date_message_chat_card_left.text = item.sentDate.toString()
    }

    private fun bindEmailInAddMember(item: DataModel.EmailInAddMember){
        itemView.add_member_email.text = item.email
    }

    private fun bindProject(item: DataModel.Project){
        itemView.project_card_project_name.text = item.name
    }

    private fun bindPostUserResponse(item: DataModel.UserPost){
        itemView.user_post_card_username.text = item.firstname + " " + item.lastname
        itemView.setOnClickListener {
            val intent = Intent(itemView.context, ProfileActivity::class.java)
            intent.putExtra("idUser", item.id)
            itemView.context.startActivity(intent)
        }
        val userConnectedPreferences = itemView.context.getSharedPreferences("Login", MODE_PRIVATE)
        GlobalScope.launch {
            val getUserProfilImage: Call<ResponseModel.ImageResponse> =
                NetworkManager.getUserProfilImage(
                    item.id!!,
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
                    itemView.user_post_card_user_avatar_image.setImageBitmap(imageToBitmap)
                }

            }
        }
    }

    private fun bindSubscription(item: DataModel.Subscription){
        itemView.subscription_card_subscription_username.text = item.username
        itemView.subscription_card_subscription_city.text = item.city
        itemView.setOnClickListener {
            val intent = Intent(itemView.context, ProfileActivity::class.java)
            intent.putExtra("idUser", item.id)
            itemView.context.startActivity(intent)
        }

        val userConnectedPreferences = itemView.context.getSharedPreferences("Login", MODE_PRIVATE)
        GlobalScope.launch {
            val getUserProfilImage: Call<ResponseModel.ImageResponse> =
                NetworkManager.getUserProfilImage(
                    item.id!!,
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
                    itemView.subscription_card_user_avatar_image.setImageBitmap(imageToBitmap)
                }

            }
        }

    }


    fun bind(dataModel: DataModel, position: Int) {
        when (dataModel) {
            is DataModel.Post -> bindPost(dataModel)
            is DataModel.Answer -> bindAnswer(dataModel)
            is DataModel.PostAnswer -> bindPostAnswer(dataModel)
            is DataModel.UserFollow -> bindUserfollow(dataModel)
            is DataModel.Group -> bindGroup(dataModel)
            is DataModel.Conversation -> bindConversation(dataModel)
            is DataModel.Message -> messageTypeManager(dataModel)
            is DataModel.UserInGroup -> bindUserInGroup(dataModel)
            is DataModel.EmailInAddMember -> bindEmailInAddMember(dataModel)
            is DataModel.Project -> bindProject(dataModel)
            is DataModel.Subscription -> bindSubscription(dataModel)
            is DataModel.UserPost -> bindPostUserResponse(dataModel)
            else -> {}
        }
    }

    fun messageTypeManager(message: DataModel.Message) {
        if( message.user == message.idSender){
            return bindMessageChatSender(message)
        }
        return bindMessageChatReceiver(message)
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



    private fun manageLikeButtonClick(
        userConnectedPreferences: SharedPreferences,
        itemView: View,
        item : DataModel.PostAnswer ){
        if( !item.liked){
            likePost(
                userConnectedPreferences, itemView, item
            )
        } else {
            dislikePost(
                userConnectedPreferences, itemView, item
            )
        }
    }

    private fun likePost(
        userConnectedPreferences: SharedPreferences,
        itemView: View,
        item : DataModel.PostAnswer
    ){
        GlobalScope.launch(Dispatchers.Default) {
            val likeRequest = NetworkManager.likePost(
                userConnectedPreferences.getLong("idUser", -1),
                item.id,
                userConnectedPreferences!!.getString("accessToken", null)!!
            )
            val liked = likeRequest.execute()
            if (liked.isSuccessful) {
                withContext(Dispatchers.Main) {
                    item.nbLikes += 1
                    item.liked = true
                    itemView.post_answer_card_answer_like_icon.setBackgroundResource(R.drawable.ic_like_full)
                    itemView.post_answer_card_answer_likes.text =
                        itemView.context.getString(R.string.nb_likes, item.nbLikes.toString())
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        itemView.context,
                        "Unable to like, something went wrong",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun dislikePost(
        userConnectedPreferences: SharedPreferences,
        itemView: View,
        item : DataModel.PostAnswer
    ){
        GlobalScope.launch(Dispatchers.Default) {
            val dislikeRequest = NetworkManager.dislikePost(
                userConnectedPreferences.getLong("idUser", -1),
                item.id,
                userConnectedPreferences!!.getString("accessToken", null)!!
            )
            val disliked = dislikeRequest.execute()
            if (disliked.isSuccessful) {
                withContext(Dispatchers.Main) {
                    item.nbLikes -= 1
                    item.liked = false
                    itemView.post_answer_card_answer_like_icon.setBackgroundResource(R.drawable.ic_like_empty)
                    itemView.post_answer_card_answer_likes.text =
                        itemView.context.getString(R.string.nb_likes, item.nbLikes.toString())
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        itemView.context,
                        "Unable to dislike, something went wrong",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
