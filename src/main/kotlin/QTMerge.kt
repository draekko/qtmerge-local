import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import controllers.importer.QCodeFagImporter
import controllers.mirror.InfChMirror
import controllers.mirror.Mirror
import controllers.mirror.TwitterArchiveMirror
import models.events.Event
import models.q.Abbreviations
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// TODO: Switch to Kotson?

fun main(args: Array<String>) {
    QTMerge()
}

class QTMerge(
    var mirrorLabel : String = "2018-02-15"
) {
    var events : MutableList<Event> = arrayListOf()

    companion object {
        val ZONEID = ZoneId.of("US/Eastern")
        val VERSION = "2018.2-6"
        val DATADIR = System.getProperty("user.dir") + File.separator + "data"
        val MIRRORDIR = System.getProperty("user.dir") + File.separator + "mirror"
        val RESULTDIR = System.getProperty("user.dir") + File.separator + "anonsw.github.io" + File.separator + "qtmerge"
        val STARTTIME : ZonedDateTime = ZonedDateTime.of(2017, 10, 28, 16, 44, 28, 0, ZoneId.of("US/Eastern"))
    }

    init {
        LoadSources()

        CalcOffsets()

        File(RESULTDIR).mkdirs()
        ExportHtml()
        //ExportJson()

        CalcQStats()
    }

    fun CalcQStats() {
        val days : MutableMap<Long, Long> = hashMapOf()
        var lastTime : ZonedDateTime? = null
        var totalPosts: Long = 0

        events.forEach {
            if(it.Type() == "Post") {
                totalPosts++
                if(lastTime == null) {
                    lastTime = it.Timestamp()
                } else {
                    val delta = ChronoUnit.DAYS.between(lastTime, it.Timestamp())
                    if(days.contains(delta)) {
                        days[delta] = days[delta]!! + 1
                    } else {
                        days[delta] = 1
                    }
                    lastTime = it.Timestamp()
                }
            }
        }

        println("Total QPosts: $totalPosts")
        days.toSortedMap().forEach {
            println("${it.key} ${it.value}")
        }
    }

    fun LoadSources() {
        val inputDirectory = MIRRORDIR + File.separator + mirrorLabel
        events.addAll(TwitterArchiveMirror(inputDirectory, "realDonaldTrump", STARTTIME).MirrorSearch())
        events.addAll(InfChMirror(inputDirectory, "pol", STARTTIME).MirrorSearch())
        events.addAll(InfChMirror(inputDirectory, "cbts", STARTTIME).MirrorSearch())
        events.addAll(InfChMirror(inputDirectory, "thestorm", STARTTIME).MirrorSearch())
        events.addAll(InfChMirror(inputDirectory, "greatawakening", STARTTIME).MirrorSearch())
        events.addAll(InfChMirror(inputDirectory, "qresearch", STARTTIME).MirrorSearch())

        // Capture qcodefag's data
        QCodeFagImporter("$DATADIR/QCodefag.github.io/data")
                .ImportQPosts("qcodefag", "pol", false, "4chan.org", "pol4chanPosts.json")
                .forEach { post ->
                    if(events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) } == null) {
                        events.add(post)
                    }
                }
        QCodeFagImporter("$DATADIR/QCodefag.github.io/data")
                .ImportQPosts("qcodefag", "cbts", false, "8ch.net", "cbtsNonTrip8chanPosts.json")
                .forEach { post ->
                    if(events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) } == null) {
                        events.add(post)
                    }
                }
        QCodeFagImporter("$DATADIR/QCodefag.github.io/data")
                .ImportQPosts("qcodefag", "cbts", true, "8ch.net", "cbtsTrip8chanPosts.json")
                .forEach { post ->
                    if(events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) } == null) {
                        events.add(post)
                    }
                }
        QCodeFagImporter("$DATADIR/QCodefag.github.io/data")
                .ImportQPosts("qcodefag", "pol", true, "8ch.net", "polTrip8chanPosts.json")
                .forEach { post ->
                    if(events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) } == null) {
                        events.add(post)
                    }
                }
        QCodeFagImporter("$DATADIR/QCodefag.github.io/data")
                .ImportQPosts("qcodefag", "thestorm", true, "8ch.net", "thestormTrip8chanPosts.json")
                .forEach { post ->
                    if(events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) } == null) {
                        events.add(post)
                    }
                }

        // Capture qanonmap's data
        QCodeFagImporter("$DATADIR/qanonmap.github.io/data")
                .ImportQPosts("qanonmap", "pol", false, "4chan.org", "pol4chanPosts.json")
                .forEach { post ->
                    if(events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) } == null) {
                        events.add(post)
                    }
                }
        QCodeFagImporter("$DATADIR/qanonmap.github.io/data")
                .ImportQPosts("qanonmap", "cbts", false, "8ch.net", "cbtsNonTrip8chanPosts.json")
                .forEach { post ->
                    if(events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) } == null) {
                        events.add(post)
                    }
                }
        QCodeFagImporter("$DATADIR/qanonmap.github.io/data")
                .ImportQPosts("qanonmap", "cbts", true, "8ch.net", "cbtsTrip8chanPosts.json")
                .forEach { post ->
                    if(events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) } == null) {
                        events.add(post)
                    }
                }
        QCodeFagImporter("$DATADIR/qanonmap.github.io/data")
                .ImportQPosts("qanonmap", "pol", true, "8ch.net", "polTrip8chanPosts.json")
                .forEach { post ->
                    if(events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) } == null) {
                        events.add(post)
                    }
                }
        QCodeFagImporter("$DATADIR/qanonmap.github.io/data")
                .ImportQPosts("qanonmap", "thestorm", true, "8ch.net", "thestormTrip8chanPosts.json")
                .forEach { post ->
                    if(events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) } == null) {
                        events.add(post)
                    }
                }
        QCodeFagImporter("$DATADIR/qanonmap.github.io/data")
                .ImportQPosts("qanonmap", "greatawakening", true, "8ch.net", "greatawakeningTrip8chanPosts.json")
                .forEach { post ->
                    if(events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) } == null) {
                        events.add(post)
                    }
                }
        QCodeFagImporter("$DATADIR/qanonmap.github.io/data")
                .ImportQPosts("qanonmap", "qresearch", true, "8ch.net", "qresearchTrip8chanPosts.json")
                .forEach { post ->
                    if(events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) } == null) {
                        events.add(post)
                    }
                }
        QCodeFagImporter("$DATADIR/qanonmap.github.io/data")
                .ImportQPosts("qanonmap", "qresearch", true, "8ch.net", "qresearchNonTrip8chanPosts.json")
                .forEach { post ->
                    if(events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) } == null) {
                        events.add(post)
                    }
                }

        events.sortBy { it.Timestamp().toEpochSecond() }

        // Initialize base class due to Gson ignoring Kotlin default constructor values, also enumerate id's
        events.forEachIndexed { index, event ->
            event.UID = index.toString()
            event.Deltas = arrayListOf()
        }

        println("Total Events: ${events.size}")
    }


    fun CalcOffsets() {
        val qq : MutableList<Event> = arrayListOf()
        var resetQQ = false
        events.forEach { event ->
            if(event.Type() == "Post") {
                resetQQ = true
            } else if(event.Type() == "Tweet") {
                if(resetQQ) { qq.clear(); resetQQ = false }
                if(qq.isNotEmpty()) {
                    event.Deltas.addAll(qq.reversed())
                }
                qq.add(event)
            }
        }
    }

    fun ExportJson() {
        val gson = GsonBuilder().setPrettyPrinting().create()
        File("$RESULTDIR/qtmerge-pretty.json").writeText(gson.toJson(events.sortedBy { it.Timestamp().toEpochSecond() }))
        File("$RESULTDIR/qtmerge.json").writeText(Gson().toJson(events.sortedBy { it.Timestamp().toEpochSecond() }))
    }

    fun ExportHtml() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
        val out = File("$RESULTDIR/index.html").outputStream().bufferedWriter()
        out.append("""<!doctype html>
            |<html lang="en">
            |   <head>
            |       <meta charset="utf-8">
            |       <title>qtmerge v$VERSION</title>
            |       <link rel="stylesheet" href="../styles/reset.css">
            |       <link rel="stylesheet" href="../styles/screen.css">
            |       <link rel="stylesheet" href="qtmerge.css">
            |       <link rel="stylesheet" href="../libs/jquery-ui-1.12.1/jquery-ui.min.css">
            |       <script type="text/javascript" src="../scripts/jquery-3.3.1.min.js"></script>
            |       <script type="text/javascript" src="../libs/jquery-ui-1.12.1/jquery-ui.min.js"></script>
            |       <script type="text/javascript" src="../scripts/jquery.jeditable.mini.js"></script>
            |       <script type="text/javascript" src="../scripts/jquery.highlight-5.js"></script>
            |       <script type="text/javascript" src="../scripts/underscore-min.js"></script>
            |       <script type="text/javascript" src="../scripts/moment.min.js"></script>
            |       <script type="text/javascript" src="../scripts/moment-timezone-with-data-2012-2022.js"></script>
            |       <!--
            |         -- qtmerge v$VERSION
            |         -- http://anonsw.github.com/qtmerge/
            |         -->
            |   </head>
            |   <body>
            |   <div id="header">
            |   <p class="timestamp">Version: $VERSION &mdash; Last Updated: ${ZonedDateTime.now(ZoneId.of("US/Eastern")).format(formatter)}</p>
            |   <p class="downloads">
            |       Datasets:
            |       <a href="http://qanonposts.com/">qcodefag</a> |
            |       <a href="http://qanonmap.github.io/">qanonmap</a> |
            |       <a href="http://anonsw.github.io/">anonsw</a> |
            |       <a href="http://trumptwitterarchive.com/">trumptwitterarchive</a> ||
            |       <input id="openScratchPadButton" type="button" value="Open Scratch Pad" disabled="disabled"> <small>(Click posts to add/remove)</small>
            |   </p>
            |   </div>
            |""".trimMargin())

        out.append("""<table>
            |   <tr>
            |       <th>Time (Count)<br>[ID]</th>
            |       <th>Trip (Board)<br>[Dataset]</th>
            |       <th>Link</th>
            |       <th>Text</th>
            |   </tr>
            |""".trimMargin())

        val ref = Regex(""">>(\d+)""")
        var qpostCount = events.count { it.Type() == "Post" } + 1
        var tweetCount = events.count { it.Type() == "Tweet" } + 1
        var isQPost = false
        events.reversed().forEachIndexed { index, event ->
            when(event.Type()) {
                "Post" -> { qpostCount--; isQPost = true }
                "Tweet" -> { tweetCount--; isQPost = false }
            }
            var count = 0
            out.appendln("  <tr class=\"event\" id=\"${event.UID}\" data-timestamp='${MakeJSONTimestamp(event.Timestamp())}'>")
            out.appendln("      <td class=\"e-timestamp\">${event.Timestamp().format(formatter)}<br><span class=\"count\">(${if(isQPost) "Post #<b>$qpostCount</b>" else "Tweet #<b>$tweetCount</b>"})</span><br><span class=\"id\">[${event.ID()}]</span></td>")
            out.appendln("      <td class=\"e-trip\">${event.Trip()}${if(event.Board().isNotEmpty()) "<br><span class=\"board\">(${event.Board()})</span>" else ""}<br><span class=\"dataset\">[${event.Dataset()}]</span></td>")
            out.appendln("      <td class=\"e-type\"><a href=\"${event.Link()}\">${event.Type()}</a></td>")
            var images = ""
            event.Images().forEach {
                if (it.first != null) {
                    images = """<a href="${it.first}">"""
                }
                if (it.second != null) {
                    images += it.second
                } else if (it.first != null) {
                    images += it.first
                }
                if (it.first != null) {
                    images += "</a>"
                }
            }
            if(images.isNotEmpty() && event.Text().isNotEmpty()) {
                images += "<br>"
            }
            var text = event.Text() //escapeHTML(event.Text())
            var refurl = event.Link().replaceAfter("#", "")
            if(!refurl.endsWith("#")) {
                refurl += "#"
            }
            text = text.replace(ref, { match ->
                "<a href=\"$refurl${match.groups[1]!!.value}\">${match.value}</a>"
            })
            val boxRegex = Regex("""\[([^\]]+)\]""")
            text = text.replace(boxRegex, { matchResult ->
                "[<span class=\"boxed\">${matchResult.groupValues[1]}</span>]"
            })
            val capsRegex = Regex("""\b((?<!@)[A-Z]{2,})+\b""")
            text = text.replace(capsRegex, { matchResult ->
                //if(Abbreviations.dict.containsKey(matchResult.groupValues[1])) {
                  //  matchResult.groupValues[1]
                //} else {
                    "<span class=\"caps\">${matchResult.groupValues[1]}</span>"
                //}
            })
            val abbrRegex = Regex("""\b(${Abbreviations.dict.keys.joinToString("|")})\b""")
            text = text.replace(abbrRegex, { matchResult ->
                "<span class=\"abbr\" title=\"${Abbreviations.dict[matchResult.groupValues[1]]}\">${matchResult.groupValues[1]}</span>"
            })
            // TODO: known acronyms
            out.appendln("        <td class=\"e-text\">$images$text</td>")
            out.appendln("    </tr>")
        }

        out.appendln("</table>")
        out.appendln("""
            |<div id="scratchPad">
            |<div id="scratchHeader">
            |<div id="scratchActions">
            |<input type="button" value="Clear Scratch Pad" onclick="removeScratchEvents();">
            |<input type="button" value="Generate Map" disabled="disabled">
            |</div>
            |<p id="scratchVersion">qtmerge v$VERSION</p>
            |<p id="scratchTimestamp"></p>
            |</div>
            |<table id="scratchTable">
            |   <tr>
            |       <th>#</th>
            |       <th>Time (Count)<br>[ID]</th>
            |       <th>Trip (Board)<br>[Dataset]</th>
            |       <th>Link</th>
            |       <th>Text</th>
            |   </tr>
            |</table>
            |<div id="scratchTimes"></div>
            |</div>
            |""".trimMargin())

        out.append("""<script type="text/javascript" src="qtmerge.js"></script>""")
        out.append("</body></html>")
        out.close()
    }

    fun MakeJSONTimestamp(timeStamp: ZonedDateTime) : JsonObject {
        val ob = JsonObject()
        ob.addProperty("unix", timeStamp.toEpochSecond())
        ob.addProperty("hour", timeStamp.hour)
        ob.addProperty("min", timeStamp.minute)
        ob.addProperty("sec", timeStamp.second)
        return ob
    }
}
