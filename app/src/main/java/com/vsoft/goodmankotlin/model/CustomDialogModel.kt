package com.vsoft.goodmankotlin.model

import com.google.gson.annotations.SerializedName

data class CustomDialogModel(

    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("image") val image: String?,
    @SerializedName("Buttons") val buttons: List<String>
){

}
