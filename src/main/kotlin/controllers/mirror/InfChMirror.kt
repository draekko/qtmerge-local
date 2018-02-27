package controllers.mirror

import QTMirror.Companion.DATEFORMATTER
import QTMirror.Companion.ZONEID
import com.google.gson.Gson
import extensions.iterate
import extensions.readBytesDelayed
import models.events.Event
import models.events.PostEvent
import models.mirror.InfChPostSet
import models.mirror.InfChThread
import models.mirror.InfChThreadPage
import utils.HTML.Companion.cleanHTMLText
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class InfChMirror(
        outputDirectory : String,
        board : String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, QTMirror.ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), QTMirror.ZONEID)
) : Mirror("8Ch", board, outputDirectory) {
    var threads : MutableList<InfChThread> = arrayListOf()
    val updatedThreads : MutableList<InfChThread> = arrayListOf()
    var mirrorRoot = outputDirectory + File.separator + "8ch"
    var boardRoot = mirrorRoot + File.separator + "boards" + File.separator + board
    var filesRoot = mirrorRoot + File.separator + "files"

    class BoardExceptions(
        val orphanThreads : List<InfChThread> = emptyList(),
        val qanonPosts : List<String> = emptyList(),
        val qstopTimes : MutableMap<String, ZonedDateTime> = mutableMapOf(),
        val nonqPosts : List<String> = emptyList()
    )

    companion object {
        val ACTIVEQTRIPS = listOf("!UW.yye1fxo")
        val QTRIPS = listOf( "!UW.yye1fxo", "!ITPb.qbhqo" )
        val EXCEPTIONS = mutableMapOf(
            Pair("qresearch", BoardExceptions(qanonPosts = listOf("476325", "476806", "508699"))),
            Pair("cbts", BoardExceptions(
                    orphanThreads = listOf(
                            InfChThread(126564, time = 1513718366, last_modified = 1513725863)
                            //404 InfChThread(139594, time = 1513718366, last_modified = 1513725863)
                    ),
                    qstopTimes = mutableMapOf(
                            Pair("!ITPb.qbhqo", ZonedDateTime.of(2017, 12, 25, 15, 57, 38, 0, ZONEID)),
                            Pair("!UW.yye1fxo", ZonedDateTime.of(2018, 1, 6, 0, 14, 43, 0, ZONEID))
                    )
            )),
            Pair("thestorm", BoardExceptions(
                    qstopTimes = mutableMapOf(
                            Pair("!ITPb.qbhqo", ZonedDateTime.ofInstant(Instant.EPOCH, ZONEID)) // Never posted on thestorm with that trip code
                    )
            ))
        )
    }

    override fun Mirror() {
        updatedThreads.clear()
        if(MakeDirectory(mirrorRoot)) {
            if(!MakeDirectory(boardRoot)) {
                return
            }
            if(!MakeDirectory(filesRoot)) {
                return
            }

            val catalogURL = URL("https://8ch.net/$board/catalog.json")
            val catalogFile = File(boardRoot + File.separator + "catalog.json")

            // Update catalog json if necessary
            try {
                if (catalogFile.iterate(catalogURL.readBytesDelayed())) {
                    println("  Updated catalog for $board")
                }
            } catch(e : FileNotFoundException) {
                println("Unable to find catalog for $board: $e")
                return
            }

            val catalog = Gson().fromJson(catalogFile.readText(), Array<InfChThreadPage>::class.java)
            threads = arrayListOf()
            catalog.forEach { page ->
                page.threads.forEach { thread ->
                    threads.add(thread)
                }
            }
            if(EXCEPTIONS.containsKey(board)) {
                if(EXCEPTIONS[board]!!.orphanThreads.isNotEmpty()) {
                    threads.addAll(EXCEPTIONS[board]!!.orphanThreads)
                }
            }
            threads.sortedBy { -it.no }.forEachIndexed { index, thread ->
                if(Instant.ofEpochSecond(thread.time).isAfter(startTime.toInstant()) &&
                        Instant.ofEpochSecond(thread.time).isBefore(stopTime.toInstant())) {
                    val threadRoot = boardRoot + File.separator + thread.no
                    if (MakeDirectory(threadRoot)) {
                        val threadURL = URL("https://8ch.net/$board/res/${thread.no}.json")
                        val threadFile = File(threadRoot + File.separator + "${thread.no}.json")

                        // Update thread json if necessary
                        try {
                            if (threadFile.iterate(threadURL.readBytesDelayed())) {
                                println("    Updated thread ${thread.no} (${ZonedDateTime.ofInstant(Instant.ofEpochSecond(thread.time), ZONEID).format(DATEFORMATTER)})")
                                updatedThreads.add(thread)
                            }
                        } catch (e: FileNotFoundException) {
                            println("Unable to find thread ${thread.no} for $board, skipping.")
                            return@forEachIndexed
                        }
                    }
                }
            }
        }
    }

    fun MirrorFile(shouldUpdate : Boolean, filesRoot : String, tim : String, filename : String, ext : String) {
        // Unable to mirror deleted files
        if(ext == "deleted") {
            return
        }
        val thumbURL = URL("https://media.8ch.net/file_store/$tim$ext")
        val fileURL = URL("https://media.8ch.net/file_store/$tim$ext/${URLEncoder.encode("$filename$ext", "UTF-8")}")
        val thumbFile = File(filesRoot + File.separator + tim.first() + File.separator + tim.substring(0,2) + File.separator + tim + File.separator + tim + ext)
        val fileFile = File(filesRoot + File.separator + tim.first() + File.separator + tim.substring(0,2) + File.separator + tim + File.separator + filename + ext)

        try {
            if (MakeDirectory(thumbFile.parentFile.absolutePath)) {
                // TODO: verify checksums
                if (!thumbFile.exists() || shouldUpdate) {
                    if (thumbFile.iterate(thumbURL.readBytesDelayed())) {
                        println("      Updated file: $tim$ext")
                    }
                }
            }
            if (MakeDirectory(fileFile.parentFile.absolutePath)) {
                // TODO: verify checksums
                if(!fileFile.exists() || shouldUpdate) {
                    if (fileFile.iterate(fileURL.readBytesDelayed())) {
                        println("      Updated file: $filename$ext")
                    }
                }
            }
        } catch(e : FileNotFoundException) {
            println("    Unable to find file : `$fileURL'\n$e")
        } catch(e : IOException) {
            println("    Unable to retrieve file: `$fileURL'\n$e")
        }
    }

    override fun MirrorReferences() {
        println(">> board: $board")

        threads.sortedBy { -it.no }.forEachIndexed { index, thread ->
            val threadRoot = boardRoot + File.separator + thread.no
            val threadFile = File(threadRoot + File.separator + "${thread.no}.json")
            val threadUpdated = updatedThreads.contains(thread)
            // TODO: verify crc's? are some images scrubbed to prevent doxxing and then the crc mismatches? md5 in post data isn't updated for changes?

            // Check thread files and references
            if (!thread.tim.isNullOrEmpty()) {
                MirrorFile(threadUpdated, filesRoot, thread.tim!!, thread.filename!!, thread.ext ?: "")
            }
            MirrorReferences(threadUpdated, boardRoot, thread.no, thread.no, listOf(thread.name, thread.com).joinToString("\n"))

            // Check post files and references
            if(threadFile.exists()) {
                val postset = Gson().fromJson(threadFile.readText(), InfChPostSet::class.java)
                postset.posts.forEachIndexed { postIndex, post ->
                    if (postIndex == 0) {
                        println(">> thread: ${thread.no} (${cleanHTMLText(post.sub
                                ?: "").lines().first()}): ${index + 1} / ${threads.size} (% ${Math.round(index.toFloat() / threads.size * 100)})")
                        if (threadUpdated) {
                        }
                    }
                    if (!post.tim.isNullOrEmpty()) {
                        MirrorFile(threadUpdated, filesRoot, post.tim!!, post.filename!!, post.ext ?: "")
                    }
                    MirrorReferences(threadUpdated, boardRoot, thread.no, post.no, listOf(post.title, post.sub, post.com).joinToString("\n"))

                    post.extra_files?.forEach { file ->
                        MirrorFile(threadUpdated, filesRoot, file.tim, file.filename, file.ext)
                    }
                }
            } else {
                println(">> thread: ${thread.no}: unable to find thread file: $threadFile")
            }
        }
    }

    fun MirrorReferences(shouldUpdate: Boolean, boardRoot: String, thread: Long, post: Long, content: String) {
        // TODO:
        //   * Detect/fix broken links
        //   * Download links to post folder:
        //      * Anonfile downloads
        //      * Hooktube/Youtube videos
        //      * NewsEvent websites, web scrape
        //      * Twitter message
        //      * Etc.
        //   * Report unhandled links
        //
    }

    override fun MirrorSearch(params : SearchParameters) : List<Event> {
        val eventList: MutableList<Event> = arrayListOf()
        val mirrorRoot = outputDirectory + File.separator + "8ch"
        val boardRoot = mirrorRoot + File.separator + "boards" + File.separator + board
        val threads = mutableListOf<InfChThread>()

        println(">> board: $board")

        // Set trips/ids if onlyQT flag set
        if(params.onlyQT) {
            params.trips.clear()
            params.trips.addAll(QTRIPS)

            params.ids.clear()
            if(EXCEPTIONS.containsKey(board)) {
                if(EXCEPTIONS[board]!!.qanonPosts.isNotEmpty()) {
                    params.ids.addAll(EXCEPTIONS[board]!!.qanonPosts)
                }
            }
        }

        // Gather threads from catalogs
        File(boardRoot).listFiles().sortedBy { -it.lastModified() }.forEach { catalogFile ->
            if(catalogFile.name.startsWith("catalog") && catalogFile.extension.startsWith("json")) {
                val catalog = Gson().fromJson(catalogFile.readText(), Array<InfChThreadPage>::class.java)
                catalog.forEach { page ->
                    page.threads.forEach { thread ->
                        if(threads.find { it.no == thread.no } == null) {
                            threads.add(thread)
                        }
                    }
                }
            }
        }

        // Gather posts from threads
        threads.sortedBy { -it.no }.forEachIndexed { index, thread ->
            val pct = Math.round(index.toFloat() / threads.size * 1000)
            if(pct.rem(100) == 0) {
                println(">> thread: ${thread.no}: ${index + 1} / ${threads.size} (% ${pct/10})")
            }

            if (Instant.ofEpochSecond(thread.time).isAfter(startTime.toInstant()) &&
                    Instant.ofEpochSecond(thread.time).isBefore(stopTime.toInstant())) {
                val threadRoot = boardRoot + File.separator + thread.no
                if (MakeDirectory(threadRoot)) {
                    File(threadRoot).listFiles().sortedBy { -it.lastModified() }.forEach {
                        if (it.name.startsWith(thread.no.toString()) && it.extension.startsWith("json")) {
                            val postset = Gson().fromJson(it.readText(), InfChPostSet::class.java)
                            postset.posts.forEach { post ->
                                val postEvent = PostEvent.fromInfChPost("anonsw", board, post)
                                var include = false

                                // Search on trip
                                if(!include && params.trips.isNotEmpty()) {
                                    include = params.trips.contains(post.trip)
                                }

                                // Search on post id
                                if(!include && params.ids.isNotEmpty()) {
                                    include = params.ids.contains(postEvent.id)
                                }

                                // Handle exceptions
                                if(include && params.onlyQT) {
                                    if(EXCEPTIONS.containsKey(board)) {
                                        if(EXCEPTIONS[board]!!.nonqPosts.isNotEmpty()) {
                                            if(EXCEPTIONS[board]!!.nonqPosts.contains(postEvent.id)) {
                                                include = false
                                            }
                                        }
                                        if(EXCEPTIONS[board]!!.qstopTimes.containsKey(postEvent.trip)) {
                                            if(Instant.ofEpochSecond(postEvent.timestamp).isAfter(EXCEPTIONS[board]!!.qstopTimes[postEvent.trip]!!.toInstant())) {
                                                include = false
                                            }
                                        }
                                    }
                                }

                                // Search on content
                                if(!include && (params.content != null)) {
                                    include = (params.content.find(postEvent.Text()) != null)
                                }

                                // Finally, add to event list if it isn't already there
                                if(include && (eventList.find { it.Link() == postEvent.Link() } == null)) {
                                    eventList.add(postEvent)
                                }
                            }
                        }
                    }
                }
            }
        }

        return eventList
    }

    /*
    override fun MirrorSearchQT(content: Regex?, referenceDepth: ReferenceDepth): List<Event> {
        var ids = emptyList<String>()
        if(EXCEPTIONS.containsKey(board)) {
            if(EXCEPTIONS[board]!!.qanonPosts.isNotEmpty()) {
                ids = EXCEPTIONS[board]!!.qanonPosts
            }
        }
        return MirrorSearch(QTRIPS, ids, content, referenceDepth)
    }
    */
}