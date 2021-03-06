package controllers.mirror

import models.events.Event
import settings.Settings.Companion.DATADIR
import settings.Settings.Companion.ZONEID
import java.io.File
import java.time.Instant
import java.time.ZonedDateTime

class TwitterMirror(
        board : String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZONEID)
) : Mirror(board, Source.Twitter, "draekko") {
    val baseURL = "https://twitter.com"
    val mirrorRoot = DATADIR + File.separator + dataset + File.separator + "twitter"

    override fun Mirror() {
        println(">> mirror: $this")
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
        println(">> mirror refs: $this")
        // TODO
    }

    override fun MirrorSearch(params: SearchParameters): List<Event> {
        println(">> search: $this")
        // TODO
        return emptyList()
    }
}
