package com.vsoft.goodmankotlin.utils

class CommonUtils {
    companion object {
        fun getFileName(path: String): String? {
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
    }
}