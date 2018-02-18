package models.events

import models.events.Event
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class NewsEvent(
        var id: String,
        var host : String,
        var url: String,
        var date : Long,
        var headline : String?,
        var description : String?,
        var imageUrl : String,
        private var references : MutableList<String>
) : Event() {
    override fun Host(): String = host

    override fun ID(): String = id

    override fun Board(): String = ""

    override fun Images(): List<Pair<String?, String?>> {
        return listOf(Pair(imageUrl, imageUrl))
    }

    override fun RawTimestamp(): String = date.toString()

    override fun Link(): String = url

    override fun ReferenceID(): String = "$host-$url-$id"

    override fun References(): List<String> = references

    override fun Text(): String = headline?:url?:""

    override fun Timestamp(): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.of("US/Eastern"))
    }

    override fun Trip(): String = ""

    override fun Type(): String = "News"
}