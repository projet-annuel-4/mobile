package com.mehdi.myapplication.adapter;

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.R
import com.mehdi.myapplication.holder.DataAdapterViewHolder
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.RequestModel
import kotlinx.android.synthetic.main.post_card.view.*
import kotlinx.android.synthetic.main.user_in_group_card.view.*
import kotlinx.android.synthetic.main.user_in_group_card.view.delete_action
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerAdapter : RecyclerView.Adapter<DataAdapterViewHolder>(){

    private val adapterData = mutableListOf<DataModel>()
    private var groupId: Long? = null
    private lateinit var context : Context
    private lateinit var userConnectedPreferences: SharedPreferences
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapterViewHolder {

        context = parent.context
        userConnectedPreferences = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        val layout = when (viewType) {
            TYPE_POST_CARD -> R.layout.post_card
            TYPE_ANSWER_CARD -> R.layout.answer_card
            TYPE_USERFOLLOW_CARD -> R.layout.userfollow_card
            TYPE_GROUP_CARD -> R.layout.group_card
            TYPE_USER_CHAT_CARD -> R.layout.user_chat_card
            TYPE_MESSAGE_CHAT_LEFT -> R.layout.message_chat_card_left
            TYPE_MESSAGE_CHAT_RIGHT -> R.layout.message_chat_card_right
            TYPE_POST_ANSWER_CARD -> R.layout.post_answer_card
            TYPE_PROJECT_CARD -> R.layout.project_card
            TYPE_USER_IN_GROUP_CARD -> R.layout.user_in_group_card
            TYPE_EMAIL_IN_ADD_MEMBER -> R.layout.add_member_in_group
            TYPE_SUBSCRIPTION_CARD -> R.layout.subscription_card
            TYPE_USER_POST_CARD -> R.layout.user_post_card
//            TYPE_FAVORITE_SONG -> R.layout.search_artist
            else -> throw IllegalArgumentException("Invalid view type")
        }
//
       val view: View = LayoutInflater.from(parent.context).inflate(layout, parent, false)
//
       return DataAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataAdapterViewHolder, position: Int) {
        holder.bind(adapterData[position], position)
        val data = adapterData[position]
        if (data !is DataModel.Message && data !is DataModel.Conversation && data !is DataModel.UserFollow &&
                data !is DataModel.UserPost ) {
            manageDisableDeleteButton(holder, adapterData[position])
            holder.itemView.delete_action.setOnClickListener {
                deleteManager(adapterData[position], position)
            }
        }
    }

    private fun manageDisableDeleteButton(holder: DataAdapterViewHolder, dataModel: DataModel){
        when (dataModel) {
            is DataModel.Post -> {
                if ( userConnectedPreferences.getLong("idUser", -1) != dataModel.creator.id){
                    holder.itemView.delete_action.visibility = View.GONE
                }
            }
            is DataModel.Answer -> {
                if ( userConnectedPreferences.getLong("idUser", -1) != dataModel.creator.id){
                    holder.itemView.delete_action.visibility = View.GONE
                }
            }
            is DataModel.PostAnswer -> {
                if ( userConnectedPreferences.getLong("idUser", -1) != dataModel.creator.id){
                    holder.itemView.delete_action.visibility = View.GONE
                }
            }
            is DataModel.Group -> {
                if ( userConnectedPreferences.getLong("idUser", -1) != dataModel.groupCreator){
                    holder.itemView.delete_action.visibility = View.GONE
                }
            }
            is DataModel.UserInGroup -> {
                if ( userConnectedPreferences.getLong("idUser", -1) != dataModel.groupCreator){
                    holder.itemView.delete_action.visibility = View.GONE
                }
            }
            is DataModel.Project -> {
                if ( userConnectedPreferences.getLong("idUser", -1) != dataModel.groupCreator){
                    holder.itemView.delete_action.visibility = View.GONE
                }
            }
            else -> {}
        }
    }

    private fun deleteManager(dataModel: DataModel, position: Int){
        when (dataModel) {
            is DataModel.Post -> bindPostDelete(dataModel, position)
            is DataModel.Answer -> bindAnswerDelete(dataModel, position)
            is DataModel.PostAnswer -> bindPostAnswerDelete(dataModel, position)
            is DataModel.UserFollow -> bindUserfollowDelete(dataModel, position)
            is DataModel.Group -> bindGroupDelete(dataModel, position)
            is DataModel.UserInGroup -> bindUserInGroupDelete(dataModel, position)
            is DataModel.EmailInAddMember -> bindEmailInAddMemberDelete(dataModel, position)
            is DataModel.Subscription -> bindSubscriptionDelete(dataModel, position)
            is DataModel.Project -> bindProjectDelete(dataModel, position)
            else -> {}
        }
    }

    private fun bindProjectDelete(dataModel: DataModel, position: Int){
        NetworkManager.deleteProjectById(
            (dataModel as DataModel.Project).id,
            userConnectedPreferences.getString("accessToken", null)!! )
            .enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Something went wrong ", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    if(response.code() == 200){
                        adapterData.removeAt(position)
                        notifyDataSetChanged()
                    }
                }
            }
            )
    }

    private fun bindSubscriptionDelete(dataModel: DataModel, position: Int){
        NetworkManager.unfollow(
            userConnectedPreferences.getLong("idUser", -1)!!,
            (dataModel as DataModel.Subscription).id,
            userConnectedPreferences.getString("accessToken", null)!! )
            .enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Something went wrong ", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    if(response.code() == 202){
                        adapterData.removeAt(position)
                        notifyDataSetChanged()
                    }
                }
            }
            )
    }

    private fun bindPostDelete(dataModel: DataModel, position: Int){
        NetworkManager.deletePostById((dataModel as DataModel.Post).id, userConnectedPreferences.getString("accessToken", null)!! )
            .enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    if(response.code() != 200){
                        Toast.makeText(context, "Email or password incorrect", Toast.LENGTH_LONG).show()
                    } else {
                        adapterData.removeAt(position)
                        notifyDataSetChanged()
                    }
                }
            }
            )
    }

    private fun bindEmailInAddMemberDelete(dataModel: DataModel, position: Int){
//        NetworkManager.deletePostById((dataModel as DataModel.Post).id, userConnectedPreferences.getString("accessToken", null)!! )
//            .enqueue(object: Callback<Void> {
//                override fun onFailure(call: Call<Void>, t: Throwable) {
//                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
//                }
//                override fun onResponse(
//                    call: Call<Void>,
//                    response: Response<Void>
//                ) {
//                    if(response.code() != 200){
//                        Toast.makeText(context, "Email or password incorrect", Toast.LENGTH_LONG).show()
//                    } else {
//                        adapterData.removeAt(position)
//                        notifyDataSetChanged()
//                    }
//                }
//            }
//            )
        adapterData.removeAt(position)
        notifyDataSetChanged()
    }

    private fun bindAnswerDelete(dataModel: DataModel.Answer, position: Int){
        NetworkManager.deletePostById((dataModel as DataModel.Answer).id, userConnectedPreferences.getString("accessToken", null)!! )
            .enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    if(response.code() != 200){
                        Toast.makeText(context, "Answer has been deleted", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Answer has been deleted", Toast.LENGTH_LONG).show()
                        adapterData.removeAt(position)
                        notifyDataSetChanged()
                    }
                }
            }
            )
        adapterData.removeAt(position)
        notifyDataSetChanged()
    }

    private fun bindPostAnswerDelete(dataModel: DataModel, position: Int){
        NetworkManager.deletePostById((dataModel as DataModel.PostAnswer).id, userConnectedPreferences.getString("accessToken", null)!! )
            .enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    if(response.code() != 200){

                    } else {
                        Toast.makeText(context, "Answer has been deleted", Toast.LENGTH_LONG).show()
                        adapterData.removeAt(position)
                        notifyDataSetChanged()
                    }
                }
            }
            )
    }

    private fun bindUserfollowDelete(dataModel: DataModel, position: Int){
//        NetworkManager.deletePostById((dataModel as DataModel.UserFollow).id, userConnectedPreferences.getString("accessToken", null)!! )
//            .enqueue(object: Callback<Void> {
//                override fun onFailure(call: Call<Void>, t: Throwable) {
//                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
//                }
//                override fun onResponse(
//                    call: Call<Void>,
//                    response: Response<Void>
//                ) {
//                    if(response.code() != 200){
//                        Toast.makeText(context, "Answer has been deleted", Toast.LENGTH_LONG).show()
//                    } else {
//                        adapterData.removeAt(position)
//                        notifyDataSetChanged()
//                    }
//                }
//            }
//            )
    }

    private fun bindGroupDelete(dataModel: DataModel, position: Int){
        NetworkManager.deleteGroupById((dataModel as DataModel.Group).id, userConnectedPreferences.getString("accessToken", null)!! )
            .enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    if(response.code() == 500){
                        Toast.makeText(context, "Group has been deleted", Toast.LENGTH_LONG).show()
                        adapterData.removeAt(position)
                        notifyDataSetChanged()
                    }
                }
            }
            )
    }

    private fun bindUserInGroupDelete(dataModel: DataModel, position: Int){
        NetworkManager.deleteMemberFromGroup(groupId!!, getAddMemberRequest((dataModel as DataModel.UserInGroup).id!!, dataModel.groupCreator), userConnectedPreferences.getString("accessToken", null)!! )
            .enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    if(response.code() != 200){
                        Toast.makeText(context, "Answer has been deleted", Toast.LENGTH_LONG).show()
                    } else {
                        adapterData.removeAt(position)
                        notifyDataSetChanged()
                    }
                }
            }
            )
    }

    private fun getAddMemberRequest(memberToAddId: Long, id: Long): RequestModel.GroupRequest {
        val memberToAdd : List<String> = listOf(memberToAddId.toString())
        return RequestModel.GroupRequest(
            groupId!!,
            "",
            memberToAdd,
            id
        )
    }

    override fun getItemViewType(position: Int): Int {
        return when (adapterData[position]) {
            is DataModel.Post -> TYPE_POST_CARD
            is DataModel.Answer -> TYPE_ANSWER_CARD
            is DataModel.PostAnswer -> TYPE_POST_ANSWER_CARD
            is DataModel.UserFollow -> TYPE_USERFOLLOW_CARD
            is DataModel.Subscription -> TYPE_SUBSCRIPTION_CARD
            is DataModel.Group -> TYPE_GROUP_CARD
            is DataModel.Conversation -> TYPE_USER_CHAT_CARD
            is DataModel.Message -> messageTypeManager(adapterData[position] as DataModel.Message)
            is DataModel.Project -> TYPE_PROJECT_CARD
            is DataModel.UserInGroup -> TYPE_USER_IN_GROUP_CARD
            is DataModel.EmailInAddMember -> TYPE_EMAIL_IN_ADD_MEMBER
            is DataModel.UserPost -> TYPE_USER_POST_CARD
            else -> throw IllegalArgumentException("unhandled object")
        }
    }

    override fun getItemCount(): Int {
        return adapterData.size
    }

    companion object {
        private const val TYPE_POST_CARD = 0
        private const val TYPE_ANSWER_CARD = 1
        private const val TYPE_USERFOLLOW_CARD = 2
        private const val TYPE_GROUP_CARD = 3
        private const val TYPE_USER_CHAT_CARD = 4
        private const val TYPE_MESSAGE_CHAT_LEFT = 5
        private const val TYPE_POST_ANSWER_CARD = 6
        private const val TYPE_PROJECT_CARD = 7
        private const val TYPE_USER_IN_GROUP_CARD = 8
        private const val TYPE_MESSAGE_CHAT_RIGHT = 9
        private const val TYPE_EMAIL_IN_ADD_MEMBER = 10
        private const val TYPE_SUBSCRIPTION_CARD = 11
        private const val TYPE_USER_POST_CARD = 12
    }

    fun setGroupId(id: Long){
        groupId = id
    }

    fun setData(data: List<DataModel>?) {
        adapterData.apply {
            clear()
            if (data != null) {
                addAll(data)
            }
        }
        notifyDataSetChanged()
    }

    fun getData(): MutableList<DataModel> {
        return adapterData
    }

    fun messageTypeManager(message: DataModel.Message): Int {
        print(message)

        if( message.user == message.idSender){
            return TYPE_MESSAGE_CHAT_RIGHT
        }
        return TYPE_MESSAGE_CHAT_LEFT
    }
}
