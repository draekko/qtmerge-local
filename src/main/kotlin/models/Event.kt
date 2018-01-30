package models

import java.time.ZonedDateTime
import java.util.*

abstract class Event(
        var UID : String = UUID.randomUUID().toString(),
        var Deltas : MutableList<Event> = arrayListOf()
) {
    abstract fun Type() : String
    abstract fun ID() : String
    abstract fun Trip() : String
    abstract fun Reference() : String
    abstract fun Timestamp() : ZonedDateTime
    abstract fun RawTimestamp() : String
    abstract fun Text() : String
    abstract fun Images() : List<Pair<String?, String?>>
}
