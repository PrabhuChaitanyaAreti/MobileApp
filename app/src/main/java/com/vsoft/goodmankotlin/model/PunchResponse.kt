package com.vsoft.goodmankotlin.model

import java.io.Serializable

class PunchResponse : Serializable {
    lateinit var inf: Array<Inf>
    lateinit var gt_unique_dies: Array<Gt_unique_dies>
    private var gt: Gt? = null
    lateinit var unique_results: Array<Unique_results>
    fun getGt(): Gt? {
        return gt
    }

    fun setGt(gt: Gt?) {
        this.gt = gt
    }

    override fun toString(): String {
        return "ClassPojo [inf = $inf, gt_unique_dies = $gt_unique_dies, gt = $gt, unique_results = $unique_results]"
    }
}