package utils

import java.io.File

// Based on SO 1155107
fun sanitizeFileName(name: String?): String {
    if (null == name) {
        return ""
    }

    return if (File.separator == "/") {
        name.replace("/+".toRegex(), "<2F>").trim()
    } else
        Regex("[\u0001-\u001f<>:\"/\\\\|?*\u007f]+").replace(name, { it.groupValues[0].map { "<${it.toInt()}>" }.toString() }).trim()
}
