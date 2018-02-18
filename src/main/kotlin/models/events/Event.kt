package models.events

import java.time.ZonedDateTime

abstract class Event(
        @Transient var UID : String = "",
        @Transient var Deltas : MutableList<Event> = arrayListOf()
) {
    abstract fun Host() : String
    abstract fun Type() : String
    abstract fun ID() : String
    abstract fun Board(): String
    abstract fun Trip() : String
    abstract fun Link() : String
    abstract fun ReferenceID() : String
    abstract fun References() : List<String>
    abstract fun Timestamp() : ZonedDateTime
    abstract fun RawTimestamp() : String
    abstract fun Text() : String
    abstract fun Images() : List<Pair<String?, String?>>
}
