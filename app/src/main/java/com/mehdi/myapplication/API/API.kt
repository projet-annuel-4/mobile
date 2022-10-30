package com.mehdi.myapplication.API

import com.mehdi.myapplication.API.dataResponse.GroupsResponse
import com.mehdi.myapplication.API.dataResponse.UserResponse
import com.mehdi.myapplication.API.dataResponse.UsersResponse
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.RequestModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface API {

    @retrofit2.http.POST(".")
    fun authenticateAsync(
      @Body post: RequestModel.AuthRequest
    ) : Call<DataModel.UserAuthenticate>

    @retrofit2.http.GET(".")
    fun getFriendsChat(
        @Header(value = "authorization") authorization: String,
        @Query("user-email") email: String
    ) : Deferred<MutableList<ResponseModel.FriendProfileResponse>>

    @retrofit2.http.GET("{cid}/last-message")
    fun getLastMessageByConversationId(
        @Header(value = "authorization") authorization: String,
        @Path("cid") cid: Long
    ) : Call<ResponseModel.MessageResponse>

    @retrofit2.http.GET("{interlocutorId}/messages")
    fun getAllMessage(
        @Header(value = "authorization") authorization: String,
        @Path("interlocutorId") id : Long,
        @Query("user-email") email: String,
    ) : Deferred<MutableList<ResponseModel.MessageResponse>>

    @retrofit2.http.GET("userId/{userId}")
    fun getUserPostsByIdUser(
        @Header(value = "authorization") authorization: String,
        @Path("userId") idUser: Long,
    ) : Deferred<MutableList<ResponseModel.PostResponse>>

    @retrofit2.http.GET("user/{userId}/answers")
    fun getUserAnswersByIdUser(
        @Header(value = "authorization") authorization: String,
        @Path("userId") idUser: Long,
    ) : Deferred<MutableList<ResponseModel.PostResponse>>

    @retrofit2.http.GET("{userId}")
    fun getUserInfoByIdUser(
        @Header(value = "authorization") authorization: String,
        @Path("userId") idUser: Long,
    ) : Call<ResponseModel.UserInfoResponse>

    @retrofit2.http.GET("{postId}")
    fun getPostByIdPost(
        @Header(value = "authorization") authorization: String,
        @Path("postId") idPost: Long,
    ) : Deferred<ResponseModel.PostResponse>

    @retrofit2.http.GET("{postId}/answers")
    fun getPostAnswersByIdPost(
        @Header(value = "authorization") authorization: String,
        @Path("postId") idPost: Long,
    ) : Deferred<MutableList<ResponseModel.PostResponse>>

    @retrofit2.http.GET("{userId}/followers")
    fun getUserFollowersByIdUser(
        @Header(value = "authorization") authorization: String,
        @Path("userId") idUser: Long,
    ) : Deferred<MutableList<ResponseModel.FollowerResponse>>

    @retrofit2.http.GET("{userId}/subscriptions")
    fun getSubscriptionByIdUser(
        @Header(value = "authorization") authorization: String,
        @Path("userId") idUser: Long,
    ) : Call<MutableList<ResponseModel.FollowerResponse>>

    @retrofit2.http.POST(".")
    fun createPost(
        @Header(value = "authorization") authorization: String,
        @Body post : RequestModel.CreatePostRequest
    ) : Call<ResponseModel.PostResponse>

    @retrofit2.http.POST("answer")
    fun createLinkBetweenPostAndAnswer(
        @Header(value = "authorization") authorization: String,
        @Body post : RequestModel.CommentRequest
    ) : Call<ResponseModel.CommentResponse>

    // todo not good url
    @retrofit2.http.GET("{groupId}/getProjects")
    fun getProjectFromGroupByGroupId(
        @Header(value = "authorization") authorization: String,
        @Path("groupId") groupId: Long,
    ) : Deferred<MutableList<ResponseModel.ProjectResponse>>

//    @retrofit2.http.GET("{userId}/subscriptions")
//    fun getMessagesFromGroupByGroupId(
//        @Header(value = "authorization") authorization: String,
//        @Path("groupId") idUser: Long,
//    ) : Deferred<MutableList<ResponseModel.FollowerResponse>>

    @retrofit2.http.POST("createProject")
    fun createProject(
        @Header(value = "authorization") authorization: String,
        @Body post : RequestModel.CreateProjectRequest
    ) : Call<ResponseModel.ProjectResponse>

    @retrofit2.http.POST(".")
    fun createGroup(
        @Header(value = "authorization") authorization: String,
        @Body post : RequestModel.CreateGroupRequest
    ) : Call<ResponseModel.GroupResponse>

    @retrofit2.http.PATCH("{groupId}/add-members")
    fun addMemberToGroup(
        @Header(value = "authorization") authorization: String,
        @Path("groupId") groupId: Long,
        @Body post : RequestModel.GroupRequest
    ) : Call<ResponseModel.GroupResponse>

    @retrofit2.http.GET("mail/{email}")
    fun getUserByEmail(
        @Header(value = "authorization") authorization: String,
        @Path("email") email : String
    ) : Call<ResponseModel.UserInfoResponse>

    @retrofit2.http.GET("{groupId}")
    fun getGroupByGroupId(
        @Header(value = "authorization") authorization: String,
        @Path("groupId") idGroup: Long,
    ) : Call<ResponseModel.GroupResponse>

    @retrofit2.http.GET("member/{email}")
    fun getGroupsByUserEmail(
        @Header(value = "authorization") authorization: String,
        @Path("email") email: String,
    ) : Deferred<MutableList<ResponseModel.GroupResponse>>

    @retrofit2.http.GET("{idGroup}/members")
    fun getGroupMembersByGroupId(
        @Header(value = "authorization") authorization: String,
        @Path("idGroup") idGroup: Long,
    ) : Deferred<MutableList<ResponseModel.UserInfoResponse>>

    @retrofit2.http.GET("userId/{userId}/postLiked")
    fun getUserPostLikedByUserId(
        @Header(value = "authorization") authorization: String,
        @Path("userId") idUser: Long,
    ) : Call<MutableList<ResponseModel.PostResponse>>

    @retrofit2.http.POST("{postId}/like/userId/{userId}")
    fun likePost(
        @Header(value = "authorization") authorization: String,
        @Path("userId") idUser: Long,
        @Path("postId") postId: Long,
    ) : Call<Void>

    @retrofit2.http.POST("{postId}/dislike/userId/{userId}")
    fun dislikePost(
        @Header(value = "authorization") authorization: String,
        @Path("userId") idUser: Long,
        @Path("postId") postId: Long,
    ) : Call<Void>

    @retrofit2.http.DELETE("{groupId}")
    fun deleteGroupById(
        @Header(value = "authorization") authorization: String,
        @Path("groupId") groupId: Long,
    ) : Call<Void>

    @retrofit2.http.PATCH("{groupId}/delete-members")
    fun deleteMemberFromGroup(
        @Header(value = "authorization") authorization: String,
        @Path("groupId") groupId: Long,
        @Body groupRequest: RequestModel.GroupRequest,
    ) : Call<Void>

    @retrofit2.http.DELETE("deleteProject/{projectId}")
    fun deleteProjectById(
        @Header(value = "authorization") authorization: String,
        @Path("projectId") projectId: Long,
    ) : Call<Void>

    @retrofit2.http.DELETE("{postId}")
    fun deletePostById(
        @Header(value = "authorization") authorization: String,
        @Path("postId") postId: Long,
    ) : Call<Void>

    @retrofit2.http.GET("{idUser1}/follow/{idUser2}")
    fun isFollowing(
        @Header(value = "authorization") authorization: String,
        @Path("idUser1") idUser1: Long,
        @Path("idUser2") idUser2: Long,
    ) : Call<Void>

    @retrofit2.http.POST("{idUser1}/follow/{idUser2}")
    fun follow(
        @Header(value = "authorization") authorization: String,
        @Path("idUser1") idUser1: Long,
        @Path("idUser2") idUser2: Long,
    ) : Call<Void>

    @retrofit2.http.POST("{idUser1}/unfollow/{idUser2}")
    fun unfollow(
        @Header(value = "authorization") authorization: String,
        @Path("idUser1") idUser1: Long,
        @Path("idUser2") idUser2: Long,
    ) : Call<Void>

    @retrofit2.http.POST(".")
    fun startConversation(
        @Header(value = "authorization") authorization: String,
        @Query("user-email") userEmail: String,
        @Query("friend-email") friendEmail: String,
    ) : Call<ResponseModel.FriendProfileResponse>

    @retrofit2.http.POST("{cid}/messages/text")
    fun sendMessage(
        @Header(value = "authorization") authorization: String,
        @Path("cid") conversationId: Long,
        @Query("user-email") userEmail: String,
        @Query("content") friendEmail: String,
    ) : Call<ResponseModel.MessageResponse>

    @retrofit2.http.GET("conversation")
    fun isExistDiscussion(
        @Header(value = "authorization") authorization: String,
        @Query("userId") userId: Long,
        @Query("friendId") friendId: Long,
    ) : Call<ResponseModel.FriendProfileResponse>

    @retrofit2.http.GET("subscriptions/userId/{userId}")
    fun getFeed(
        @Header(value = "authorization") authorization: String,
        @Path("userId") userId: Long,
    ) : Deferred<MutableList<ResponseModel.PostResponse>>

    @retrofit2.http.GET(".")
    fun getAllPost(
        @Header(value = "authorization") authorization: String,
    ) : Call<MutableList<ResponseModel.PostResponse>>

    @retrofit2.http.GET("image/{userId}")
    fun getUserImage(
        @Header(value = "authorization") authorization: String,
        @Path("userId") userId: Long
    ) : Call<MutableList<ResponseModel.PostResponse>>

    @retrofit2.http.POST("getAllByFilters")
    fun searchPostWithFilter(
        @Header(value = "authorization") authorization: String,
        @Body filters: RequestModel.FilterRequest
    ) : Deferred<MutableList<ResponseModel.PostResponse>>

    @retrofit2.http.GET("firstname/{firstname}")
    fun searchUserByFirstname(
        @Header(value = "authorization") authorization: String,
        @Path("firstname") firstname: String
    ) : Deferred<MutableList<ResponseModel.PostUserResponse>>

    @retrofit2.http.GET("image/{userId}")
    fun getUserProfilImage(
        @Header(value = "authorization") authorization: String,
        @Path("userId") userId: Long
    ) : Call<ResponseModel.ImageResponse>

//    @retrofit2.http.GET()
//    fun getUserInfoAsync(
//        @Url url: String,
//        @Query("country") country: String,
//        @Query("type") type: String,
//        @Query("format") format: String,
//    ): Deferred<UserResponse>
//
//    @retrofit2.http.GET()
//    fun getUserGroupsAsync(
//        @Url url: String,
//        @Query("country") country: String,
//        @Query("type") type: String,
//        @Query("format") format: String
//    ): Deferred<GroupsResponse>
//
//    @retrofit2.http.GET()
//    fun getGroupMembersAsync(
//        @Url url: String,
//        @Query("s") artistName: String
//    ): Deferred<UsersResponse>
//
//    @retrofit2.http.GET()
//    fun getProjectByGroupAsync(
//        @Url url: String,
//        @Query("i") artistId: Int
//    ): Deferred<AlbumResponse>
//
//    @retrofit2.http.GET()
//    fun getArtistAsync(
//        @Url url: String,
//        @Query("s") artistName: String
//    ): Deferred<ArtistResponse>
//
//    @retrofit2.http.GET()
//    fun getArtistTop10SongsAsync(
//        @Url url: String,
//        @Query("s") artistName: String
//    ): Deferred<SongResponse>
//
//    @retrofit2.http.GET()
//    fun getAlbumAsync(
//        @Url url: String,
//        @Query("m") idAlbum: Int
//    ): Deferred<AlbumResponse>
//
//    @retrofit2.http.GET()
//    fun getSongsOfAlbumAsync(
//        @Url url: String,
//        @Query("m") idAlbum: Int
//    ): Deferred<SongResponse>
//
//    @Headers(
//        "x-rapidapi-host: mourits-lyrics.p.rapidapi.com",
//        "x-rapidapi-key: 3f2fac0f14msh1370b581afa048ep135a1djsna145e5512318"
//    )
//
//    @retrofit2.http.GET()
//    fun getLyricsAsync(
//        @Url url: String,
//    ): Deferred<LyricsResponse>
}