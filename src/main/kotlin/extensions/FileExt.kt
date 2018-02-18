package extensions

import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

fun File.iterate(bytes : ByteArray) : Boolean {
    val temp = File.createTempFile("qtmerge", ".tmp")
    temp.writeBytes(bytes)

    if(!exists()) {
        FileUtils.moveFile(temp, this)
        return true
    } else {
        if (!FileUtils.contentEquals(this, temp)) {
            val original = File(this.absolutePath)
            if (renameTo(File(absolutePath + "-" + lastModified()))) {
                FileUtils.moveFile(temp, original)
                return true
            } else {
                throw IOException("Unable to rename file from $absolutePath to $absolutePath-${lastModified()}")
            }
        } else {
            temp.delete()
        }
    }

    return false
}