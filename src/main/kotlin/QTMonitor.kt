import controllers.mirror.*
import settings.Settings.Companion.FORMATTER
import settings.Settings.Companion.ZONEID
import java.io.File
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    QTMonitor().Monitor()
}

class QTMonitor {
    fun Monitor() {
        val countFile = File(System.getProperty("user.dir") + File.separator + ".qtmerge.cnt")
        val qtmerge = QTMerge(System.getProperty("user.dir") + File.separator + "anonsw.github.io-prod" + File.separator + "qtmerge")

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

            // Load previous count
            var count = 0
            println("\nLoading Previous QT Event Count")
            if(countFile.exists()) {
                count = countFile.readText().toInt()
            }
            println(">> $count events")

            // Mirror post data first
            println("\nMirroring Events")
            mirrors.forEach {
                try {
                    it.Mirror()
                } catch(e : Exception) {
                    println(e)
                }
            }

            // Count QT events after
            println("\nCounting QT Events")
            var postcount = 0
            qtmerge.mirrors.forEach {
                postcount += it.mirror.MirrorSearch(Mirror.SearchParameters(Mirror.SearchOperand.QT())).count()
            }
            println(">> $postcount events")

            // Run QT Merge and deploy if counts differ
            if(count != postcount) {
                println("\nEvent counts differ, merging")
                ProcessBuilder(listOf("git", "pull"))
                        .directory(File(System.getProperty("user.dir") + File.separator + "anonsw.github.io-prod/qtmerge/"))
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .start()
                        .waitFor(15, TimeUnit.MINUTES)
                qtmerge.ExportHtml()
                qtmerge.ExportJson()
                println("\nDeploying")
                ProcessBuilder(listOf("./deploy.sh", System.getProperty("user.dir") + File.separator + "anonsw.github.io-prod/qtmerge/"))
                    .directory(File(System.getProperty("user.dir")))
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start()
                    .waitFor(15, TimeUnit.MINUTES)

                countFile.writeText(postcount.toString())
            } else {
                println("\nEvent count unchanged.")
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

            println("\nSleeping...")
            Thread.sleep(60000)
        }
    }
}