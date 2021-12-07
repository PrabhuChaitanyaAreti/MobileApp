@file:Suppress("ClassName")

package com.vsoft.goodmankotlin.model

import java.io.Serializable

class Gt_unique_dies : Serializable {
    private var base64_image_segment: String? = null
    private var image_segment_width: String? = null
    var correct: String? = null
    private var image_segment_height: String? = null
    private var ground_truth: String? = null
    private var prediction: String? = null
    private var label_id: String? = null
    override fun toString(): String {
        return "ClassPojo [base64_image_segment = $base64_image_segment, image_segment_width = $image_segment_width, correct = $correct, image_segment_height = $image_segment_height, ground_truth = $ground_truth, prediction = $prediction, label_id = $label_id]"
    }
}