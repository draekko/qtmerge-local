package models

import java.time.ZonedDateTime

abstract class Event(
        @Transient var UID : String = "",
        @Transient var Deltas : MutableList<Event> = arrayListOf()
) {
    abstract fun Type() : String
    abstract fun ID() : String
    abstract fun Board(): String
    abstract fun Trip() : String
    abstract fun Reference() : String
    abstract fun Timestamp() : ZonedDateTime
    abstract fun RawTimestamp() : String
    abstract fun Text() : String
    abstract fun Images() : List<Pair<String?, String?>>
}
