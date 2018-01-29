package models

import java.time.ZonedDateTime

abstract class Event {
    abstract fun Type() : String
    abstract fun ID() : String
    abstract fun Trip() : String
    abstract fun Reference() : String
    abstract fun Timestamp() : ZonedDateTime
    abstract fun RawTimestamp() : String
    abstract fun Text() : String
}