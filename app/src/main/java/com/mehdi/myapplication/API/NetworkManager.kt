package com.mehdi.myapplication.API

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.RequestModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

object NetworkManager {

    private val httpClient : OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    val authentificationApi: API =
        Retrofit.Builder()
        .baseUrl("http://social-code.fr/backend/api/v1/auth/login/")
        .addConverterFactory(
            GsonConverterFactory.create()
        )
        .addCallAdapterFactory(
            CoroutineCallAdapterFactory()
        )
        .build().create(API::class.java)

    val userInfoApi: API =
        Retrofit.Builder()
            .baseUrl("http://social-code.fr/backend/api/v1/auth/users/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .addCallAdapterFactory(
                CoroutineCallAdapterFactory()
            )
            .build().create(API::class.java)

    val groupApi: API =
        Retrofit.Builder()
            .baseUrl("http://social-code.fr/backend/api/v1/groups/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .addCallAdapterFactory(
                CoroutineCallAdapterFactory()
            )
            .build().create(API::class.java)

    val postApi: API =
        Retrofit.Builder()
            .baseUrl("http://social-code.fr/backend/api/v1/post/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .addCallAdapterFactory(
                CoroutineCallAdapterFactory()
            )
            .build().create(API::class.java)

    val userPostApi: API =
        Retrofit.Builder()
            .baseUrl("http://social-code.fr/backend/api/v1/post/user/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .addCallAdapterFactory(
                CoroutineCallAdapterFactory()
            )
            .build().create(API::class.java)

    val followApi: API =
        Retrofit.Builder()
            .baseUrl("http://social-code.fr/backend/api/v1/post/followLink/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .addCallAdapterFactory(
                CoroutineCallAdapterFactory()
            )
            .build().create(API::class.java)

    val chatApi: API =
        Retrofit.Builder()
            .baseUrl("http://social-code.fr/backend/api/v1/chat/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .addCallAdapterFactory(
                CoroutineCallAdapterFactory()
            )
            .build().create(API::class.java)

    val versioningApi: API =
        Retrofit.Builder()
            .baseUrl("http://social-code.fr/backend/api/v1/project/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .addCallAdapterFactory(
                CoroutineCallAdapterFactory()
            )
            .build().create(API::class.java)

    val fileApi: API =
        Retrofit.Builder()
            .baseUrl("http://social-code.fr/backend/api/v1/files/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .addCallAdapterFactory(
                CoroutineCallAdapterFactory()
            )
            .build().create(API::class.java)


    val api: API = Retrofit.Builder()
        .baseUrl("https://theaudiodb.com/api/v1/json/523532/")
        .addConverterFactory(
            GsonConverterFactory.create()
        )
        .addCallAdapterFactory(
            CoroutineCallAdapterFactory()
        )
        .build()
        .create(API::class.java)

    fun login(loginRequest: RequestModel.AuthRequest): Call<DataModel.UserAuthenticate> {
        return authentificationApi.authenticateAsync(loginRequest)
    }

    suspend fun getFriendsChat(email: String, authorization: String): MutableList<ResponseModel.FriendProfileResponse> {
        return chatApi.getFriendsChat(authorization,email).await()
    }

    fun getLastMessageByConversationId(cid: Long, authorization: String): Call<ResponseModel.MessageResponse>{
        return chatApi.getLastMessageByConversationId(authorization, cid)
    }

    suspend fun getAllMessage(email: String, interlocutorId: Long, authorization: String): MutableList<ResponseModel.MessageResponse> {
        return chatApi.getAllMessage(authorization,interlocutorId, email).await()
    }

    fun createPost(createPostRequest: RequestModel.CreatePostRequest, authorization: String): Call<ResponseModel.PostResponse> {
        return postApi.createPost(authorization, createPostRequest)
    }

    suspend fun getUserFollowersByIdUser(idUser: Long, authorization: String): MutableList<ResponseModel.FollowerResponse>{
        return followApi.getUserFollowersByIdUser(authorization, idUser ).await()
    }

