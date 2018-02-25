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

class QCodeFagMirror(
        outputDirectory : String,
        board : String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, QTMirror.ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), QTMirror.ZONEID)
) : Mirror("qcodefag", board, outputDirectory) {
    var threads: MutableList<InfChThread> = mutableListOf()
    val updatedThreads: MutableList<InfChThread> = mutableListOf()
    var mirrorRoot = outputDirectory + File.separator + "qcodefag"
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

            //val catalogURL = URL("http://qcodefag.github.io/data/$board.json")
            //val catalogURL = URL("http://qanonposts.com/data/$board.json")
            val catalogURL = URL("https://github.com/QCodefag/QCodefag.github.io/raw/master/data/$board.json")
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

    override fun MirrorSearch(params: SearchParameters): List<Event> {
        val eventList: MutableList<Event> = arrayListOf()
        val mirrorRoot = outputDirectory + File.separator + "qcodefag"
        val boardRoot = mirrorRoot + File.separator + "boards" + File.separator + board
        val catalogFile = File(boardRoot + File.separator + "$board.json")

        // Set trips/ids if onlyQT flag set
        if(params.onlyQT) {
            params.trips.clear()
            params.trips.addAll(InfChMirror.QTRIPS)

            params.ids.clear()
            if(InfChMirror.EXCEPTIONS.containsKey(board)) {
                if(InfChMirror.EXCEPTIONS[board]!!.qanonPosts.isNotEmpty()) {
                    params.ids.addAll(InfChMirror.EXCEPTIONS[board]!!.qanonPosts)
                }
            }
        }

        println(">> board: $board")
        val posts = Gson().fromJson(catalogFile.readText(), Array<QCodeFagPost>::class.java)
        posts.forEachIndexed { index, post ->
            val postEvent = PostEvent.fromQCodeFagPost("qcodefag", board, post)
            var include = false

            // Search on trip
            if(params.trips.isNotEmpty() && params.trips.contains(post.trip)) {
                include = true
            }

            // Search on content
            if(!include) {
                if (params.content != null) {
                    val results = params.content.find(postEvent.Text())
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

