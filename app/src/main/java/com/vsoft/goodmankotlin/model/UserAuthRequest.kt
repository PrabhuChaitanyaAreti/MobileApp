package com.vsoft.goodmankotlin.model

import com.google.gson.annotations.SerializedName

data class UserAuthRequest(@SerializedName("user_name") val user_name : String,
                           @SerializedName("password") val password : String ) {}
