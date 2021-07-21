package com.vsoft.goodmankotlin.utils

import android.content.Context
import android.hardware.Camera
import androidx.appcompat.app.AlertDialog
class CommonUtils {
    companion object {
        fun getFileName(path: String): String {
            var filename = ""
            val pathContents = path.split("[\\\\/]").toTypedArray()
            if (pathContents != null) {
                val pathContentsLength = pathContents.size
                //  System.out.println("Path Contents Length: " + pathContentsLength);
                for (i in pathContents.indices) {
                    //System.out.println("Path " + i + ": " + pathContents[i]);
                }
                //lastPart: s659629384_752969_4472.jpg
                val lastPart = pathContents[pathContentsLength - 1]
                val lastPartContents = lastPart.split("\\.").toTypedArray()
                if (lastPartContents != null && lastPartContents.size > 1) {
                    val lastPartContentLength = lastPartContents.size
                    //  System.out.println("Last Part Length: " + lastPartContentLength);
                    //filenames can contain . , so we assume everything before
                    //the last . is the name, everything after the last . is the
                    //extension
                    var name = ""
                    for (i in 0 until lastPartContentLength) {
                        //  System.out.println("Last Part " + i + ": "+ lastPartContents[i]);
                        if (i < lastPartContents.size - 1) {
                            name += lastPartContents[i]
                            if (i < lastPartContentLength - 2) {
                                name += "."
                            }
                        }
                    }
                    val extension = lastPartContents[lastPartContentLength - 1]
                    filename = "$name.$extension"
                    //System.out.println("Name: " + name);
                    //System.out.println("Extension: " + extension);
                    //System.out.println("Filename: " + filename);
                }
            }
            return filename
        }

        fun getOptimalPreviewSize(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
            val ASPECT_TOLERANCE = 0.1
            val targetRatio = h.toDouble() / w
            if (sizes == null) return null
            var optimalSize: Camera.Size? = null
            var minDiff = Double.MAX_VALUE
            for (size in sizes) {
                val ratio = size.width.toDouble() / size.height
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE
                for (size in sizes) {
                    if (Math.abs(size.height - h) < minDiff) {
                        optimalSize = size
                        minDiff = Math.abs(size.height - h).toDouble()
                    }
                }
            }
            return optimalSize
        }

        /**
         * Alert dialog to navigate to app settings
         * to enable necessary permissions
         */
        fun showPermissionsAlert(context: Context?) {
            val builder = AlertDialog.Builder(
                context!!
            )
            builder.setTitle("Permissions required!")
                .setMessage("Camera needs few permissions to work properly. Grant them in settings.")
                .setPositiveButton("GOTO SETTINGS") { dialog, which ->
                    CameraUtils.openSettings(
                        context
                    )
                }
                .setNegativeButton("CANCEL") { dialog, which -> }.show()
        }
    }
}