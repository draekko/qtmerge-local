package controllers.mirror

import com.google.gson.Gson
import extensions.iterate
import extensions.readBytesDelayed
import models.events.Event
import models.events.PostEvent
import models.mirror.QCodeFagPost
import models.mirror.InfChThread
import settings.Settings.Companion.DATADIR
import settings.Settings.Companion.ZONEID
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

class QCodeFagMirror(
        board : String,
        source : Source,
        val boardFileID: String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZONEID)
) : Mirror(board, source, "qcodefag") {
    var threads: MutableList<InfChThread> = mutableListOf()
    val updatedThreads: MutableList<InfChThread> = mutableListOf()
    var mirrorLayout = MirrorLayout(DATADIR, dataset, "github.com", boardFileID)
    //var mirrorRoot = DATADIR + File.separator + dataset
    //var boardRoot = mirrorRoot + File.separator + "boards" + File.separator + boardFileID
    //var filesRoot = mirrorRoot + File.separator + "files"
    val exceptions = when(source) {
        Source.FourChan -> FourPlebsMirror.Companion.EXCEPTIONS[board]!!
        Source.InfChan -> InfChMirror.Companion.EXCEPTIONS[board]!!
        else -> BoardExceptions()
    }

    override fun Mirror() {
        println(">> mirror: $this")
        updatedThreads.clear()
        if (MakeDirectory(mirrorLayout.root)) {
            if (!MakeDirectory(mirrorLayout.boards)) {
                return
            }
            if (!MakeDirectory(mirrorLayout.files)) {
                return
            }

            //val catalogURL = URL("http://qcodefag.github.io/data/$board.json")
            //val catalogURL = URL("http://qanonposts.com/data/$board.json")
            val catalogURL = URL("https://github.com/QCodefag/QCodefag.github.io/raw/master/data/$boardFileID.json")
            val catalogFile = File(mirrorLayout.boards + File.separator + "$boardFileID.json")

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
        val catalogFile = File(mirrorLayout.boards + File.separator + "$boardFileID.json")

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

