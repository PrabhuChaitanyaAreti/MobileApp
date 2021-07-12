package com.vsoft.goodmankotlin.model

class Inf {
    lateinit var shapes: Array<Shapes>
    var inf_img: String? = null
    override fun toString(): String {
        return "ClassPojo [shapes = $shapes, inf_img = $inf_img]"
    }
}