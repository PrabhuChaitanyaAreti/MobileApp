package com.vsoft.goodmankotlin.model

import com.google.gson.annotations.SerializedName
    data class VideoUploadSaveResponse(@SerializedName("statusCode") val statusCode : Int,
                                       @SerializedName("body") val body : String)
