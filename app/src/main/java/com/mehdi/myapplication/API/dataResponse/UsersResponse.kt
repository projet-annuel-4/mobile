package com.mehdi.myapplication.API.dataResponse

import com.google.gson.annotations.SerializedName
import com.mehdi.myapplication.models.DataModel

data class UsersResponse (val response: ArrayList<DataModel.Post>)