package com.vsoft.goodmankotlin.model

import java.io.Serializable

class Gt_points : Serializable {
    lateinit var x: Array<String>
    lateinit var y: Array<String>
    override fun toString(): String {
        return "ClassPojo [x = $x, y = $y]"
    }
}