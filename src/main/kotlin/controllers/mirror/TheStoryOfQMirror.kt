package controllers.mirror

import com.google.gson.Gson
import extensions.iterate
import extensions.readBytesDelayed
import models.events.Event
import models.events.PostEvent
import models.importer.QCodeFagPost
import models.mirror.InfChThread
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

class TheStoryOfQMirror(
        outputDirectory : String,
        board : String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, QTMirror.ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), QTMirror.ZONEID)
) : Mirror("thestoryofq", board, outputDirectory) {
    var threads: MutableList<InfChThread> = arrayListOf()
    val updatedThreads: MutableList<InfChThread> = arrayListOf()
    var mirrorRoot = outputDirectory + File.separator + "thestoryofq"
    var boardRoot = mirrorRoot + File.separator + "boards" + File.separator + board
    var filesRoot = mirrorRoot + File.separator + "files"

    override fun Mirror() {
        updatedThreads.clear()
        if (MakeDirectory(mirrorRoot)) {
            if (!MakeDirectory(boardRoot)) {
                return
            }
            if (!MakeDirectory(filesRoot)) {
                return
            }

            val catalogURL = URL("http://www.thestoryofq.com/data/$board.json")
            val catalogFile = File(boardRoot + File.separator + "$board.json")

            // Update catalog json if necessary
            try {
                if (catalogFile.iterate(catalogURL.readBytesDelayed())) {
                    println("  Updated catalog for $board")
                }
            } catch (e: FileNotFoundException) {
                println("Unable to find catalog for $board: $e")
                return
            }
        }
    }

    override fun MirrorReferences() {
        // TODO
    }

    override fun MirrorSearch(trips: List<String>, ids: List<String>, content: Regex?, referenceDepth: ReferenceDepth): List<Event> {
        val eventList: MutableList<Event> = arrayListOf()
        val mirrorRoot = outputDirectory + File.separator + "thestoryofq"
        val boardRoot = mirrorRoot + File.separator + "boards" + File.separator + board
        val catalogFile = File(boardRoot + File.separator + "$board.json")

        println(">> board: $board")
        val posts = Gson().fromJson(catalogFile.readText(), Array<QCodeFagPost>::class.java)
        posts.forEachIndexed { index, post ->
            val postEvent = PostEvent.fromQCodeFagPost("thestoryofq", board, post)
            var include = false

            // Search on trip
            if(trips.isNotEmpty() && trips.contains(post.trip)) {
                include = true
            }

            // Search on content
            if(!include) {
                if (content != null) {
                    val results = content.find(postEvent.Text())
                    if (results != null) {
                        include = true
                    }
                }
            }

            if(include) {
                eventList.add(postEvent)
            }
        }

        return eventList
    }
}

