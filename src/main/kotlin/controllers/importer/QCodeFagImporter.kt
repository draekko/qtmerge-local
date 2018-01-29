package controllers.importer

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import models.QPost
import java.io.File

class QCodeFagImporter(importDirectory : String) : Importer(importDirectory) {
    fun Import(board : String, hasTripCode: Boolean, forum : String, fileName : String) : List<QPost> {
        println("Importing qposts from $importDirectory/$fileName...")
        return Gson().fromJson(File("$importDirectory/$fileName").readText(), Array<QPost>::class.java).filter { it.Timestamp().isAfter(startTime) }
    }
}