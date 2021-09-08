package com.vsoft.goodmankotlin.model

import java.io.Serializable

class Gt : Serializable {
    var gtImageWidth: String? = null
    var gtImageHeight: String? = null
    private var counts: Counts? = null
    lateinit var shapes: Array<Shapes>
    var gtImage: String? = null
    override fun toString(): String {
        return "ClassPojo [gt_image_width = $gtImageWidth, gt_image_height = $gtImageHeight, counts = $counts, shapes = $shapes, gt_image = $gtImage]"
    }
}