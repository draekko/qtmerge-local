package settings

import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Settings {
    companion object {
        val VERSION = "2018.3-5"
        val MIRRORROOT = System.getProperty("user.dir") + File.separator + "mirror"
        val DATADIR = MIRRORROOT + File.separator + "data"
        val CACHEDIR = MIRRORROOT + File.separator + "cache"
        val ZONEID = ZoneId.of("US/Eastern")
        val STARTTIME : ZonedDateTime = ZonedDateTime.of(2017, 10, 28, 16, 44, 28, 0, ZONEID)
        val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
    }
}
