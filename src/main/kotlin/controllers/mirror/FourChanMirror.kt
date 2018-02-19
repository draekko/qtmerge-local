package controllers.mirror

import com.google.gson.Gson
import extensions.iterate
import extensions.readBytesDelayed
import models.events.Event
import models.mirror.FourChanBoardActivity
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

class FourChanMirror(
        outputDirectory : String,
        val board : String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, QTMirror.ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), QTMirror.ZONEID)
) : Mirror(outputDirectory) {
    val baseURL = "https://archive.4plebs.org"

    override fun Mirror() {
        val mirrorRoot = outputDirectory + File.separator + "4chan"
        if (MakeDirectory(mirrorRoot)) {
            val boardRoot = mirrorRoot + File.separator + "boards" + File.separator + board
            val filesRoot = mirrorRoot + File.separator + "files"

            if (!MakeDirectory(boardRoot)) {
                return
            }
            if (!MakeDirectory(filesRoot)) {
                return
            }

            val activityURL = URL("http://archive.4plebs.org/_/api/chan/statistics/?board=$board&stat=activity")
            val activityFile = File(boardRoot + File.separator + "activity.json")

            println(">> board: $board")
            // Update activity json if necessary
            try {
                if (activityFile.iterate(activityURL.readBytesDelayed())) {
                    println("  Updated catalog for $board")
                }
            } catch(e : FileNotFoundException) {
                println("Unable to find catalog for $board: $e")
                return
            }

            val activity = Gson().fromJson(activityFile.readText(), FourChanBoardActivity::class.java)
            activity.data.total.forEach {
                println("${it.time}")
            }
        }
    }

    override fun MirrorReferences() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun MirrorSearch(trips: List<String>, content: Regex?, referenceDepth: ReferenceDepth): List<Event> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
