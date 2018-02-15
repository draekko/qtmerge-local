package controllers.importer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import models.*
import java.io.File
import java.net.URL

class QCodeFagImporter(importDirectory : String) : Importer(importDirectory) {
    fun ImportQPosts(board : String, hasTripCode: Boolean, forum : String, fileName : String) : List<QPost> {
        println("Importing qposts from $importDirectory/$fileName...")
        return Gson().fromJson(File("$importDirectory/$fileName").readText(), Array<QPost>::class.java).filter { it.Timestamp().isAfter(startTime) }
    }

    fun UpdateQPosts(board : String, hasTripCode: Boolean, forum : String, fileName : String) {
        println("Updating qposts from ${forum}/${board}...")
        val posts = ImportQPosts(board, hasTripCode, forum, fileName).toMutableList()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val catalog = Gson().fromJson(URL("https://8ch.net/${board}/catalog.json").readText(), Array<InfChThreadPage>::class.java)
        val threads = mutableListOf<InfChThread>()
        catalog.forEach { page ->
            page.threads.forEach { thread ->
                threads.add(thread)
            }
        }
        threads.sortedBy { -it.no }.forEachIndexed { index, thread ->
            if(index > 20) {
                return@forEachIndexed
            }

            val postset = Gson().fromJson(URL("https://8ch.net/$board/res/${thread.no}.json").readText(), InfChPostSet::class.java)
            postset.posts.filter { it.trip == "!UW.yye1fxo" }.forEach {
                val qpost = QPost.fromInfChPost(board, it)
                if(posts.count { it.id == qpost.id } == 0) {
                    posts.add(qpost)
                    println("New post: $board/${thread.no}#${qpost.id}")
                }
            }
        }
        File("$importDirectory/$fileName").writeText(gson.toJson(posts))
    }

    fun ImportNews(fileName: String) : List<News> {
        println("Importing news from $importDirectory/$fileName...")
        return Gson().fromJson(File("$importDirectory/$fileName").readText(), Array<News>::class.java).filter { it.Timestamp().isAfter(startTime)}
    }
}