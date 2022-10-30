package com.mehdi.myapplication.API.dataResponse

import com.mehdi.myapplication.models.ResponseModel

data class ProfileFollowersResponse (val friends : MutableList<ResponseModel.FollowerResponse>)