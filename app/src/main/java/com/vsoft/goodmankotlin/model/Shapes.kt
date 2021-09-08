package com.vsoft.goodmankotlin.model

import java.io.Serializable

class Shapes : Serializable {
    var gtPoints: GtPoints? = null
    var labelId: String? = null
    override fun toString(): String {
        return "ClassPojo [gt_points = $gtPoints, label_id = $labelId]"
    }
}