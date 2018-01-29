package models

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class QPostImage(
        var url : String,
        var filename : String
)

data class QPost(
        var id : String,
        var userId : String,
        var timestamp : Long,
        var subject : String,
        var name : String,
        var email : String,
        var trip : String,
        var text : String?,
        var images : Array<QPostImage>,
        var threadId : String,
        var source : String,
        var link : String,
        var references : Array<QPost>
) : Event() {
    override fun Type(): String = "QPost"

    override fun ID(): String {
        return userId
    }

    override fun Trip(): String {
        return trip
    }

    override fun Reference(): String {
        return link
    }

    override fun RawTimestamp(): String {
        return timestamp.toString()
    }

    override fun Timestamp(): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("US/Eastern"))
    }

    override fun Text(): String {
        return text?:""
    }
}