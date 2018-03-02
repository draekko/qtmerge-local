package controllers.mirror

import QTMirror.Companion.ZONEID
import models.events.Event
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.ZonedDateTime
import java.time.Instant

abstract class Mirror(
        val mirrorDirectory : String,
        val board : String,
        val source : Source,
        val dataset: String
) {
    enum class ReferenceDepth {
        None,
        Shallow,
        Deep
    }

    enum class Source {
        FourChan,
        InfChan,
        Twitter,
        LinkedData
    }

    data class SearchParameters(
        val trips : MutableList<String> = mutableListOf(),
        val ids : MutableList<String> = mutableListOf(),
        val content : Regex? = null,
        val referenceDepth : ReferenceDepth = ReferenceDepth.None,
        val onlyQT : Boolean = true
    )

    data class BoardExceptions(
        val orphanThreads : List<String> = emptyList(),
        val qtrips : List<String> = emptyList(),
        val qanonPosts : List<String> = emptyList(),
        val qstopTimes : MutableMap<String, ZonedDateTime> = mutableMapOf(),
        val nonqPosts : List<String> = emptyList(),
        val qgraphics : List<Pair<String, List<String>>> = emptyList()
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

    fun SetupSearchParameters(params: SearchParameters, exceptions : BoardExceptions) {
        // Set trips/ids if onlyQT flag set
        if(params.onlyQT) {
            params.trips.clear()
            params.trips.addAll(exceptions.qtrips)

            params.ids.clear()
            if(exceptions.qanonPosts.isNotEmpty()) {
                params.ids.addAll(exceptions.qanonPosts)
            }
        }
    }

    fun TestSearchParameters(params: SearchParameters, exceptions: BoardExceptions, event : Event) : Boolean {
        var testPasses = false

        // Search on trip
        if(!testPasses && params.trips.isNotEmpty()) {
            testPasses = params.trips.contains(event.Trip())
        }

        // Search on post id
        if(!testPasses && params.ids.isNotEmpty()) {
            testPasses = params.ids.contains(event.ID())
        }

        // Handle exceptions
        if(testPasses && params.onlyQT) {
            if (exceptions.nonqPosts.isNotEmpty()) {
                if (exceptions.nonqPosts.contains(event.ID())) {
                    testPasses = false
                }
            }
            if (exceptions.qstopTimes.containsKey(event.Trip())) {
                if (event.Timestamp().isAfter(exceptions.qstopTimes[event.Trip()]!!)) {
                    testPasses = false
                }
            }
        }

        // Search on content
        if(!testPasses && (params.content != null)) {
            testPasses = (params.content.find(event.Text()) != null)
        }


        return testPasses
    }

    override fun toString(): String {
        return "$dataset $source $board"
    }
}