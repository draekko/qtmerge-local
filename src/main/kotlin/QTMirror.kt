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
                TwitterArchiveMirror(outputDirectory, "realDonaldTrump"),
                /*
                TwitterArchiveMirror(mirrorDirectory, "hillaryclinton"),
                TwitterArchiveMirror(mirrorDirectory, "senatorsessions"),
                TwitterArchiveMirror(mirrorDirectory, "POTUS"),
                TwitterArchiveMirror(mirrorDirectory, "genflynn"),
                TwitterArchiveMirror(mirrorDirectory, "govpencein"),
                TwitterArchiveMirror(mirrorDirectory, "vp"),
                TwitterArchiveMirror(mirrorDirectory, "repmikepompeo"),
                TwitterArchiveMirror(mirrorDirectory, "seanhannity"),
                */
                QCodeFagMirror(outputDirectory, "greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts"),
                QCodeFagMirror(outputDirectory, "qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts"),
                QCodeFagMirror(outputDirectory, "thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts"),
                QCodeFagMirror(outputDirectory, "cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts"),
                QCodeFagMirror(outputDirectory, "cbts", Mirror.Source.InfChan,"cbtsNonTrip8chanPosts"),
                QCodeFagMirror(outputDirectory, "pol", Mirror.Source.InfChan, "polTrip8chanPosts"),
                QCodeFagMirror(outputDirectory, "pol", Mirror.Source.FourChan, "pol4chanPosts"),
                QAnonMapMirror(outputDirectory, "greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts"),
                QAnonMapMirror(outputDirectory, "qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts"),
                QAnonMapMirror(outputDirectory, "qresearch", Mirror.Source.InfChan, "qresearchNonTrip8chanPosts"),
                QAnonMapMirror(outputDirectory, "thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts"),
                QAnonMapMirror(outputDirectory, "cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts"),
                QAnonMapMirror(outputDirectory, "cbts", Mirror.Source.InfChan, "cbtsNonTrip8chanPosts"),
                QAnonMapMirror(outputDirectory, "pol", Mirror.Source.InfChan, "polTrip8chanPosts"),
                QAnonMapMirror(outputDirectory, "pol", Mirror.Source.FourChan, "pol4chanPosts"),
                TheStoryOfQMirror(outputDirectory, "greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts"),
                TheStoryOfQMirror(outputDirectory, "qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts"),
                TheStoryOfQMirror(outputDirectory, "qresearch", Mirror.Source.InfChan, "qresearchNonTrip8chanPosts"),
                TheStoryOfQMirror(outputDirectory, "thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts"),
                TheStoryOfQMirror(outputDirectory, "cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts"),
                TheStoryOfQMirror(outputDirectory, "cbts", Mirror.Source.InfChan, "cbtsNonTrip8chanPosts"),
                TheStoryOfQMirror(outputDirectory, "pol", Mirror.Source.InfChan, "polTrip8chanPosts"),
                TheStoryOfQMirror(outputDirectory, "pol", Mirror.Source.FourChan, "pol4chanPosts"),
                InfChMirror(outputDirectory, "greatawakening"),
                InfChMirror(outputDirectory, "qresearch"),
                InfChMirror(outputDirectory, "thestorm", STARTTIME, ZonedDateTime.of(2018, 1, 15, 0, 0, 0, 0, ZONEID)),
                InfChMirror(outputDirectory, "cbts", STARTTIME, ZonedDateTime.of(2018, 1, 15, 0, 0, 0, 0, ZONEID)),
                InfChMirror(outputDirectory, "pol", STARTTIME, ZonedDateTime.of(2018, 2, 15, 0, 0, 0, 0, ZONEID)),
                FourChanMirror(outputDirectory, "pol", STARTTIME, ZonedDateTime.of(2017, 12, 14, 0, 0, 0, 0, ZONEID))
                //TwitterMirror(mirrorDirectory, "JulianAssange")
                //TwitterMirror(mirrorDirectory, "Wikileaks")
                //TwitterMirror(mirrorDirectory, "Snowden")
                //TwitterMirror(mirrorDirectory, "Snowden")
        )

        // Mirror post data first
        println("\nMirroring Posts")
        mirrors.forEach {
            it.Mirror()
        }

        // Now mirror references
        println("\nMirroring References")
        mirrors.forEach {
            it.MirrorReferences()
        }

        // TODO: generate html report of missing threads/posts/files and files where checksum is mismatched

        // TODO: generate master archive

        // TODO: generate q-only archive (q posts, posts that directly reference q posts)

        // TODO: generate q-extended archive (q posts, threads and posts that directly or indirectly reference q posts)

        // TODO: generate text-only archive
    }
}