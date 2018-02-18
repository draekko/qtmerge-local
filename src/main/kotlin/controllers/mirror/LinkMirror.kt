package controllers.mirror

import java.time.Instant
import java.time.ZonedDateTime

class LinkMirror(
        outputDirectory : String,
        link : String
) : Mirror(outputDirectory) {

    fun Mirror() {
        // TODO: use sha256 of URL to store links similar to 8ch?
    }
}
