package controllers.mirror

import models.events.Event
import java.time.Instant
import java.time.ZonedDateTime

class LinkMirror(
        outputDirectory : String,
        link : String
) : Mirror(outputDirectory) {

    override fun Mirror() {
        // TODO: use sha256 of URL to store links similar to 8ch?
    }

    override fun MirrorReferences() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun MirrorSearch(trips: List<String>, content: Regex?, referenceDepth: ReferenceDepth): List<Event> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
