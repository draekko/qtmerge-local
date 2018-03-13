package models.events

import controllers.mirror.Mirror
import models.mirror.ReferenceCache
import java.security.MessageDigest
import java.time.ZonedDateTime
import javax.xml.bind.DatatypeConverter

abstract class Event(
        var type : String,
        var datasets : MutableList<String> = mutableListOf(),
        var board : String,
        var source : Mirror.Source,
        @Transient var mirrorFile : String,
        @Transient var UID : String = ""
) {
    companion object {
        val MD5: MessageDigest = MessageDigest.getInstance("MD5")

        fun MergeEvent(eventList: MutableList<Event>, event: Event, dataset : String) {
            val existingEvent = eventList.find { it.Board() == event.Board() && it.ID() == event.ID() && (it.Link() == event.Link() || it.Timestamp() == event.Timestamp()) }
            if(existingEvent == null) {
                eventList.add(event)
            } else {
                // TODO: also note any differences from existing event
                //      ThreadID
                //      Link
                //      Timestamp
                //      Name
                //      Trip
                //      Subject
                //      Text
                existingEvent.datasets.add(dataset)
            }
        }
    }

    fun Type(): String = type
    fun Datasets(): List<String> = datasets
    fun Board(): String = board
    fun Source() : Mirror.Source = source

    abstract fun ID() : String
    abstract fun ThreadID() : String
    abstract fun Trip() : String
    abstract fun Link() : String
    abstract fun FindReferences() : List<Pair<ReferenceCache.ReferenceType, String>>
    abstract fun ReferenceID() : String
    abstract fun Timestamp() : ZonedDateTime
    abstract fun RawTimestamp() : String
    abstract fun Subject() : String
    abstract fun Text() : String
    abstract fun Images() : List<Pair<String?, String?>>
}
