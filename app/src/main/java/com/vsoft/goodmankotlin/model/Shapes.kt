package com.vsoft.goodmankotlin.model

import java.io.Serializable

class Shapes : Serializable {
    var gt_points: Gt_points? = null
    var label_id: String? = null
    override fun toString(): String {
        return "ClassPojo [gt_points = $gt_points, label_id = $label_id]"
    }
}