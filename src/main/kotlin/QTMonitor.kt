import controllers.mirror.*
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {
    QTMonitor().Monitor()
}

class QTMonitor(
        mirrorLabel : String = "2018-02-15",
        val outputDirectory : String = DATADIR + File.separator + mirrorLabel
) {
    companion object {
        val ZONEID = ZoneId.of("US/Eastern")
        val VERSION = "2018.2-1"
        val DATADIR = System.getProperty("user.dir") + File.separator + "mirror"
        val STARTTIME : ZonedDateTime = ZonedDateTime.now(ZoneId.of("US/Eastern")).minusHours(24)
        val DATEFORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
        val ACTIVEQTRIPS = listOf("!UW.yye1fxo")
    }

    fun Monitor() {
        val mirrors = arrayListOf(
            TwitterArchiveMirror(outputDirectory, "realDonaldTrump"),
            InfChMirror(outputDirectory, "greatawakening"),
            InfChMirror(outputDirectory, "qresearch")
        )

        while(true) {
            // Mirror post data first
            println("\nMirroring Posts")
            mirrors.forEach {
                println(">> $it")
                try {
                    it.Mirror()
                } catch(e : Exception) {
                    println(e)
                }
            }

            println("\nSleeping...")
            Thread.sleep(60000)
        }
    }
}