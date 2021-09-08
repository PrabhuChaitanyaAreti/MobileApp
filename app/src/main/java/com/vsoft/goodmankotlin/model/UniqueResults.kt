package com.vsoft.goodmankotlin.model

import java.io.Serializable

class UniqueResults : Serializable {
    var base64ImageSegment: String? = null
    var imageSegmentWidth: String? = null
    var correct: Boolean? = null
    var imageSegmentHeight: String? = null
    var groundTruth: String? = null
    var prediction: String? = null
    var labelId: String? = null
    override fun toString(): String {
        return "ClassPojo [base64_image_segment = $base64ImageSegment, image_segment_width = $imageSegmentWidth, correct = $correct, image_segment_height = $imageSegmentHeight, ground_truth = $groundTruth, prediction = $prediction, label_id = $labelId]"
    }
}