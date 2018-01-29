package controllers.importer

import com.google.gson.Gson
import models.TTweet
import java.time.chrono.ChronoZonedDateTime

class TwitterArchiveImporter(importDirectory : String) : Importer(importDirectory) {
    fun ImportLatest() : List<TTweet> {
        println("Importing tweets from ${latestFile.absolutePath}...")
        return Gson().fromJson(latestFile.readText(), Array<TTweet>::class.java).filter { it.Timestamp().isAfter(startTime) }
    }
}