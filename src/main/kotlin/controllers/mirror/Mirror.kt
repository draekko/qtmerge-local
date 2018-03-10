package controllers.mirror

import models.events.Event
import settings.Settings.Companion.ZONEID
import utils.sanitizeFileName
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

abstract class Mirror(
        val board : String,
        val source : Source,
        val dataset: String
) {
    class MirrorLayout(
            directory : String,
            dataset : String,
            source : String,
            board : String,
            var root : String = directory + File.separator + dataset + File.separator + source,
            var boards : String = root + File.separator + "boards" + File.separator + board,
            var files : String = root + File.separator + "files"
    )

    enum class ReferenceDepth {
        None,
        Shallow,
        Deep;

        fun cacheID() : String = name
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
        Or(false),
        AndNot(true),
        OrNot(false);

        fun cacheID() : String = name

        fun combine(op1 : Boolean, op2 : Boolean) : Boolean {
            return when(this) {
                And -> {
                    op1 && op2
                }
                Or -> {
                    op1 || op2
                }
                AndNot -> {
                    op1 && !op2
                }
                OrNot -> {
                    op1 || !op2
                }
            }
        }
    }

    abstract class SearchOperand(
        val name : String
    ) {
        abstract fun cacheID() : String
        abstract fun Search(exceptions: BoardExceptions, event: Event) : Boolean

        class IDs(val ids : MutableList<String>) : SearchOperand("IDs") {
            override fun cacheID(): String = "$name:" + ids.joinToString(",")
            override fun Search(exceptions: BoardExceptions, event: Event) : Boolean = ids.contains(event.ID())
        }

        class Trips(val trips : MutableList<String>) : SearchOperand("Trips") {
            override fun cacheID(): String = "$name:" + trips.joinToString(",")
            override fun Search(exceptions: BoardExceptions, event: Event): Boolean = trips.contains(event.Trip())
        }

        class QT : SearchOperand("QT") {
            override fun cacheID(): String = name
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
            override fun cacheID(): String = name
            override fun Search(exceptions: BoardExceptions, event: Event): Boolean = event.ID() == event.ThreadID()
        }

        class Content(val regex: Regex) : SearchOperand("Content") {
            override fun cacheID(): String = "regex|${regex.pattern}|"
            override fun Search(exceptions: BoardExceptions, event: Event): Boolean = regex.find(event.Text()) != null
        }

        data class Condition (
                val operator : SearchOperator,
                val operands : List<SearchOperand>
        ) : SearchOperand("Condition") {
            constructor(operator : SearchOperator, operand: SearchOperand) : this(operator, listOf(operand))

            override fun cacheID() : String {
                return "(" + operands.map { it.cacheID() }.joinToString(" ${operator.cacheID()} ") + ")"
            }

            override fun Search(exceptions: BoardExceptions, event: Event): Boolean =
                    operands.foldRight(operator.initialCondition, { op, acc -> operator.combine(acc, op.Search(exceptions, event)) })
        }
    }

    data class SearchParameters(
        val condition: SearchOperand = SearchOperand.QT(),
        val referenceDepth : ReferenceDepth = ReferenceDepth.None
    ) {
        fun cacheID() : String {
            val id = condition.cacheID() + "-" + referenceDepth.cacheID()

            return sanitizeFileName(id)
        }
    }

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