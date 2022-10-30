package com.mehdi.myapplication.models

import java.time.LocalDateTime

sealed class RequestModel {

    data class AuthRequest(
        val email : String,
        val password : String
    ) : RequestModel()

    data class GetAllMessageRequest(
        val ids : MutableList<Long>
    ) : RequestModel()

    data class CreatePostRequest(
        val title: String,
        val content: String,
        val nbLike: Int,
        val userId: Long,
        val tagsName: List<String>,
        val attachmentUrl: String,
        val attachmentDescription: String
    ) : RequestModel()

    data class CommentRequest (
        val postId: Long,
        val answerId: Long,
        val userId: Long
    ) : RequestModel()

    data class CreateProjectRequest(
        val name: String,
        val visibility: Boolean,
        val groupId: Long,
    ) : RequestModel()

    data class CreateGroupRequest(
        val name: String,
        val creatorId: Long,
        val members: List<String>
    ) : RequestModel()

    data class GroupRequest(
        val id: Long,
        val name: String,
        val members: List<String>,
        val creatorId: Long
    ) : RequestModel()

    data class FilterRequest(
        val filters: MutableList<Filter>
    )

    data class Filter(
        val field: String,
        val operator: Long,
        val value: String,
        val values: MutableList<String>
    )
}