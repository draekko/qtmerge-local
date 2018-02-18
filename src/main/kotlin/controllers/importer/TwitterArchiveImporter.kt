package controllers.importer

import com.google.gson.Gson
import models.events.TweetEvent

class TwitterArchiveImporter(importDirectory : String) : Importer(importDirectory) {
    fun ImportLatest() : List<TweetEvent> {
        println("Importing tweets from ${latestFile.absolutePath}...")
        return Gson().fromJson(latestFile.readText(), Array<TweetEvent>::class.java).filter { it.Timestamp().isAfter(startTime) }
    }
}