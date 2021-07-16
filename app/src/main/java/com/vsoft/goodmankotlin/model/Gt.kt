package com.vsoft.goodmankotlin.model

import java.io.Serializable

class Gt : Serializable {
    var gt_image_width: String? = null
    var gt_image_height: String? = null
    var counts: Counts? = null
    lateinit var shapes: Array<Shapes>
    var gt_image: String? = null
    override fun toString(): String {
        return "ClassPojo [gt_image_width = $gt_image_width, gt_image_height = $gt_image_height, counts = $counts, shapes = $shapes, gt_image = $gt_image]"
    }
}