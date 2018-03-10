import controllers.mirror.*
import settings.Settings.Companion.FORMATTER
import settings.Settings.Companion.ZONEID
import java.time.ZonedDateTime

fun main(args: Array<String>) {
    QTMonitor().Monitor()
}

class QTMonitor {
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
            val startTime = ZonedDateTime.now(ZONEID).minusHours(48)
            println("Mirroring forward from ${startTime.format(FORMATTER)}")
            val mirrors = arrayListOf(
                    TwitterArchiveMirror("realDonaldTrump", startTime),
                    QCodeFagMirror("greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts"),
                    QCodeFagMirror("qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts"),
                    QCodeFagMirror("thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts"),
                    QCodeFagMirror("cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts"),
                    QCodeFagMirror("cbts", Mirror.Source.InfChan,"cbtsNonTrip8chanPosts"),
                    QCodeFagMirror("pol", Mirror.Source.InfChan, "polTrip8chanPosts"),
                    QCodeFagMirror("pol", Mirror.Source.FourChan, "pol4chanPosts"),
                    QAnonMapMirror("greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts"),
                    QAnonMapMirror("qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts"),
                    QAnonMapMirror("qresearch", Mirror.Source.InfChan, "qresearchNonTrip8chanPosts"),
                    QAnonMapMirror("thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts"),
                    QAnonMapMirror("cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts"),
                    QAnonMapMirror("cbts", Mirror.Source.InfChan, "cbtsNonTrip8chanPosts"),
                    QAnonMapMirror("pol", Mirror.Source.InfChan, "polTrip8chanPosts"),
                    QAnonMapMirror("pol", Mirror.Source.FourChan, "pol4chanPosts"),
                    TheStoryOfQMirror("greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts"),
                    TheStoryOfQMirror("qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts"),
                    TheStoryOfQMirror("qresearch", Mirror.Source.InfChan, "qresearchNonTrip8chanPosts"),
                    TheStoryOfQMirror("thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts"),
                    TheStoryOfQMirror("cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts"),
                    TheStoryOfQMirror("cbts", Mirror.Source.InfChan, "cbtsNonTrip8chanPosts"),
                    TheStoryOfQMirror("pol", Mirror.Source.InfChan, "polTrip8chanPosts"),
                    TheStoryOfQMirror("pol", Mirror.Source.FourChan, "pol4chanPosts"),
                    InfChMirror("greatawakening", startTime),
                    InfChMirror("qresearch", startTime)
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