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
        val mirrorDirectory: String = DATADIR + File.separator + mirrorLabel
) {
    companion object {
        val ZONEID = ZoneId.of("US/Eastern")
        val VERSION = "2018.2-1"
        val DATADIR = System.getProperty("user.dir") + File.separator + "mirror"
        val STARTTIME : ZonedDateTime = ZonedDateTime.now(ZoneId.of("US/Eastern")).minusHours(24)
        val DATEFORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
    }

    fun Monitor() {
        // Count QT events before
        /*
        println("\nCounting QT Events")
        var count = 0
        mirrors.forEach {
            count += it.MirrorSearch().count()
        }
        println(">> $count events")
        */

        while(true) {
            val mirrors = arrayListOf(
                    TwitterArchiveMirror(mirrorDirectory, "realDonaldTrump", STARTTIME),
                    QCodeFagMirror(mirrorDirectory, "greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts"),
                    QCodeFagMirror(mirrorDirectory, "qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts"),
                    QCodeFagMirror(mirrorDirectory, "thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts"),
                    QCodeFagMirror(mirrorDirectory, "cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts"),
                    QCodeFagMirror(mirrorDirectory, "cbts", Mirror.Source.InfChan,"cbtsNonTrip8chanPosts"),
                    QCodeFagMirror(mirrorDirectory, "pol", Mirror.Source.InfChan, "polTrip8chanPosts"),
                    QCodeFagMirror(mirrorDirectory, "pol", Mirror.Source.FourChan, "pol4chanPosts"),
                    QAnonMapMirror(mirrorDirectory, "greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts"),
                    QAnonMapMirror(mirrorDirectory, "qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts"),
                    QAnonMapMirror(mirrorDirectory, "qresearch", Mirror.Source.InfChan, "qresearchNonTrip8chanPosts"),
                    QAnonMapMirror(mirrorDirectory, "thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts"),
                    QAnonMapMirror(mirrorDirectory, "cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts"),
                    QAnonMapMirror(mirrorDirectory, "cbts", Mirror.Source.InfChan, "cbtsNonTrip8chanPosts"),
                    QAnonMapMirror(mirrorDirectory, "pol", Mirror.Source.InfChan, "polTrip8chanPosts"),
                    QAnonMapMirror(mirrorDirectory, "pol", Mirror.Source.FourChan, "pol4chanPosts"),
                    TheStoryOfQMirror(mirrorDirectory, "greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts"),
                    TheStoryOfQMirror(mirrorDirectory, "qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts"),
                    TheStoryOfQMirror(mirrorDirectory, "qresearch", Mirror.Source.InfChan, "qresearchNonTrip8chanPosts"),
                    TheStoryOfQMirror(mirrorDirectory, "thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts"),
                    TheStoryOfQMirror(mirrorDirectory, "cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts"),
                    TheStoryOfQMirror(mirrorDirectory, "cbts", Mirror.Source.InfChan, "cbtsNonTrip8chanPosts"),
                    TheStoryOfQMirror(mirrorDirectory, "pol", Mirror.Source.InfChan, "polTrip8chanPosts"),
                    TheStoryOfQMirror(mirrorDirectory, "pol", Mirror.Source.FourChan, "pol4chanPosts"),
                    InfChMirror(mirrorDirectory, "greatawakening", STARTTIME),
                    InfChMirror(mirrorDirectory, "qresearch", STARTTIME)
            )

            // Mirror post data first
            println("\nMirroring Events")
            mirrors.forEach {
                try {
                    it.Mirror()
                } catch(e : Exception) {
                    println(e)
                }
            }

            // Mirror post references second
            println("\nMirroring Event References")
            mirrors.forEach {
                try {
                    it.MirrorReferences()
                } catch(e : Exception) {
                    println(e)
                }
            }

            /*
              // Count QT events after
              println("\nCounting QT Events")
              var postcount = 0
              mirrors.forEach {
                  postcount += it.MirrorSearch().count()
              }
              println(">> $postcount events")

              // Run QT Merge and deploy if counts differ
              if(count != postcount) {
                  println("\nEvent counts differ, merging")
                  QTMerge(mirrorDirectory = System.getProperty("user.dir") + File.separator + "anonsw.github.io-prod" + File.separator + "qtmerge").ExportHtml()
                  println("\nDeploying")
                  // TODO: run delpoy.sh
              } else {
                  println("\nEvent count unchanged.")
              }
              count = postcount
              */

            println("\nSleeping...")
            Thread.sleep(60000)
        }
    }
}