package com.vsoft.goodmankotlin.model

import java.io.Serializable

class Counts : Serializable {
    var cORRECT_PUNCHES: String? = null
    var uNDETECTED_HOLES: String? = null
    var uNDETECTED_DYES: String? = null
    var iNCORRECT_PUNCHES: String? = null
    var uNDETECTED_PUNCHES: String? = null
    var mISSING_PUNCHES: String? = null
    override fun toString(): String {
        return "ClassPojo [CORRECT_PUNCHES = " + cORRECT_PUNCHES + ", UNDETECTED_HOLES = " + uNDETECTED_HOLES + ", UNDETECTED_DYES = " + uNDETECTED_DYES + ", INCORRECT_PUNCHES = " + iNCORRECT_PUNCHES + ", UNDETECTED_PUNCHES = " + uNDETECTED_PUNCHES + ", MISSING_PUNCHES = " + mISSING_PUNCHES + "]"
    }
}