import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import controllers.importer.QCodeFagImporter
import controllers.mirror.InfChMirror
import controllers.mirror.TwitterArchiveMirror
import models.events.Event
import models.events.PostEvent
import models.q.Abbreviations
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// TODO: Switch to Kotson?

fun main(args: Array<String>) {
    QTMerge().ExportHtml()
}

class QTMerge(
    var mirrorLabel : String = "2018-02-15",
    var outputDirectory : String = System.getProperty("user.dir") + File.separator + "anonsw.github.io" + File.separator + "qtmerge"
) {
    var events : MutableList<Event> = arrayListOf()

    companion object {
        val ZONEID = ZoneId.of("US/Eastern")
        val VERSION = "2018.2-7"
        val DATADIR = System.getProperty("user.dir") + File.separator + "data"
        val MIRRORDIR = System.getProperty("user.dir") + File.separator + "mirror"
        val STARTTIME : ZonedDateTime = ZonedDateTime.of(2017, 10, 28, 16, 44, 28, 0, ZONEID)
        val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
    }

    init {
        LoadSources()
        File(outputDirectory).mkdirs()
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
        }

        println("Total Events: ${events.size}")
    }

    fun ExportJson() {
        val gson = GsonBuilder().setPrettyPrinting().create()
        File("$outputDirectory/qtmerge-pretty.json").writeText(gson.toJson(events.sortedBy { it.Timestamp().toEpochSecond() }))
        File("$outputDirectory/qtmerge.json").writeText(Gson().toJson(events.sortedBy { it.Timestamp().toEpochSecond() }))
    }

    fun ExportHtml() {
        val dsout = File("$outputDirectory/datasets.html").outputStream().bufferedWriter()
        dsout.append("""
            |<!doctype html>
            |<html lang="en">
            |   <head>
            |       <meta charset="utf-8">
            |       <title>qtmerge v$VERSION Datasets</title>
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
            |         -- https://anonsw.github.com/qtmerge/
            |         -->
            |   </head>
            |   <body style="margin-top :0 ">
            |   <h1>qtmerge v$VERSION Datasets</h1>
            |
            |   <h2>Raw Sources</h2>
            |   <p>The raw source data for posts and tweets come originally from these websites:</p>
            |   <ul>
            |       <li>https://4chan.org/</li>
            |       <li>https://8ch.net/</li>
            |       <li>https://twitter.com/</li>
            |   </ul>
            |
            |   <h2>Mirrored Data</h2>
            |   <p>Because the data at the raw source websites can be deleted/modified the following websites mirror or have mirrored data
            |       either in raw form or modified raw form:</p>
            |   <ul>
            |       <li>https://trumptwitterarchive.com/</li>
            |       <li>https://github.com/qcodefag/ &rarr; https://qcodefag.github.io/, https://qanonposts.com/</li>
            |       <li>https://github.com/qanonmap/ &rarr; https://qanonmap.github.io/, https://thestoryofq.com/ ?</li>
            |       <li>https://github.com/anonsw/ &rarr; https://anonsw.github.io/</li>
            |       <li>Others...?</li>
            |   </ul>
            |
            |   <h2>Formatted Data</h2>
            |   <p>To make data easier to consume various anon's have formatted it into spreadsheets, text files, PDF's and graphics. Many
            |   of these can be found by visiting the qresearch resource library: https://8ch.net/qresearch/res/4352.html</p>
            |   <p>Early posts by Q were made anonymously (without a trip code) and therefore required confirmed graphics (maps). Below is a
            |   summary of those confirmations (a work in progress):</p>
            |
            |   <dl>
            |       <dt>Q Map 1: Anonymous confirmation (confirmed with later tripped confirmations)</dt>
            |       <dd>
            |           Graphic 1: https://archive.4plebs.org/pol/thread/148136485/#148136656<br>
            |           Confirmation: Conf: https://archive.4plebs.org/pol/thread/148136485/#148139234 "Re-read complete crumb graphic (confirmed good)."
            |       </dd>
            |
            |       <dt>Q Map 2: Anonymous confirmation (confirmed with later tripped confirmations)</dt>
            |       <dd>
            |           Graphic 2: https://archive.4plebs.org/pol/thread/148146734/#148147343<br>
            |           Confirmation: https://archive.4plebs.org/pol/thread/148146734/#148148004 "Graphic confirmed."
            |       </dd>
            |
            |       <dt>Q Map 3: Tripped confirmation</dt>
            |       <dd>
            |           Graphic 3 + Confirmation: https://archive.4plebs.org/pol/thread/148777785/#148779656 "Attached gr[A]phic is correct."
            |       </dd>
            |
            |       <dt>Q Map 4: Tripped confirmation</dt>
            |       <dd>
            |           Graphic 4: https://archive.4plebs.org/pol/thread/148866159/#148872136<br>
            |           Confirmation: https://archive.4plebs.org/pol/thread/148866159/#148872500 "Confirmed.\nCorrect."
            |       </dd>
            |
            |       <dt>Q Maps 5-7: Tripped confirmations</dt>
            |       <dd>
            |           Graphic 5: https://archive.4plebs.org/pol/thread/149080733/#149083850  1?<br>
            |           Graphic 6: https://archive.4plebs.org/pol/thread/150171127/#150171513  2?<br>
            |           Graphic 7: https://archive.4plebs.org/pol/thread/150171127/#150171863  2?<br>
            |           Graphic 8: https://archive.4plebs.org/pol/thread/149920858/#149922836 (No new Q anonymous posts)<br>
            |           Graphic 9: https://archive.4plebs.org/pol/thread/150166775/#150170214 (No new Q anonymous posts)<br>
            |           Confirmation: https://archive.4plebs.org/pol/thread/150171127/#150172069 "QMAP 1/2 confirmed."<br>
            |           Confirmation: https://archive.4plebs.org/pol/thread/150171127/#150172817 "1&2 confirmed."<br>
            |       </dd>
            |
            |       <dt>Unconfirmed Q Map Set in recent qresearch OPs?</dt>
            |       <dd>
            |           Graphic: https://8ch.net/qresearch/res/387596.html#387700
            |       </dd>
            |   </dl>
            |
            |   <p>Currently qtmerge relies on graphics/maps 1-7 to mark non-tripped posts as confirmed from Q.</p>
            |
            |   <h2>Anonymous Q Posts</h2>
            |
            |   <p>The following table shows the confirmed status of Anonymous Q posts according to the datasets and methodology described above (red means not confirmed):</p>
            |   <table>
            |   <tr>
            |       <th>Time (Count)<br>[ID]</th>
            |       <th>Trip (Board)<br>[Datasets]</th>
            |       <th>Link</th>
            |       <th>Text</th>
            |   </tr>
            """.trimMargin())
        val out = File("$outputDirectory/index.html").outputStream().bufferedWriter()
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
            |         -- https://anonsw.github.com/qtmerge/
            |         -->
            |   </head>
            |   <body>
            |   <div id="header">
            |       <p class="timestamp">Version: $VERSION &mdash; Last Updated: ${ZonedDateTime.now(ZONEID).format(FORMATTER)}</p>
            |       <div class="datasets">
            |           Datasets (<a href="datasets.html">Detailed Information</a>):
            |           <a href="https://qanonposts.com/">qcodefag</a> |
            |           <a href="https://qanonmap.github.io/">qanonmap</a> |
            |           <a href="https://anonsw.github.io/">anonsw</a> |
            |           <a href="https://trumptwitterarchive.com/">trumptwitterarchive</a> |
            |           <a href="https://archive.4plebs.org/pol/thread/148136485/#148136656" title="148136656">Q Map 1</a> |
            |           <a href="https://archive.4plebs.org/pol/thread/148146734/#148147343" title="148147343">Q Map 2</a> |
            |           <a href="https://archive.4plebs.org/pol/thread/148777785/#148779656" title="148779656">Q Map 3</a> |
            |           <a href="https://archive.4plebs.org/pol/thread/148866159/#148872136" title="148872136">Q Map 4</a> |
            |           <a href="https://archive.4plebs.org/pol/thread/149080733/#149083850" title="149083850">Q Map 5</a> |
            |           <a href="https://archive.4plebs.org/pol/thread/150171127/#150171513" title="150171513">Q Map 6</a> |
            |           <a href="https://archive.4plebs.org/pol/thread/150171127/#150171863" title="150171863">Q Map 7</a> ||
            |       </div>
            |       <div class="menu">
            |           <input id="openScratchPadButton" type="button" value="Open Scratch Pad" disabled="disabled"> <small>(Click posts to add/remove)</small>
            |       </div>
            |   </div>
            |""".trimMargin())

        out.append("""<table>
            |   <tr>
            |       <th>Time (Count)<br>[ID]</th>
            |       <th>Trip (Board)<br>[Datasets]</th>
            |       <th>Link</th>
            |       <th>Text</th>
            |   </tr>
            |""".trimMargin())

        var qpostCount = events.count { it.Type() == "Post" } + 1
        var tweetCount = events.count { it.Type() == "Tweet" } + 1
        var isQPost = false
        events.reversed().forEachIndexed { index, event ->
            when(event.Type()) {
                "Post" -> {
                    qpostCount--
                    isQPost = true
                    if(event.Trip() == "Anonymous") {
                        dsout.appendln(MakeEventRow(event, qpostCount))
                    }
                }
                "Tweet" -> { tweetCount--; isQPost = false }
            }
            out.appendln(MakeEventRow(event, if(isQPost) qpostCount else tweetCount))
        }

        out.appendln("</table>")
        out.appendln("""
            |<div id="scratchPad">
            |<div id="scratchHeader">
            |<div id="scratchActions">
            |<input type="button" value="Clear Scratch Pad" onclick="removeScratchEvents();">
            |<!-- input type="button" value="Generate Map" disabled="disabled" -->
            |</div>
            |<p id="scratchVersion">qtmerge v$VERSION</p>
            |<p id="scratchTimestamp"></p>
            |</div>
            |<table id="scratchTable">
            |   <tr>
            |       <th>#</th>
            |       <th>Time (Count)<br>[ID]</th>
            |       <th>Trip (Board)<br>[Datasets]</th>
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

        dsout.appendln("</table></body></html>")
        dsout.close()
    }

    fun MakeEventRow(event: Event, count : Int) : String {
        val out = StringBuilder()
        val isQPost = event.Type() == "Post"
        val ref = Regex(""">>(\d+)""")
        var trip = event.Trip()
        var datasets = event.Dataset()
        var noconfclass = ""
        var confattr = " data-conf=\"true\""
        if((event.Type() == "Post") && (event.Trip() == "Anonymous")) {
            if ((event as PostEvent).anonVerifiedAsQ.size > 0) {
                trip += " Q"
                datasets += ",<br>Q Map ${event.anonVerifiedAsQ.joinToString(",<br>Q Map ")}"
            } else {
                trip += " Q?"
                noconfclass = " event-noconf"
                confattr = " data-conf=\"false\""
            }
        }
        out.appendln("  <tr class=\"event$noconfclass\" id=\"${event.UID}\" data-timestamp='${MakeJSONTimestamp(event.Timestamp())}'$confattr>")
        out.appendln("      <td class=\"e-timestamp\">${event.Timestamp().format(FORMATTER)}<br><span class=\"count\">(${if(isQPost) "Post" else "Tweet"} #<b>$count</b>)</span><br><span class=\"id\">[${event.ID()}]</span></td>")
        out.appendln("      <td class=\"e-trip\">$trip${if(event.Board().isNotEmpty()) "<br><span class=\"board\">(${event.Board()})</span>" else ""}<br><span class=\"datasets\">[$datasets]</span></td>")
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

        return out.toString()
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
