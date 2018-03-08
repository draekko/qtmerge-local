package models.events

import controllers.mirror.Mirror
import models.events.Event
import settings.Settings.Companion.ZONEID
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class NewsEvent(
        datasets : MutableList<String>,
        board : String,
        source : Mirror.Source,
        mirrorFile : String,
        var id: String,
        var url: String,
        var date : Long,
        var headline : String?,
        var description : String?,
        var imageUrl : String
) : Event("News", datasets, board, source, mirrorFile) {
    override fun ID(): String = id

    override fun ThreadID(): String = ""

    override fun Images(): List<Pair<String?, String?>> {
        return listOf(Pair(imageUrl, imageUrl))
    }

    override fun RawTimestamp(): String = date.toString()

    override fun Link(): String = url

    override fun ReferenceID(): String = url

    override fun FindReferences(): List<Event> {
        return emptyList()
    }

    override fun Subject(): String = headline ?: ""

    override fun Text(): String = headline ?: url ?: ""

    override fun Timestamp(): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), ZONEID)
    }

    override fun Trip(): String = ""
}
