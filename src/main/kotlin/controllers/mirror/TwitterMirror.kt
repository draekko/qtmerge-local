package controllers.mirror

import models.events.Event
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

    override fun Mirror() {
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

    override fun MirrorReferences() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun MirrorSearch(trips: List<String>, content: Regex?, referenceDepth: ReferenceDepth): List<Event> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
