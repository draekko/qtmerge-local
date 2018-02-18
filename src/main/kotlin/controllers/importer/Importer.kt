package controllers.importer

import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

abstract class Importer(protected val importDirectory : String) {
    val startTime : ZonedDateTime = ZonedDateTime.of(2017, 10, 28, 0, 0, 0, 0, ZoneId.of("US/Eastern"))
    //val startTime : ZonedDateTime = ZonedDateTime.of(2017, 11, 25, 0, 0, 0, 0, ZoneId.of("US/Eastern"))
   lateinit var latestFile : File
    init {
        val dir = File(importDirectory)
        if(dir.isDirectory) {
            latestFile = dir.listFiles().sortedBy { it.lastModified() }.last()
        }
    }
}