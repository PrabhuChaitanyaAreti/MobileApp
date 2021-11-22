package com.vsoft.goodmankotlin.model

data class OperatorList(
    val message: String,
    val operatorlist: List<String>,
    val success: Boolean,
    val timestamp: String
)