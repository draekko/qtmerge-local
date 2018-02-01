package models

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class News(
        var id: String,
        var url: String,
        var date : Long,
        var headline : String?,
        var description : String?,
        var imageUrl : String
) : Event() {
    override fun ID(): String {
        return id
    }

    override fun Images(): List<Pair<String?, String?>> {
        return listOf(Pair(imageUrl, imageUrl))
    }

    override fun RawTimestamp(): String {
        return date.toString()
    }

    override fun Reference(): String {
        return url
    }

    override fun Text(): String {
        return headline?:url?:""
    }

    override fun Timestamp(): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.of("US/Eastern"))
    }

    override fun Trip(): String {
        return ""
    }

    override fun Type(): String {
        return "News"
    }
}