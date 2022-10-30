package com.mehdi.myapplication.models

import java.time.LocalDateTime
import java.util.*
// @SerializedName("intYearReleased") a placer avant la val
sealed class ResponseModel {

    data class FriendProfileResponse(
        val id : String,
        val friendId : String,
        val email : String,
        val firstName : String,
        val lastName : String,
        val imgUrl : String,
        val blockedBy : String,
    )

    data class UserInfoResponse(
        val id: Long ,
        val email: String,
        val firstName: String,
        val lastName: String,
        val city: String?,
        val imgUrl: String?,
    ) : ResponseModel()

    data class MessageResponse(
        val id: Long,
        val senderId: Long,
        val conversationId: Long,
        val createdAt: String,
        val content: String,
    ) : ResponseModel()

    data class PostResponse(
        val id: Long,
        val title: String,
        val content: String,
        val nbLike: Int,
        var nbAnswer: Int,
        val creationDate: String,
        val updateDate: String,
        val tags: kotlin.collections.List<TagResponse>?,
        val user: PostUserResponse
    ): ResponseModel()

    data class PostUserResponse(
        val id : Long?,
        val firstname : String?,
        val lastname : String?,
        val email : String?,
        val nbFollowers : Int?,
        val nbSubscriptions : Int?
    ): ResponseModel()

    data class TagResponse(
        val postId: Long,
        val name: String
    ): ResponseModel()

    data class FollowerResponse(
        val id : Long,
        val firstname : String,
        val lastname : String,
        val email : String,
        val nbFollowers : Int,
        val nbSubscriptions : Int
    ): ResponseModel()

    data class CommentResponse (
        val postId: Long,
        val answerId: Long,
        val userId: Long
    ): ResponseModel()

    data class ProjectResponse(
        val id : Long,
        val name : String,
        val creationDate : String,
        val visibility : Boolean
    ): ResponseModel()

    data class GroupResponse(
        val id: Long,
        val name: String,
        val members: kotlin.collections.List<UserInfoResponse>,
        val creatorId: Long
    ): ResponseModel()

    data class ConversationResponse(
          val id: Long,
          val user1: Long,
          val user2: Long,
          val blockedBy: Long,
          val createdAt: String,
          val updatedAt: String
    ): ResponseModel()

    data class ImageResponse(
        val id: Long,
        val file: String,
        val title: String,
        val link: String,
        val description: String,
        val details: String,
    )
 }