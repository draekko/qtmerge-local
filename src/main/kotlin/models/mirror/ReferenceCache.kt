package models.mirror

import controllers.mirror.Mirror
import settings.Settings
import java.time.Instant
import java.time.ZonedDateTime

data class ReferenceCache(
        val version : String = Settings.VERSION,
        val timestamp : Long = Instant.now().toEpochMilli(),
        val timestring : String = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), Settings.ZONEID).format(Settings.FORMATTER),
        val refs : MutableMap<String, Reference> = mutableMapOf()
) {
    enum class ReferenceType {
        BoardPost,
        SitePost,
        URL
    }

    data class Reference(
        val refid : String,
        val link : String,
        val type : String,
        val source : Mirror.Source,
        val board : String,
        val id : String,
        val references : MutableList<String?> = mutableListOf()
    )
}
