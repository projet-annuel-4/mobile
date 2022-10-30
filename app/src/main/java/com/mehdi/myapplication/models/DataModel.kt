package com.mehdi.myapplication.models

import kotlin.collections.ArrayList

sealed class DataModel {

    data class Post(
        val id: Long,
        val creator: UserInPost,
        val title: String,
        val date: String,
        val content: String,
        var nbLikes: Int,
        var nbAnswers: Int,
        val answers: ArrayList<Answer>,
        val tags: ArrayList<Tags>,
        val attachments: ArrayList<Attachment>,
        var liked: Boolean
    ) : DataModel()

    data class UserInPost(
        val id : Long,
        val firstname : String,
        val lastname : String,
        val avatarUrl: String?,
    ): DataModel()

    data class Project(
        val id : Long,
        val name : String,
        val creationDate : String,
        val visibility : Boolean,
        val groupCreator: Long,
    ): DataModel()

    data class Answer(
        val id: Long,
        val creator: UserInAnswer,
        val parentPost : Post?,
        val date: String,
        val content: String,
        val nbLikes: Int,
        val attachments: ArrayList<Attachment>,
    ) : DataModel()

    data class PostAnswer(
        val id: Long,
        val creator: UserInAnswer,
        val date: String,
        val content: String,
        var nbLikes: Int,
        val attachments: ArrayList<Attachment>,
        var liked: Boolean
    ) : DataModel()

    data class UserInAnswer(
        val id : Long,
        val firstname : String,
        val lastname : String,
        val avatarUrl: String,
    )

    data class Tags(
        val name: String
        ): DataModel()

    data class UserProfileInfo(
        val id: Long,
        val firstname: String?,
        val lastname: String?,
        val email : String?,
        val city : String?,
        val nbFollow: Int?,
        val nbPosts: Int?,
        val avatarUrl: String?,
    ) : DataModel()

    data class UserCardChat(
        val id: Long,
        val username: String,
        val email: String,
        val imgUrl: String?
    ) : DataModel()

    data class UserFollow(
        val id: Long,
        val username: String,
        val avatarUrl: String,
        val city: String?
    ) : DataModel()

    data class UserAuthenticate(
        val id: String,
        val email: String,
        val firstName: String,
        val lastName: String,
        val tokenType: String,
        val accessToken: String,
        val imageUrl: String,
        val userRole: String,
    ) : DataModel()

    data class Code(
        val id: Long,
        val language: String,
        val code: String,
        val runnable: Boolean
    ) : DataModel()

    data class Attachment(
        val id: Long,
        val url: String,
        val description: String
    ) : DataModel()

    data class Group(
        val id: Long,
        val name: String,
        val nbMembers: kotlin.collections.List<UserInGroup>,
        var nbProject: Long,
        val groupCreator: Long,
    ): DataModel()

    data class UserInGroup(
        val id: Long,
        val username: String,
        val avatarUrl: String?,
        val city: String?,
        val groupCreator: Long,
    ): DataModel()

    data class Message(
        val id: Long,
        val idSender : Long,
        val content: String,
        val sentDate: String,
        val user: Long
    ): DataModel()

    data class ChatCard(
        val idChat: Long,
        val idUser1: Long,
        val idUser2: Long,
        val lastMessage: Message
    ): DataModel()

    data class MemberCard(
        val email: String,
        val avatarUrl: String?
    ): DataModel()

    data class EmailInAddMember(
        val id: Long,
        val email: String
    ) : DataModel()

    data class Conversation(
        var id: Long?,
        val friendId: Long,
        val userName: String,
        val imgUrl: String?,
        val friendEmail: String,
        val lastMessage: Message?
    ): DataModel()

    data class Subscription(
        val id: Long,
        val username: String,
        val avatarUrl: String,
        val city: String?
    ): DataModel()

    data class UserPost(
        val id : Long?,
        val firstname : String?,
        val lastname : String?,
        val email : String?,
        val nbFollowers : Int?,
        val nbSubscriptions : Int?,
        val img: String?
    ): DataModel()
}