    fun getUserSubscriptionsByIdUser(idUser: Long, authorization: String): Call<MutableList<ResponseModel.FollowerResponse>>{
        return followApi.getSubscriptionByIdUser(authorization, idUser )
    }

    fun getUserInfoByIdUser(idUser: Long, authorization: String): Call<ResponseModel.UserInfoResponse> {
        return userInfoApi.getUserInfoByIdUser(authorization, idUser)
    }

    fun getUserProfilImage(idUser: Long, authorization: String): Call<ResponseModel.ImageResponse> {
        return fileApi.getUserProfilImage(authorization, idUser)
    }

    suspend fun getUserAnswersByIdUser(idUser: Long, authorization: String): MutableList<ResponseModel.PostResponse>{
        return postApi.getUserAnswersByIdUser(authorization, idUser).await()
    }

    suspend fun getUserPostsByIdUser(idUser: Long, authorization: String): MutableList<ResponseModel.PostResponse>{
        return postApi.getUserPostsByIdUser(authorization, idUser).await()
    }

    suspend fun getPostByIdPost(idPost: Long, authorization: String): ResponseModel.PostResponse{
        return postApi.getPostByIdPost(authorization, idPost).await()
    }

    suspend fun getPostAnswersByIdPost(idPost: Long, authorization: String): MutableList<ResponseModel.PostResponse>{
        return postApi.getPostAnswersByIdPost(authorization, idPost).await()
    }

    fun createLinkAnswerToPost(request: RequestModel.CommentRequest, authorization: String): Call<ResponseModel.CommentResponse>{
        return postApi.createLinkBetweenPostAndAnswer(authorization, request)
    }

    suspend fun getProjectFromGroupByGroupId(groupId: Long, authorization: String) : MutableList<ResponseModel.ProjectResponse>{
        return versioningApi.getProjectFromGroupByGroupId(authorization, groupId).await()
    }

    fun createProject(request: RequestModel.CreateProjectRequest, authorization: String) : Call<ResponseModel.ProjectResponse>{
        return versioningApi.createProject(authorization, request)
    }

    fun getGroupById(idGroup: Long, authorization: String): Call<ResponseModel.GroupResponse> {
        return groupApi.getGroupByGroupId(authorization, idGroup)
    }

    fun createGroup(request: RequestModel.CreateGroupRequest, authorization: String) : Call<ResponseModel.GroupResponse>{
        return groupApi.createGroup(authorization, request)
    }

    fun addMemberToGroup(groupId: Long, request: RequestModel.GroupRequest, authorization: String) : Call<ResponseModel.GroupResponse>{
        return groupApi.addMemberToGroup( authorization, groupId, request)
    }

    fun getUserInfoByEmail(email: String, authorization: String): Call<ResponseModel.UserInfoResponse>{
        return userInfoApi.getUserByEmail(authorization, email)
    }

    suspend fun getGroupsByUserEmail(email: String, authorization: String) : MutableList<ResponseModel.GroupResponse>{
        return groupApi.getGroupsByUserEmail(authorization, email).await()
    }

    suspend fun getGroupMembersByGroupId(idGroup: Long, authorization: String) : MutableList<ResponseModel.UserInfoResponse>{
        return groupApi.getGroupMembersByGroupId(authorization, idGroup).await()
    }

    fun getPostLikedByUser(idUser: Long, authorization: String): Call<MutableList<ResponseModel.PostResponse>> {
        return postApi.getUserPostLikedByUserId(authorization, idUser)
    }

    fun likePost(idUser: Long, postId: Long, authorization: String):Call<Void>{
        return postApi.likePost(authorization, idUser, postId)
    }

    fun dislikePost(idUser: Long, postId: Long, authorization: String):Call<Void>{
        return postApi.dislikePost(authorization, idUser, postId)
    }

    fun deleteGroupById(groupId: Long, authorization: String): Call<Void>{
        return groupApi.deleteGroupById(authorization, groupId)
    }

