package controllers.mirror

import QTMirror.Companion.ZONEID
import com.sun.org.apache.xpath.internal.operations.Or
import models.events.Event
import models.events.TweetEvent
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

    enum class Source(val title : String, val url : String) {
        FourChan("4Chan", "https://4chan.org/"),
        InfChan("8ch", "https://8ch.net/"),
        Twitter("Twitter", "https://twitter.com/"),
        LinkedData("Link", "");

        override fun toString(): String {
            return title
        }
    }

    enum class SearchOperator(val initialCondition : Boolean) {
        And(true),
        Or(false);

        fun combine(op1 : Boolean, op2 : Boolean) : Boolean = if(this == And) op1 && op2 else op1 || op2
    }

    abstract class SearchOperand(
        val name : String
    ) {
        abstract fun Search(exceptions: BoardExceptions, event: Event) : Boolean

        class IDs(val ids : MutableList<String>) : SearchOperand("IDs") {
            override fun Search(exceptions: BoardExceptions, event: Event) : Boolean = ids.contains(event.ID())
        }

        class Trips(val trips : MutableList<String>) : SearchOperand("Trips") {
            override fun Search(exceptions: BoardExceptions, event: Event): Boolean = trips.contains(event.Trip())
        }

        class QT : SearchOperand("QT") {
            override fun Search(exceptions: BoardExceptions, event: Event): Boolean {
                if(event.Source() == Source.Twitter) {
                    return true
                }
                if (exceptions.qstopTimes.containsKey(event.Trip())) {
                    if (event.Timestamp().isAfter(exceptions.qstopTimes[event.Trip()]!!)) {
                        return false
                    }
                }
                if (exceptions.nonqPosts.isNotEmpty()) {
                    if (exceptions.nonqPosts.contains(event.ID())) {
                        return false
                    }
                }
                if(exceptions.qtrips.isNotEmpty()) {
                    if(exceptions.qtrips.contains(event.Trip())) {
                        return true
                    }
                }
                if(exceptions.qanonPosts.isNotEmpty()) {
                    if(exceptions.qanonPosts.contains(event.ID())) {
                        return true
                    }
                }

                return false
            }
        }

        class OP : SearchOperand("OP") {
            override fun Search(exceptions: BoardExceptions, event: Event): Boolean = event.ID() == event.ThreadID()
        }

        class Content(val regex: Regex) : SearchOperand("Content") {
            override fun Search(exceptions: BoardExceptions, event: Event): Boolean = regex.find(event.Text()) != null
        }

        data class Condition (
                val operator : SearchOperator,
                val operands : List<SearchOperand>
        ) : SearchOperand("Condition") {
            override fun Search(exceptions: BoardExceptions, event: Event): Boolean =
                    operands.foldRight(operator.initialCondition, { op, acc -> operator.combine(acc, op.Search(exceptions, event)) })
        }
    }

    data class SearchParameters(
        val condition: SearchOperand = SearchOperand.QT(),
        val referenceDepth : ReferenceDepth = ReferenceDepth.None
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

    override fun toString(): String {
        return "$dataset $source $board"
    }
}