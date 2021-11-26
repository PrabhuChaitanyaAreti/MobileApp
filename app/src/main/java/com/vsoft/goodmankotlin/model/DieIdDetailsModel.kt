package com.vsoft.goodmankotlin.model

import com.google.gson.annotations.SerializedName
data class DieIdDetailsModel (

    @SerializedName("response"   ) var response   : List<DieIdResponse> = arrayListOf(),
    @SerializedName("statusCode" ) var statusCode : Int?           = null

)