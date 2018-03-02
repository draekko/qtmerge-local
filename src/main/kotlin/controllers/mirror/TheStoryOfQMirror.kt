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
        mirrorDirectory : String,
        board : String,
        source : Source,
        val boardFileID : String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, QTMirror.ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), QTMirror.ZONEID)
) : Mirror(mirrorDirectory, board, source, "thestoryofq") {
    var threads: MutableList<InfChThread> = arrayListOf()
    val updatedThreads: MutableList<InfChThread> = arrayListOf()
    var mirrorRoot = mirrorDirectory + File.separator + "thestoryofq"
    var boardRoot = mirrorRoot + File.separator + "boards" + File.separator + boardFileID
    var filesRoot = mirrorRoot + File.separator + "files"
    val exceptions = when(source) {
        Source.FourChan -> FourChanMirror.Companion.EXCEPTIONS[board]!!
        Source.InfChan -> InfChMirror.Companion.EXCEPTIONS[board]!!
        else -> BoardExceptions()
    }

    override fun Mirror() {
        println(">> mirror: $this")
        updatedThreads.clear()
        if (MakeDirectory(mirrorRoot)) {
            if (!MakeDirectory(boardRoot)) {
                return
            }
            if (!MakeDirectory(filesRoot)) {
                return
            }

            val catalogURL = URL("http://www.thestoryofq.com/data/json/$boardFileID.json")
            val catalogFile = File(boardRoot + File.separator + "$boardFileID.json")

            // Update catalog json if necessary
            try {
                if (catalogFile.iterate(catalogURL.readBytesDelayed())) {
                    println("  Updated catalog for $boardFileID")
                }
            } catch (e: FileNotFoundException) {
                println("Unable to find catalog for $boardFileID: $e")
                return
            }
        }
    }

    override fun MirrorReferences() {
        println(">> mirror refs: $this")
        // TODO
    }

    override fun MirrorSearch(params:SearchParameters): List<Event> {
        val eventList: MutableList<Event> = arrayListOf()
        val mirrorRoot = mirrorDirectory + File.separator + "thestoryofq"
        val boardRoot = mirrorRoot + File.separator + "boards" + File.separator + boardFileID
        val catalogFile = File(boardRoot + File.separator + "$boardFileID.json")

        SetupSearchParameters(params, exceptions)

        println(">> search: $this")
        val posts = Gson().fromJson(catalogFile.readText(), Array<QCodeFagPost>::class.java)
        posts.forEachIndexed { index, post ->
            val postEvent = PostEvent.fromQCodeFagPost("thestoryofq", source, board, post)
            if(TestSearchParameters(params, exceptions, postEvent)) {
                eventList.add(postEvent)
            }
        }

        return eventList
    }
}

