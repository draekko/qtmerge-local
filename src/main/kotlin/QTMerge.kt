import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import controllers.importer.QCodeFagImporter
import controllers.importer.TwitterArchiveImporter
import models.Event
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.util.CellRangeAddress
import java.io.File
import java.io.FileOutputStream
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

// TODO: Switch to Kotson?

val DATADIR = System.getProperty("user.dir") + "/data"
val RESULTDIR = System.getProperty("user.dir") + "/anonsw.github.io/qtmerge"

fun main(args: Array<String>) {
    QTMerge()
}

class QTMerge {
    var events : MutableList<Event> = arrayListOf()

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
            if(it.Type() == "QPost") {
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
        //events.addAll(TwitterArchiveImporter("$DATADIR/Trump").ImportLatest())
        events.addAll(TwitterArchiveImporter("$DATADIR/Trump/2017").ImportLatest())
        events.addAll(TwitterArchiveImporter("$DATADIR/Trump/2018").ImportLatest())
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").ImportQPosts("cbts", false, "8ch.net", "cbtsNonTrip8chanPosts.json"))
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").ImportQPosts("cbts", true, "8ch.net", "cbtsTrip8chanPosts.json"))
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").ImportQPosts("greatawakening", true, "8ch.net", "greatawakeningTrip8chanPosts.json"))
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").ImportQPosts("pol", false, "4chan.org", "pol4chanPosts.json"))
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").ImportQPosts("pol", true, "8ch.net", "polTrip8chanPosts.json"))
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").ImportQPosts("qresearch", true, "8ch.net", "qresearchTrip8chanPosts.json"))
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").ImportQPosts("thestorm", true, "8ch.net", "thestormTrip8chanPosts.json"))
        //events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").ImportNews("news.json"))

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
            if(event.Type() == "QPost") {
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
        val out = File("$RESULTDIR/qtmerge.html").outputStream().bufferedWriter()
        out.append("""<!doctype html>
            |<html lang="en">
            |   <head>
            |       <meta charset="utf-8">
            |       <title>qtmerge</title>
            |       <link rel="stylesheet" href="../styles/screen.css">
            |       <link rel="stylesheet" href="qtmerge.css">
            |       <link rel="stylesheet" href="../libs/jquery-ui-1.12.1/jquery-ui.min.css">
            |       <script type="text/javascript" src="../scripts/jquery-3.3.1.min.js"></script>
            |       <script type="text/javascript" src="../libs/jquery-ui-1.12.1/jquery-ui.min.js"></script>
            |   </head>
            |   <body>
            |   <div id="header">
            |   <p class="timestamp">Last Updated: ${ZonedDateTime.now(ZoneId.of("US/Eastern")).format(formatter)}</p>
            |   <p class="downloads">
            |       Sources:
            |       <a href="http://qcodefag.github.io/">Q Posts</a> (qcodefag.github.io) |
            |       <a href="http://trumptwitterarchive.com/">Trump Tweets</a> (trumptwitterarchive.com) ||
            |       <input id="openScratchPadButton" type="button" value="Open Scratch Pad" disabled="disabled"> <small>(Click posts to add/remove)</small>
            |   </p>
            |   </div>
            |""".trimMargin())

        out.append("""<table>
            |   <tr>
            |       <th>Time</th>
            |       <th>Trip (ID) [Board]</th>
            |       <th>Link</th>
            |       <th>Text</th>
            |   </tr>
            |""".trimMargin())

        val ref = Regex(""">>(\d+)""")
        events.reversed().forEach {
            out.appendln("  <tr class=\"event\" id=\"${it.UID}\" data-timestamp='${MakeJSONTimestamp(it.Timestamp())}'>")
            out.appendln("      <td class=\"e-timestamp\">${it.Timestamp().format(formatter)}</td>")
            out.appendln("      <td class=\"e-trip\">${it.Trip()}<br>(${it.ID()})${if(it.Board().isNotEmpty()) "<br>[${it.Board()}]" else ""}</td>")
            out.appendln("      <td class=\"e-type\"><a href=\"${it.Reference()}\">${it.Type()}</a></td>")
            var images = ""
            it.Images().forEach {
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
            if(images.isNotEmpty() && it.Text().isNotEmpty()) {
                images += "<br>"
            }
            var text = it.Text()
            var refurl = it.Reference().replaceAfter("#", "")
            if(!refurl.endsWith("#")) {
                refurl += "#"
            }
            text = text.replace(ref, { match ->
                "<a href=\"$refurl${match.groups[1]!!.value}\">${match.value}</a>"
            })
            out.appendln("        <td class=\"e-text\">$images$text</td>")
            out.appendln("    </tr>")
        }

        out.appendln("</table>")
        out.appendln("""
            |<div id="scratchPad">
            |<table id="scratchTable">
            |   <tr>
            |       <th>#</th>
            |       <th>Time</th>
            |       <th>Trip (ID) [Board]</th>
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