    fun deleteMemberFromGroup(groupId: Long, groupRequest: RequestModel.GroupRequest, authorization: String): Call<Void>{
        return groupApi.deleteMemberFromGroup(authorization, groupId, groupRequest)
    }

    fun deleteProjectById(projectId: Long, authorization: String): Call<Void>{
        return versioningApi.deleteProjectById(authorization, projectId)
    }

    fun deletePostById(postId: Long, authorization: String): Call<Void>{
        return postApi.deletePostById(authorization, postId)
    }

    fun isFollowing(idUser1: Long, idUser2: Long, authorization: String): Call<Void>{
        return followApi.isFollowing(authorization, idUser1 , idUser2)
    }

    fun follow(idUser1: Long, idUser2: Long, authorization: String): Call<Void>{
        return followApi.follow(authorization, idUser1 , idUser2)
    }

    fun unfollow(idUser1: Long, idUser2: Long, authorization: String): Call<Void>{
        return followApi.unfollow(authorization, idUser1 , idUser2)
    }

    fun startConversation(userEmail: String, friendEmail: String, authorization: String):
            Call<ResponseModel.FriendProfileResponse>{
        return chatApi.startConversation(authorization, userEmail, friendEmail)
    }

    fun sendMessage(cid: Long, userEmail: String, content:String, authorization: String):
            Call<ResponseModel.MessageResponse>{
        return chatApi.sendMessage(authorization, cid, userEmail, content)
    }

    fun isExistDiscussion(userId: Long, friendId: Long, authorization: String):
            Call<ResponseModel.FriendProfileResponse>{
        return chatApi.isExistDiscussion(authorization, userId, friendId)
    }

    suspend fun getFeed(userId: Long, authorization: String):
            MutableList<ResponseModel.PostResponse>{
        return  postApi.getFeed(authorization, userId).await()
    }

    fun getAllPost(authorization: String):
            Call<MutableList<ResponseModel.PostResponse>>{
        return  postApi.getAllPost(authorization)
    }

    suspend fun searchPostWithFilter(authorization: String, filters: RequestModel.FilterRequest):
            MutableList<ResponseModel.PostResponse>{
        return  postApi.searchPostWithFilter(authorization,filters).await()
    }

    suspend fun searchUser(authorization: String, firstname: String):
            MutableList<ResponseModel.PostUserResponse>{
        return  userPostApi.searchUserByFirstname(authorization,firstname).await()
    }
//
//    suspend fun getTrendingSongs(): TrendingSongResponse {
//        return api.getTrendingSongsAsync("trending.php", "us", "itunes", "singles").await()
//    }
//
//    suspend fun getAlbumsOfArtist(artistName: String): AlbumResponse {
//        return api.getAlbumsOfArtistAsync("searchalbum.php", artistName).await()
//    }
//
//    suspend fun getArtistAsync(artistName: String): ArtistResponse {
//        return api.getArtistAsync("search.php", artistName).await()
//    }
//
//    suspend fun getAlbumsOfArtistByArtistId(artistId: Int): AlbumResponse {
//        return api.getAlbumsOfArtistByArtistIdAsync("album.php", artistId).await()
//    }
//
//    suspend fun getArtistTop10Songs(artistName: String): SongResponse {
//        return api.getArtistTop10SongsAsync("track-top10.php", artistName).await()
//    }
//
//    suspend fun getAlbumAsync(idAlbum: Int): AlbumResponse {
//        return api.getAlbumAsync("album.php", idAlbum).await()
//    }
//
//    suspend fun getSongsOfAlbum(idAlbum: Int): SongResponse {
//        return api.getSongsOfAlbumAsync("track.php", idAlbum).await()
//    }
//
//    suspend fun getLyricsOfSong(artistName: String, songTitle: String): LyricsResponse {
//        return lyricsApi.getLyricsAsync("$artistName/$songTitle").await()
//    }
}