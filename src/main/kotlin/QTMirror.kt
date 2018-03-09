import controllers.mirror.*
import settings.Settings.Companion.MIRRORDIR
import settings.Settings.Companion.STARTTIME
import settings.Settings.Companion.ZONEID
import java.io.File
import java.time.ZonedDateTime

fun main(args: Array<String>) {
    QTMirror()
}

class QTMirror(
    mirrorLabel : String = "2018-02-15"
) {
    init {
        val mirrorDirectory = MIRRORDIR + File.separator + mirrorLabel
        val cacheDirectory = MIRRORDIR + File.separator + "cache"
        val mirrors = arrayListOf(
                TwitterArchiveMirror(mirrorDirectory, "realDonaldTrump"),
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
                //QCodeFagNetMirror(mirrorDirectory, "pol", Mirror.Source.FourChan, "_allQPosts"),
                InfChMirror(mirrorDirectory, cacheDirectory, "greatawakening"),
                InfChMirror(mirrorDirectory, cacheDirectory, "qresearch"),
                InfChMirror(mirrorDirectory, cacheDirectory, "thestorm", STARTTIME, ZonedDateTime.of(2018, 1, 15, 0, 0, 0, 0, ZONEID)),
                InfChMirror(mirrorDirectory, cacheDirectory, "cbts", STARTTIME, ZonedDateTime.of(2018, 1, 15, 0, 0, 0, 0, ZONEID)),
                InfChMirror(mirrorDirectory, cacheDirectory, "pol", STARTTIME, ZonedDateTime.of(2018, 2, 15, 0, 0, 0, 0, ZONEID)),
                FourChanMirror(mirrorDirectory, cacheDirectory, "pol", STARTTIME, ZonedDateTime.of(2017, 12, 14, 0, 0, 0, 0, ZONEID))
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