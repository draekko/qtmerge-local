import controllers.mirror.*
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {
    QTMirror()
}

class QTMirror(
    mirrorLabel : String = "2018-02-15"
) {
    companion object {
        val ZONEID = ZoneId.of("US/Eastern")
        val VERSION = "2018.2-1"
        val DATADIR = System.getProperty("user.dir") + File.separator + "mirror"
        val STARTTIME : ZonedDateTime = ZonedDateTime.of(2017, 10, 28, 0, 0, 0, 0, ZoneId.of("US/Eastern"))
        val DATEFORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
    }

    init {
        val outputDirectory = DATADIR + File.separator + mirrorLabel
        val mirrors = arrayListOf(
                /*
            cbtsNonTrip8chanPosts.json
            cbtsTrip8chanPosts.json
            greatawakeningTrip8chanPosts.json
            pol4chanPosts.json
            polTrip8chanPosts.json
            qresearchTrip8chanPosts.json
            thestormTrip8chanPosts.json
                */

            TwitterArchiveMirror(outputDirectory, "realDonaldTrump"),
            //TwitterArchiveMirror(outputDirectory, "hillaryclinton")
            //TwitterArchiveMirror(outputDirectory, "senatorsessions")
            //TwitterArchiveMirror(outputDirectory, "POTUS")
            //TwitterArchiveMirror(outputDirectory, "genflynn")
            //TwitterArchiveMirror(outputDirectory, "govpencein")
            //TwitterArchiveMirror(outputDirectory, "vp")
            //TwitterArchiveMirror(outputDirectory, "repmikepompeo")
            //TwitterArchiveMirror(outputDirectory, "seanhannity")
            QCodeFagMirror(outputDirectory, "cbtsNonTrip8chanPosts"),
            QCodeFagMirror(outputDirectory, "cbtsTrip8chanPosts"),
            QCodeFagMirror(outputDirectory, "greatawakeningTrip8chanPosts"),
            QCodeFagMirror(outputDirectory, "pol4chanPosts"),
            QCodeFagMirror(outputDirectory, "polTrip8chanPosts"),
            QCodeFagMirror(outputDirectory, "qresearchTrip8chanPosts"),
            QCodeFagMirror(outputDirectory, "thestormTrip8chanPosts"),
            QCodeFagMirror(outputDirectory, "cbtsNonTrip8chanPosts"),
            QAnonMapMirror(outputDirectory, "cbtsTrip8chanPosts"),
            QAnonMapMirror(outputDirectory, "greatawakeningTrip8chanPosts"),
            QAnonMapMirror(outputDirectory, "pol4chanPosts"),
            QAnonMapMirror(outputDirectory, "polTrip8chanPosts"),
            QAnonMapMirror(outputDirectory, "qresearchNonTrip8chanPosts"),
            QAnonMapMirror(outputDirectory, "qresearchTrip8chanPosts"),
            QAnonMapMirror(outputDirectory, "thestormTrip8chanPosts"),
            TheStoryOfQMirror(outputDirectory, "cbtsNonTrip8chanPosts"),
            TheStoryOfQMirror(outputDirectory, "cbtsTrip8chanPosts"),
            TheStoryOfQMirror(outputDirectory, "greatawakeningTrip8chanPosts"),
            TheStoryOfQMirror(outputDirectory, "pol4chanPosts"),
            TheStoryOfQMirror(outputDirectory, "polTrip8chanPosts"),
            TheStoryOfQMirror(outputDirectory, "qresearchTrip8chanPosts"),
            TheStoryOfQMirror(outputDirectory, "thestormTrip8chanPosts"),
            InfChMirror(outputDirectory, "greatawakening"),
            InfChMirror(outputDirectory, "qresearch"),
            InfChMirror(outputDirectory, "thestorm"),
            InfChMirror(outputDirectory, "cbts"),
            InfChMirror(outputDirectory, "pol", STARTTIME) //, ZonedDateTime.of(2018, 1, 5, 0, 0, 0, 0, ZONEID))
            //FourChanMirror(outputDirectory, "pol", STARTTIME, ZonedDateTime.of(2017, 12, 14, 0, 0, 0, 0, ZONEID))
            //TwitterMirror(outputDirectory, "JulianAssange")
            //TwitterMirror(outputDirectory, "Wikileaks")
            //TwitterMirror(outputDirectory, "Snowden")
            //TwitterMirror(outputDirectory, "Snowden")
        )

        // Mirror post data first
        println("\nMirroring Posts")
        mirrors.forEach {
            println(">> $it")
            it.Mirror()
        }

        // Now mirror references
        println("\nMirroring References")
        mirrors.forEach {
            println(">> $it")
            it.MirrorReferences()
        }

        // TODO: generate html report of missing threads/posts/files and files where checksum is mismatched

        // TODO: generate master archive

        // TODO: generate q-only archive (q posts, posts that directly reference q posts)

        // TODO: generate q-extended archive (q posts, threads and posts that directly or indirectly reference q posts)

        // TODO: generate text-only archive
    }
}