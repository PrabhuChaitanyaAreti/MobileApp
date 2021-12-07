package com.vsoft.goodmankotlin.model

import com.google.gson.annotations.SerializedName

class DieIdResponse (
    @SerializedName("_id"     ) var Id     : Id?          = Id(),
    @SerializedName("die_id"  ) var dieId  : String?      = null,
    @SerializedName("part_id" ) var partId : List<String> = arrayListOf()
)