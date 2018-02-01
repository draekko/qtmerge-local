package controllers.importer

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import models.News
import models.QPost
import java.io.File

class QCodeFagImporter(importDirectory : String) : Importer(importDirectory) {
    fun ImportQPosts(board : String, hasTripCode: Boolean, forum : String, fileName : String) : List<QPost> {
        println("Importing qposts from $importDirectory/$fileName...")
        return Gson().fromJson(File("$importDirectory/$fileName").readText(), Array<QPost>::class.java).filter { it.Timestamp().isAfter(startTime) }
    }

    fun ImportNews(fileName: String) : List<News> {
        println("Importing news from $importDirectory/$fileName...")
        return Gson().fromJson(File("$importDirectory/$fileName").readText(), Array<News>::class.java).filter { it.Timestamp().isAfter(startTime)}
    }
}