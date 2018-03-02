package models.events

import java.security.MessageDigest
import java.time.ZonedDateTime
import javax.xml.bind.DatatypeConverter

abstract class Event(
        @Transient var UID : String = ""
) {
    companion object {
        @Transient val MD5: MessageDigest = MessageDigest.getInstance("MD5")
    }
    abstract fun Datasets() : List<String>
    abstract fun Type() : String
    abstract fun ID() : String
    abstract fun Board(): String
    abstract fun Trip() : String
    abstract fun Link() : String
    abstract fun FindReferences()
    abstract fun ReferenceID() : String
    abstract fun References() : List<String>
    abstract fun Timestamp() : ZonedDateTime
    abstract fun RawTimestamp() : String
    abstract fun Subject() : String
    abstract fun Text() : String
    abstract fun Images() : List<Pair<String?, String?>>
}
