import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import controllers.mirror.*
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
        val VERSION = "2018.3-1"
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
        val mirrorDirectory = MIRRORDIR + File.separator + mirrorLabel
        events.addAll(TwitterArchiveMirror(mirrorDirectory, "realDonaldTrump", STARTTIME).MirrorSearch())
        events.addAll(FourChanMirror(mirrorDirectory, "pol", STARTTIME).MirrorSearch())
        events.addAll(InfChMirror(mirrorDirectory, "pol", STARTTIME).MirrorSearch())
        events.addAll(InfChMirror(mirrorDirectory, "cbts", STARTTIME).MirrorSearch())
        events.addAll(InfChMirror(mirrorDirectory, "thestorm", STARTTIME).MirrorSearch())
        events.addAll(InfChMirror(mirrorDirectory, "greatawakening", STARTTIME).MirrorSearch())
        events.addAll(InfChMirror(mirrorDirectory, "qresearch", STARTTIME).MirrorSearch())

        // Capture qcodefag's data
        QCodeFagMirror(mirrorDirectory, "pol", Mirror.Source.FourChan, "pol4chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qcodefag") }
        QCodeFagMirror(mirrorDirectory, "cbts", Mirror.Source.InfChan, "cbtsNonTrip8chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qcodefag") }
        QCodeFagMirror(mirrorDirectory, "cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qcodefag") }
        QCodeFagMirror(mirrorDirectory, "pol", Mirror.Source.InfChan, "polTrip8chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qcodefag") }
        QCodeFagMirror(mirrorDirectory, "thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qcodefag") }
        QCodeFagMirror(mirrorDirectory, "greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qcodefag") }
        QCodeFagMirror(mirrorDirectory, "qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qcodefag") }

        // Capture qanonmap's data
        QAnonMapMirror(mirrorDirectory, "pol", Mirror.Source.FourChan, "pol4chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qanonmap") }
        QAnonMapMirror(mirrorDirectory, "cbts", Mirror.Source.InfChan, "cbtsNonTrip8chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qanonmap") }
        QAnonMapMirror(mirrorDirectory, "cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qanonmap") }
        QAnonMapMirror(mirrorDirectory, "pol", Mirror.Source.InfChan, "polTrip8chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qanonmap") }
        QAnonMapMirror(mirrorDirectory, "thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qanonmap") }
        QAnonMapMirror(mirrorDirectory, "greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qanonmap") }
        QAnonMapMirror(mirrorDirectory, "qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qanonmap") }
        QAnonMapMirror(mirrorDirectory, "qresearch", Mirror.Source.InfChan, "qresearchNonTrip8chanPosts", STARTTIME).MirrorSearch()
                .forEach { post -> MergePostEvent(post as PostEvent, "qanonmap") }

        events.sortBy { it.Timestamp().toEpochSecond() }

        // Initialize base class due to Gson ignoring Kotlin default constructor values, also enumerate id's
        events.forEachIndexed { index, event ->
            event.UID = index.toString()
        }

        println("Total Events: ${events.size}")
    }

    fun MergePostEvent(post: PostEvent, dataset : String) {
        val event = events.find { it.Board() == post.Board() && it.ID() == post.ID() && (it.Link() == post.Link() || it.Timestamp() == post.Timestamp()) }
        if(event == null) {
            events.add(post)
        } else {
            (event as PostEvent).datasets.add(dataset)
        }
    }

    fun ExportJson() {
        val gson = GsonBuilder().setPrettyPrinting().create()
        File("$outputDirectory/qtmerge-pretty.json").writeText(gson.toJson(events.sortedBy { it.Timestamp().toEpochSecond() }))
        File("$outputDirectory/qtmerge.json").writeText(Gson().toJson(events.sortedBy { it.Timestamp().toEpochSecond() }))
    }

    fun ExportHtml() {
        val qcat = File("$outputDirectory/catalog.html").outputStream().bufferedWriter()
        val dsout = File("$outputDirectory/datasets.html").outputStream().bufferedWriter()
        val graphicrows = PostEvent.GRAPHICS.mapIndexed { index, graphic -> """
            |<tr>
            |   <td>${index+1}</td>
            |   <td>${if(graphic.anon) "Anonymous" else "Tripped"}</td>
            |   <td>${graphic.links.joinToString("<br>") { "<a href=\"$it\">${it.substringAfter("#")}</a>"}}</td>
            |   <td>${graphic.conflinks.joinToString("<br>") { "<a href=\"$it\">${it.substringAfter("#")}</a>"}}</td>
            |   <td style="text-align:left;">${graphic.confnotes.joinToString("<br>")}</td>
            |   <td style="text-align:left;">${graphic.notes}</td>
            |</tr>
            """.trimMargin() }.joinToString("")
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
            |       <li>https://archive.4plebs.org/</li>
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
            |   <table class="confirmations">
            |   <tr><th>#</th><th>Type</th><th>Graphics</th><th>Confirmations</th><th>Q Note</th><th>Other Notes</th></tr>
            |   $graphicrows
            |   </table>
            |   <dl>
            |       <dt>Unconfirmed Q Map Set in recent qresearch OPs?</dt>
            |       <dd>
            |           Graphic: https://8ch.net/qresearch/res/387596.html#387700
            |       </dd>
            |   </dl>
            |
            |   <p>Currently qtmerge relies on graphics/maps 1-${PostEvent.GRAPHICS.size} above to mark non-tripped posts as confirmed from Q.</p>
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
        val qmaps = PostEvent.GRAPHICS.mapIndexed { index, graphic -> "<a href=\"${graphic.links.first()}\" title=\"${graphic.id}\">Q Map ${index+1}</a>" }.joinToString(" | ")
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
            |           <b><a href="datasets.html">Datasets</a>:</b>
            |           <a href="https://anonsw.github.io/">anonsw</a> |
            |           <a href="https://qanonposts.com/">qcodefag</a> |
            |           <a href="https://qanonmap.github.io/">qanonmap</a> |
            |           <a href="https://trumptwitterarchive.com/">twitterarchive</a> |
            |           $qmaps
            |       </div>
            |       <div class="wip">
            |           <b>Work in progress:</b> <i>Auto update</i> | <i>Dataset Downloads</i> | <i>Thread Catalog</i> | <i>Thread Maps</i> | <i>Search/Filter</i>
            |       </div>
            |       <div class="menu">
            |           <input id="openScratchPadButton" type="button" value="Open Scratch Pad" disabled="disabled"> <small>(Click posts to add/remove)</small>
            |       </div>
            |   </div>
            |""".trimMargin())

        out.append("""<table id="events">
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
        var datasets = event.Datasets().joinToString(",<br>")
        var noconfclass = ""
        var confattr = " data-conf=\"true\""
        if(event.Type() == "Post") {
            if ((event as PostEvent).inGraphics.size > 0) {
                datasets += ",<br>Q Map ${event.inGraphics.map { graphId -> PostEvent.GRAPHICS.indexOfFirst { it.id == graphId && it.source == event.source } + 1 }.joinToString(",<br>Q Map ")}"
            }
            if(event.Trip() == "Anonymous") {
                if (event.inGraphics.size > 0) {
                    trip += " Q"
                } else {
                    trip += " Q?"
                    noconfclass = " event-noconf"
                    confattr = " data-conf=\"false\""
                }
            }
        }

        // TODO: need to show subject/name/email...Q uses these in peculiar ways?
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
