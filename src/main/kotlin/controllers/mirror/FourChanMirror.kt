package controllers.mirror

import QTMirror
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import extensions.iterate
import extensions.readBytesDelayed
import models.events.Event
import models.events.PostEvent
import models.mirror.FourChanPost
import models.mirror.FourChanThread
import utils.HTML.Companion.cleanHTMLText
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

class FourChanMirror(
        outputDirectory : String,
        board : String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, QTMirror.ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), QTMirror.ZONEID)
) : Mirror("4chan", board, outputDirectory) {
    val mirrorRoot = outputDirectory + File.separator + "4chan"
    val boardRoot = mirrorRoot + File.separator + "boards" + File.separator + board
    var filesRoot = mirrorRoot + File.separator + "files"
    var threads : MutableList<String> = arrayListOf()
    val updatedThreads : MutableList<String> = arrayListOf()

    init {
        // Collect unique threads from qcodefag and qanonmap
        QCodeFagMirror(outputDirectory, "pol4chanPosts", startTime, stopTime).MirrorSearch(SearchParameters(content = Regex(".*"), onlyQT = false))
                .forEach { post ->
                    if(!threads.contains((post as PostEvent).threadId)) {
                        threads.add(post.threadId)
                    }
                }
        QAnonMapMirror(outputDirectory, "pol4chanPosts", startTime, stopTime).MirrorSearch(SearchParameters(content = Regex(".*"), onlyQT = false))
                .forEach { post ->
                    if(!threads.contains((post as PostEvent).threadId)) {
                        threads.add(post.threadId)
                    }
                }
    }

    override fun Mirror() {
        updatedThreads.clear()
        if (MakeDirectory(mirrorRoot)) {
            val filesRoot = mirrorRoot + File.separator + "files"

            if (!MakeDirectory(boardRoot)) {
                return
            }
            if (!MakeDirectory(filesRoot)) {
                return
            }

            threads.sortedBy{ -it.toLong() }.forEach { thread ->
                val threadRoot = boardRoot + File.separator + thread
                if (MakeDirectory(threadRoot)) {
                    val threadURL = URL("https://archive.4plebs.org/_/api/chan/thread/?board=pol&num=$thread")
                    val threadFile = File(threadRoot + File.separator + "$thread.json")

                    // Update activity json if necessary
                    try {
                        if (threadFile.iterate(threadURL.readBytesDelayed())) {
                            println("    Updated thread $thread") // (${ZonedDateTime.ofInstant(Instant.ofEpochSecond(thread.time), ZONEID).format(DATEFORMATTER)})")
                            updatedThreads.add(thread)
                        }
                    } catch (e: FileNotFoundException) {
                        println("Unable to find thread for $board: $e")
                        return
                    }
                }
            }
        }
    }

    override fun MirrorReferences() {
        threads.sortedBy { -it.toLong() }.forEach { thread ->
            val threadRoot = boardRoot + File.separator + thread
            val threadFile = File(threadRoot + File.separator + "$thread.json")
            val threadUpdated = updatedThreads.contains(thread)

            // Check post files and references
            if(threadFile.exists()) {
                val listType = object : TypeToken<Map<String, FourChanThread>>() {}.type
                val threadMap : Map<String, FourChanThread> = Gson().fromJson(threadFile.readText(), listType)

                // Check thread files and references
                if(threadMap[thread]!!.op.media != null) {
                    MirrorFile(threadUpdated, threadMap[thread]!!.op.media!!)
                }
                //MirrorReferences(threadUpdated, boardRoot, thread.no, thread.no, listOf(thread.name, thread.com).joinToString("\n"))

                threadMap[thread]!!.posts.keys.forEachIndexed { index, postIndex ->
                    val post = threadMap[thread]!!.posts[postIndex]!!
                    if (index == 0) {
                        println(">> thread: $thread (${cleanHTMLText(post.comment)
                                .lines().first()}): ${index + 1} / ${threads.size} (% ${Math.round(index.toFloat() / threads.size * 100)})")
                        if (threadUpdated) {
                        }
                    }

                    if(post.media != null) {
                        MirrorFile(threadUpdated, post.media!!)
                    }
                    //MirrorReferences(threadUpdated, boardRoot, thread.no, post.no, listOf(post.title, post.sub, post.com).joinToString("\n"))
                }
            } else {
                println(">> thread: $thread: unable to find thread file: $threadFile")
            }
        }
    }

    fun MirrorFile(shouldUpdate: Boolean, media : FourChanPost.FourChanPostMedia) {
        // Don't mirror banned files
        if(media.banned == "1") {
            return
        }
        val thumbURL = URL(media.thumb_link)
        val fileURL = URL(media.media_link)
        val firstNest = media.media.substring(0, 4)
        val secondNest = media.media.substring(4, 6)
        val folder = media.media.substring(0, media.media.lastIndexOf('.'))
        val thumbFile = File(filesRoot + File.separator + firstNest + File.separator + secondNest + File.separator + folder + File.separator + media.preview_orig)
        val fileFile = File(filesRoot + File.separator + firstNest + File.separator + secondNest + File.separator + folder + File.separator + media.media_filename_processed)

        try {
            if (MakeDirectory(thumbFile.parentFile.absolutePath)) {
                // TODO: verify checksums
                if (!thumbFile.exists() || shouldUpdate) {
                    if (thumbFile.iterate(thumbURL.readBytesDelayed())) {
                        println("      Updated file: ${thumbFile.name}")
                    }
                }
            }
            if (MakeDirectory(fileFile.parentFile.absolutePath)) {
                // TODO: verify checksums
                if(!fileFile.exists() || shouldUpdate) {
                    if (fileFile.iterate(fileURL.readBytesDelayed())) {
                        println("      Updated file: ${fileFile.name}")
                    }
                }
            }
        } catch(e : FileNotFoundException) {
            println("    Unable to find file : `$fileURL'\n$e")
        } catch(e : IOException) {
            println("    Unable to retrieve file: `$fileURL'\n$e")
        }

    }

    override fun MirrorSearch(params: SearchParameters): List<Event> {
        // TODO
        return emptyList()
    }
}
