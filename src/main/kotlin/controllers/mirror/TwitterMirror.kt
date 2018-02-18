package controllers.mirror

import java.io.File
import java.time.Instant
import java.time.ZonedDateTime

class TwitterMirror(
        outputDirectory : String,
        val board : String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, QTMirror.ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), QTMirror.ZONEID)
) : Mirror(outputDirectory) {
    val baseURL = "https://twitter.com"

    fun Mirror() {
        val mirrorRoot = outputDirectory + File.separator + "twitter"
        if (MakeDirectory(mirrorRoot)) {
            val boardRoot = mirrorRoot + File.separator + "boards" + File.separator + board
            val filesRoot = mirrorRoot + File.separator + "files"

            if (!MakeDirectory(boardRoot)) {
                return
            }
            if (!MakeDirectory(filesRoot)) {
                return
            }
        }
    }
}
