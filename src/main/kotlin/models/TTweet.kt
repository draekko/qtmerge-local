package models

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class TTweet(
        val source : String,
        val id_str : String,
        val text : String?,
        val created_at : String,
        val retweet_count : Long,
        val in_reply_to_user_id_str : String?,
        val favorite_count : Long,
        val is_retweet : Boolean
) : Event() {
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy")
    }

    override fun Type(): String = "Tweet"

    override fun ID(): String = id_str

    override fun Board(): String = ""

    override fun Trip(): String {
        return "realDonaldTrump"
    }

    override fun Reference(): String {
        return "http://twitter.com/realDonaldTrump/status/$id_str"
    }

    override fun RawTimestamp(): String {
        return created_at
    }

    override fun Timestamp() : ZonedDateTime {
        return ZonedDateTime.parse(created_at, formatter).withZoneSameInstant(ZoneId.of("US/Eastern"))
    }

    override fun Text(): String {
        return text?:""
    }

    override fun Images(): List<Pair<String?, String?>> {
        return emptyList()
    }
}