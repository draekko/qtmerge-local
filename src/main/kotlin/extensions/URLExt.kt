package extensions

import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.net.URL


fun URL.readBytesDelayed(useAgent : Boolean = false) : ByteArray {
    // TODO: random delay 0-5s
    //Thread.sleep(200)

    val connection = openConnection()
    if(useAgent) {
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11")
    }
    connection.connect()

    // SO 2295221
    val stream: InputStream? = connection.getInputStream()
    val bytes = IOUtils.toByteArray(stream)
    stream?.close()

    // Minimum sleep between URL reads
    Thread.sleep(100)
    return bytes
}

