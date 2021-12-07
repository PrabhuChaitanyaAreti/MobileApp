package com.vsoft.goodmankotlin.model

import java.io.Serializable

class UniqueResults : Serializable {
    private var base64ImageSegment: String? = null
    private var imageSegmentWidth: String? = null
    var correct: Boolean? = null
    private var imageSegmentHeight: String? = null
    private var groundTruth: String? = null
    private var prediction: String? = null
    private var labelId: String? = null
    override fun toString(): String {
        return "ClassPojo [base64_image_segment = $base64ImageSegment, image_segment_width = $imageSegmentWidth, correct = $correct, image_segment_height = $imageSegmentHeight, ground_truth = $groundTruth, prediction = $prediction, label_id = $labelId]"
    }
}