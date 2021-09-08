package com.vsoft.goodmankotlin.model

import java.io.Serializable

class Counts : Serializable {
    private var correctPunches: String? = null
    private var undetectedHoles: String? = null
    private var undetectedDyes: String? = null
    private var incorrectPunches: String? = null
    private var undetectedPunches: String? = null
    private var missingPunches: String? = null
    override fun toString(): String {
        return "ClassPojo [CORRECT_PUNCHES = $correctPunches, UNDETECTED_HOLES = $undetectedHoles, UNDETECTED_DYES = $undetectedDyes, INCORRECT_PUNCHES = $incorrectPunches, UNDETECTED_PUNCHES = $undetectedPunches, MISSING_PUNCHES = $missingPunches]"
    }
}