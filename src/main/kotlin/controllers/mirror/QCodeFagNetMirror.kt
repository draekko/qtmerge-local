package controllers.mirror

import com.google.gson.Gson
import extensions.iterate
import extensions.readBytesDelayed
import models.events.Event
import models.events.PostEvent
import models.importer.QCodeFagPost
import models.mirror.InfChThread
import settings.Settings.Companion.ZONEID
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

class QCodeFagNetMirror(
        mirrorDirectory : String,
        board : String,
        source : Source,
        val boardFileID: String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZONEID)
) : Mirror(mirrorDirectory, board, source, "qcodefagnet") {
    var threads: MutableList<InfChThread> = mutableListOf()
    val updatedThreads: MutableList<InfChThread> = mutableListOf()
    var mirrorRoot = mirrorDirectory + File.separator + dataset
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

            // board name : _allQPosts
            val catalogURL = URL("https://github.com/QCodeFagNet/SFW.ChanScraper/raw/master/SFW.ChanScraper/json/$boardFileID.json")
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

    override fun MirrorSearch(params: SearchParameters): List<Event> {
        val eventList: MutableList<Event> = arrayListOf()
        val mirrorRoot = mirrorDirectory + File.separator + dataset
        val boardRoot = mirrorRoot + File.separator + "boards" + File.separator + boardFileID
        val catalogFile = File(boardRoot + File.separator + "$boardFileID.json")

        println(">> search: $this")
        val posts = Gson().fromJson(catalogFile.readText(), Array<QCodeFagPost>::class.java)
        posts.forEachIndexed { index, post ->
            val postEvent = PostEvent.fromQCodeFagPost(dataset, source, board, catalogFile.absolutePath, post)
            if(params.condition.Search(exceptions, postEvent)) {
                // Add to event list if it isn't already there
                if(eventList.find { it.Link() == postEvent.Link() } == null) {
                    eventList.add(postEvent)
                }
            }
        }

        return eventList
    }
}
