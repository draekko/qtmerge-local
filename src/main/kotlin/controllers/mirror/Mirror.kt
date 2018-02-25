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

    data class SearchParameters(
        val trips : MutableList<String> = mutableListOf(),
        val ids : MutableList<String> = mutableListOf(),
        val content : Regex? = null,
        val referenceDepth : ReferenceDepth = ReferenceDepth.None,
        val onlyQT : Boolean = true
    )
    abstract fun Mirror()
    abstract fun MirrorReferences()
    abstract fun MirrorSearch(params : SearchParameters = SearchParameters()) : List<Event>

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