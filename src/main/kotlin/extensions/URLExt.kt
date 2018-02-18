package extensions

import java.io.FileNotFoundException
import java.net.URL

fun URL.readBytesDelayed() : ByteArray {
    val bytes : ByteArray
    // TODO: random delay 0-5s
    //Thread.sleep(200)
    try {
        bytes = readBytes()
    } catch(e : FileNotFoundException) {
        throw(e)
    }
    // Minimum sleep between URL reads
    Thread.sleep(200)
    return bytes
}
