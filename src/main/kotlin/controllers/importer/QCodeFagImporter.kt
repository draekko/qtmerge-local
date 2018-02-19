package controllers.importer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import models.events.NewsEvent
import models.events.PostEvent
import models.importer.QCodeFagPost
import models.mirror.InfChPostSet
import models.mirror.InfChThread
import models.mirror.InfChThreadPage
import java.io.File
import java.net.URL

class QCodeFagImporter(importDirectory : String) : Importer(importDirectory) {
    fun ImportQPosts(dataset : String, board : String, hasTripCode: Boolean, forum : String, fileName : String) : List<PostEvent> {
        println("Importing qposts from $importDirectory/$fileName...")
        return Gson().fromJson(File("$importDirectory/$fileName").readText(), Array<QCodeFagPost>::class.java).map { PostEvent.fromQCodeFagPost(dataset, board, it) }.filter { it.Timestamp().isAfter(startTime) }
    }

    fun ImportNews(fileName: String) : List<NewsEvent> {
        println("Importing news from $importDirectory/$fileName...")
        return Gson().fromJson(File("$importDirectory/$fileName").readText(), Array<NewsEvent>::class.java).filter { it.Timestamp().isAfter(startTime)}
    }
}