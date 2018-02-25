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
    }

    fun Monitor() {
        val mirrors = arrayListOf(
            TwitterArchiveMirror(outputDirectory, "realDonaldTrump"),
            InfChMirror(outputDirectory, "greatawakening"),
            InfChMirror(outputDirectory, "qresearch")
        )

        while(true) {
            // Count QT events before
            println("\nCounting QT Events")
            var precount = 0
            mirrors.forEach {
                precount += it.MirrorSearch().count()
            }
            println(">> $precount events")

            // Mirror post data first
            println("\nMirroring Events")
            mirrors.forEach {
                println(">> $it")
                try {
                    it.Mirror()
                } catch(e : Exception) {
                    println(e)
                }
            }

            // Count QT events after
            println("\nCounting QT Events")
            var postcount = 0
            mirrors.forEach {
                postcount += it.MirrorSearch().count()
            }
            println(">> $postcount events")

            // Run QT Merge and deploy if counts differ
            if(precount != postcount) {
                println("\nEvent counts differ, merging")
                QTMerge()
                if(File("deploy.sh").exists()) {
                    println("\nDeploying")
                    // TODO: run delpoy.sh
                }
            }

            println("\nSleeping...")
            Thread.sleep(60000)
        }
    }
}