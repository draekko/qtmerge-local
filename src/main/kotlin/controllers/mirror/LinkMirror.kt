package controllers.mirror

import models.events.Event

class LinkMirror(
        board : String,
        val link : String
) : Mirror(board, Source.LinkedData, "draekko") {

    override fun Mirror() {
        println(">> mirror: $this")
        // TODO: use sha256 of URL to store links similar to 8ch?
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
