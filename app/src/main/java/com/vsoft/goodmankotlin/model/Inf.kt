package com.vsoft.goodmankotlin.model

class Inf {
    private lateinit var shapes: Array<Shapes>
    var infImg: String? = null
    override fun toString(): String {
        return "ClassPojo [shapes = $shapes, inf_img = $infImg]"
    }
}