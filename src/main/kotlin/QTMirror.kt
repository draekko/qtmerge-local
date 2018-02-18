import controllers.mirror.FourChanMirror
import controllers.mirror.InfChMirror
import controllers.mirror.TwitterArchiveMirror
import controllers.mirror.TwitterMirror
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

        TwitterArchiveMirror(outputDirectory, "realDonaldTrump").Mirror()
//        TwitterArchiveMirror(outputDirectory, "hillaryclinton").Mirror()
//        TwitterArchiveMirror(outputDirectory, "senatorsessions").Mirror()
//        TwitterArchiveMirror(outputDirectory, "POTUS").Mirror()
//        TwitterArchiveMirror(outputDirectory, "genflynn").Mirror()
//        TwitterArchiveMirror(outputDirectory, "govpencein").Mirror()
//        TwitterArchiveMirror(outputDirectory, "vp").Mirror()
//        TwitterArchiveMirror(outputDirectory, "repmikepompeo").Mirror()
//        TwitterArchiveMirror(outputDirectory, "seanhannity").Mirror()
        InfChMirror(outputDirectory, "greatawakening").Mirror()
        InfChMirror(outputDirectory, "qresearch").Mirror()
        InfChMirror(outputDirectory, "thestorm").Mirror()
        InfChMirror(outputDirectory, "cbts").Mirror()
        InfChMirror(outputDirectory, "pol", STARTTIME).Mirror() //, ZonedDateTime.of(2018, 1, 5, 0, 0, 0, 0, ZONEID))
        //FourChanMirror(outputDirectory, "pol", STARTTIME, ZonedDateTime.of(2017, 12, 14, 0, 0, 0, 0, ZONEID)).Mirror()

        //TwitterMirror(outputDirectory, "JulianAssange").Mirror()
        //TwitterMirror(outputDirectory, "Wikileaks").Mirror()
        //TwitterMirror(outputDirectory, "Snowden").Mirror()
        //TwitterMirror(outputDirectory, "Snowden").Mirror()

        /*
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").ImportQPosts("cbts", false, "8ch.net", "cbtsNonTrip8chanPosts.json"))
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").ImportQPosts("pol", false, "4chan.org", "pol4chanPosts.json"))
        */

        // TODO: generate html report of missing threads/posts/files and files where checksum is mismatched

        // TODO: generate master archive

        // TODO: generate q-only archive (q posts, posts that directly reference q posts)

        // TODO: generate q-extended archive (q posts, threads and posts that directly or indirectly reference q posts)

        // TODO: generate text-only archive
    }
}