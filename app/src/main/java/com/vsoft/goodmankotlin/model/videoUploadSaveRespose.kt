package com.vsoft.goodmankotlin.model

import com.google.gson.annotations.SerializedName
    data class videoUploadSaveRespose(@SerializedName("statusCode") val statusCode : Int,
                                @SerializedName("body") val body : String) {
    }
