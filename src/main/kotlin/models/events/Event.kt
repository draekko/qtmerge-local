package models.events

import controllers.mirror.Mirror
import java.security.MessageDigest
import java.time.ZonedDateTime
import javax.xml.bind.DatatypeConverter

abstract class Event(
        var datasets : MutableList<String> = mutableListOf(),
        var board : String,
        var source : Mirror.Source,
        var mirrorFile : String,
        var UID : String = ""
) {
    companion object {
        val MD5: MessageDigest = MessageDigest.getInstance("MD5")
    }

    fun Datasets(): List<String> = datasets
    fun Board(): String = board
    fun Source() : Mirror.Source = source

    abstract fun Type() : String
    abstract fun ID() : String
    abstract fun ThreadID() : String
    abstract fun Trip() : String
    abstract fun Link() : String
    abstract fun FindReferences() : List<Event>
    abstract fun ReferenceID() : String
    abstract fun References() : List<String>
    abstract fun Timestamp() : ZonedDateTime
    abstract fun RawTimestamp() : String
    abstract fun Subject() : String
    abstract fun Text() : String
    abstract fun Images() : List<Pair<String?, String?>>
}
