package com.vsoft.goodmankotlin.model

import java.io.Serializable

class GtPoints : Serializable {
    private lateinit var x: Array<String>
    private lateinit var y: Array<String>
    override fun toString(): String {
        return "ClassPojo [x = $x, y = $y]"
    }
}