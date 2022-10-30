package com.mehdi.myapplication.API.dataResponse

import com.google.gson.annotations.SerializedName
import com.mehdi.myapplication.models.DataModel

data class GroupResponse (@SerializedName("post") val response: DataModel.Post)