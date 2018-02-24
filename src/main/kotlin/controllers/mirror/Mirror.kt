package controllers.mirror

import QTMirror.Companion.ZONEID
import models.events.Event
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.Instant



abstract class Mirror(
    val type : String,
    val board : String,
    val outputDirectory : String
) {
    enum class ReferenceDepth {
        None,
        Shallow,
        Deep
    }
    abstract fun Mirror()
    abstract fun MirrorReferences()
    abstract fun MirrorSearch(
            trips : List<String> = listOf(),
            ids : List<String> = listOf(),
            content : Regex? = null,
            referenceDepth : ReferenceDepth = ReferenceDepth.None
    ) : List<Event>

    fun MakeDirectory(path : String) : Boolean {
        var isSuccessful = true
        val outDir = File(path)
        if (outDir.exists() && !outDir.isDirectory) {
            println("Destination path is not a directory: ${outDir.absolutePath}")
            isSuccessful = false
        } else {
            // Create mirror directory if it doesn't exist
            if (!outDir.exists()) {
                if(outDir.mkdirs()) {
                    //println("Created path: ${outDir.absolutePath}")
                } else {
                    println("Unable to create path: ${outDir.absolutePath}")
                    isSuccessful = false
                }
            }
        }
        return isSuccessful
    }

    fun URLModifiedDateTime(url : URL) : ZonedDateTime {
        val connection = url.openConnection() as HttpURLConnection
        val dateTime = connection.lastModified
        connection.disconnect()
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateTime), ZONEID)
    }

    override fun toString(): String {
        return "$type $board"
    }
}