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
import java.time.ZonedDateTime

class InfChMirror(
        outputDirectory : String,
        val board : String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, QTMirror.ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), QTMirror.ZONEID)
) : Mirror(outputDirectory) {

    fun Mirror() {
        val mirrorRoot = outputDirectory + File.separator + "8ch"
        if(MakeDirectory(mirrorRoot)) {
            val boardRoot = mirrorRoot + File.separator + "boards" + File.separator + board
            val filesRoot = mirrorRoot + File.separator + "files"

            if(!MakeDirectory(boardRoot)) {
                return
            }
            if(!MakeDirectory(filesRoot)) {
                return
            }

            val catalogURL = URL("https://8ch.net/$board/catalog.json")
            val catalogFile = File(boardRoot + File.separator + "catalog.json")

            println(">> board: $board")
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
            val threads = mutableListOf<InfChThread>()
            catalog.forEach { page ->
                page.threads.forEach { thread ->
                    threads.add(thread)
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
                        var threadUpdated = false
                        try {
                            if (threadFile.iterate(threadURL.readBytesDelayed())) {
                                threadUpdated = true
                            }
                        } catch (e: FileNotFoundException) {
                            println("Unable to find thread ${thread.no} for $board, skipping.")
                            return@forEachIndexed
                        }

                        // TODO: verify crc's? are some images scrubbed to prevent doxxing and then the crc mismatches? md5 in post data isn't updated for changes?

                        // Check thread files and references
                        if(!thread.tim.isNullOrEmpty()) {
                            MirrorFile(threadUpdated, filesRoot, thread.tim!!, thread.filename!!, thread.ext?:"")
                        }
                        MirrorReferences(threadUpdated, boardRoot, thread.no, thread.no, listOf(thread.name, thread.com).joinToString("\n"))

                        // Check post files and references
                        val postset = Gson().fromJson(threadFile.readText(), InfChPostSet::class.java)
                        postset.posts.forEachIndexed { postIndex, post ->
                            if(postIndex == 0) {
                                println(">> thread: ${thread.no} (${cleanHTMLText(post.sub?:"").lines().first()}): ${index + 1} / ${threads.size} (% ${Math.round(index.toFloat() / threads.size * 100)})")
                                if (threadUpdated) {
                                    println("    Updated thread ${thread.no} (${ZonedDateTime.ofInstant(Instant.ofEpochSecond(thread.time), ZONEID).format(DATEFORMATTER)})")
                                }
                            }
                            if(!post.tim.isNullOrEmpty()) {
                                MirrorFile(threadUpdated, filesRoot, post.tim!!,  post.filename!!, post.ext?:"")
                            }
                            MirrorReferences(threadUpdated, boardRoot, thread.no, post.no, listOf(post.title, post.sub, post.com).joinToString("\n"))

                            post.extra_files?.forEach { file ->
                                MirrorFile(threadUpdated, filesRoot, file.tim, file.filename, file.ext)
                            }
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
        val fileURL = URL("https://media.8ch.net/file_store/$tim$ext/${URLEncoder.encode("$filename.$ext", "UTF-8")}")
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

    fun MirrorSearch(trips : List<String> = listOf(), content : Regex? = null) : List<Event> {
        val eventList: MutableList<Event> = arrayListOf()
        val mirrorRoot = outputDirectory + File.separator + "8ch"
        val boardRoot = mirrorRoot + File.separator + "boards" + File.separator + board
        val catalogFile = File(boardRoot + File.separator + "catalog.json")

        println(">> board: $board")
        val catalog = Gson().fromJson(catalogFile.readText(), Array<InfChThreadPage>::class.java)
        val threads = mutableListOf<InfChThread>()
        catalog.forEach { page ->
            page.threads.forEach { thread ->
                threads.add(thread)
            }
        }
        threads.sortedBy { -it.no }.forEachIndexed { index, thread ->
            val pct = Math.round(index.toFloat() / threads.size * 1000)
            if(pct.rem(100) == 0) {
                println(">> thread: ${thread.no}: ${index + 1} / ${threads.size} (% ${pct/10})")
            }

            if (Instant.ofEpochSecond(thread.time).isAfter(startTime.toInstant()) &&
                    Instant.ofEpochSecond(thread.time).isBefore(stopTime.toInstant())) {
                val threadRoot = boardRoot + File.separator + thread.no
                if (MakeDirectory(threadRoot)) {
                    val threadFile = File(threadRoot + File.separator + "${thread.no}.json")

                    val postset = Gson().fromJson(threadFile.readText(), InfChPostSet::class.java)
                    postset.posts.forEach { post ->
                        val postEvent = PostEvent.fromInfChPost(board, post)
                        var include = false
                        // TODO: perform searches
                        //println(">> post: ${post.no}")
                        if(trips.isNotEmpty() && trips.contains(post.trip)) {
                            include = true
                        }

                        if(!include) {
                            if (content != null) {
                                val results = content.find(postEvent.Text())
                                if (results != null) {
                                    include = true
                                }
                            }
                        }

                        if(include) {
                            eventList.add(postEvent)
                        }
                    }
                }
            }
        }

        return eventList
    }
}