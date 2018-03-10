import controllers.mirror.*
import settings.Settings.Companion.STARTTIME
import settings.Settings.Companion.ZONEID
import java.time.ZonedDateTime

fun main(args: Array<String>) {
    QTMirror()
}

class QTMirror {
    init {
        val mirrors = arrayListOf(
                TwitterArchiveMirror("realDonaldTrump"),
                /*
                TwitterArchiveMirror("hillaryclinton"),
                TwitterArchiveMirror("senatorsessions"),
                TwitterArchiveMirror("POTUS"),
                TwitterArchiveMirror("genflynn"),
                TwitterArchiveMirror("govpencein"),
                TwitterArchiveMirror("vp"),
                TwitterArchiveMirror("repmikepompeo"),
                TwitterArchiveMirror("seanhannity"),
                */
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
                //QCodeFagNetMirror("pol", Mirror.Source.FourChan, "_allQPosts"),
                InfChMirror("greatawakening"),
                InfChMirror("qresearch"),
                InfChMirror("thestorm", STARTTIME, ZonedDateTime.of(2018, 1, 15, 0, 0, 0, 0, ZONEID)),
                InfChMirror("cbts", STARTTIME, ZonedDateTime.of(2018, 1, 15, 0, 0, 0, 0, ZONEID)),
                InfChMirror("pol", STARTTIME, ZonedDateTime.of(2018, 2, 15, 0, 0, 0, 0, ZONEID)),
                FourPlebsMirror("pol", STARTTIME, ZonedDateTime.of(2017, 12, 14, 0, 0, 0, 0, ZONEID))
                //TwitterMirror("JulianAssange")
                //TwitterMirror("Wikileaks")
                //TwitterMirror("Snowden")
                //TwitterMirror("Snowden")
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