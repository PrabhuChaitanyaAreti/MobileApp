package com.vsoft.goodmankotlin.model

import com.google.gson.annotations.SerializedName

data class UserAuthResponse(@SerializedName("statusCode") val statusCode : Int,
                            @SerializedName("body") val body : String) {
}
