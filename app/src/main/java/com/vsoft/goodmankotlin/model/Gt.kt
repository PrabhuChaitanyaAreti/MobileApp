package com.vsoft.goodmankotlin.model

import java.io.Serializable

class Gt : Serializable {
    private var gtImageWidth: String? = null
    private var gtImageHeight: String? = null
    private var counts: Counts? = null
    private lateinit var shapes: Array<Shapes>
    private var gtImage: String? = null
    override fun toString(): String {
        return "ClassPojo [gt_image_width = $gtImageWidth, gt_image_height = $gtImageHeight, counts = $counts, shapes = $shapes, gt_image = $gtImage]"
    }
}