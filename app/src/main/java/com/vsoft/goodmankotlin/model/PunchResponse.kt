package com.vsoft.goodmankotlin.model

import java.io.Serializable

class PunchResponse : Serializable {
    private lateinit var inf: Array<Inf>
    private lateinit var gtUniqueDies: Array<Gt_unique_dies>
    private var gt: Gt? = null
    lateinit var uniqueResults: Array<UniqueResults>
    fun getGt(): Gt? {
        return gt
    }

    override fun toString(): String {
        return "ClassPojo [inf = $inf, gt_unique_dies = $gtUniqueDies, gt = $gt, unique_results = $uniqueResults]"
    }
}