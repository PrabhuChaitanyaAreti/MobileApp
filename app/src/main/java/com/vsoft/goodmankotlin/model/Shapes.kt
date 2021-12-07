package com.vsoft.goodmankotlin.model

import java.io.Serializable

class Shapes : Serializable {
    private var gtPoints: GtPoints? = null
    private var labelId: String? = null
    override fun toString(): String {
        return "ClassPojo [gt_points = $gtPoints, label_id = $labelId]"
    }
}