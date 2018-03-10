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

class TheStoryOfQMirror(
        board : String,
        source : Source,
        val boardFileID : String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZONEID)
) : Mirror(board, source, "thestoryofq") {
    var threads: MutableList<InfChThread> = arrayListOf()
    val updatedThreads: MutableList<InfChThread> = arrayListOf()
    var mirrorLayout = MirrorLayout(DATADIR, dataset, "thestoryofq.com", boardFileID)
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

            val catalogURL = URL("http://www.thestoryofq.com/data/json/$boardFileID.json")
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

    override fun MirrorSearch(params:SearchParameters): List<Event> {
        val eventList: MutableList<Event> = arrayListOf()
        val catalogFile = File(mirrorLayout.boards + File.separator + "$boardFileID.json")

        println(">> search: $this")
        val posts = Gson().fromJson(catalogFile.readText(), Array<QCodeFagPost>::class.java)
        posts.forEachIndexed { index, post ->
            val postEvent = PostEvent.fromQCodeFagPost("thestoryofq", source, board, catalogFile.absolutePath, post)
            if(params.condition.Search(exceptions, postEvent)) {
                eventList.add(postEvent)
            }
        }

        return eventList
    }
}

