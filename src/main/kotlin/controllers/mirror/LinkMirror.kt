package controllers.mirror

import models.events.Event
import java.time.Instant
import java.time.ZonedDateTime

class LinkMirror(
        outputDirectory : String,
        board : String,
        link : String
) : Mirror("Link", board, outputDirectory) {

    override fun Mirror() {
        // TODO: use sha256 of URL to store links similar to 8ch?
    }

    override fun MirrorReferences() {
        // TODO
    }

    override fun MirrorSearch(trips: List<String>, ids: List<String>, content: Regex?, referenceDepth: ReferenceDepth): List<Event> {
        // TODO
        return emptyList()
    }
}
