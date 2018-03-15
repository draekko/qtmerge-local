import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import controllers.mirror.*
import models.events.Event
import models.events.Event.Companion.MergeEvent
import models.events.PostEvent
import models.mirror.ReferenceCache
import models.q.Abbreviations
import settings.Settings
import settings.Settings.Companion.FORMATTER
import settings.Settings.Companion.STARTTIME
import settings.Settings.Companion.VERSION
import settings.Settings.Companion.ZONEID
import utils.HTML.Companion.escapeHTML
import java.io.File
import java.time.ZonedDateTime

// TODO: Switch to Kotson?

fun main(args: Array<String>) {
    val qtmerge = QTMerge()
    qtmerge.ExportHtml()
    qtmerge.ExportJson()
}

class QTMerge(
    var outputDirectory : String = System.getProperty("user.dir") + File.separator + "anonsw.github.io" + File.separator + "qtmerge",
    var mirrors : MutableList<Mirror.MirrorConfig> = arrayListOf()
) {
    var events : MutableList<Event> = arrayListOf()
    var threads : MutableList<Event> = arrayListOf()
    var refs : ReferenceCache = ReferenceCache()

    companion object {
        val ROLLBACKDAYS = 111L
    }

    init {
        LoadMirrors()
    }

    fun LoadMirrors() {
        mirrors.addAll(listOf(
            // Twitter Archive
            Mirror.MirrorConfig(TwitterArchiveMirror("realDonaldTrump", STARTTIME), true),

            // Anonsw
            Mirror.MirrorConfig(FourPlebsMirror("pol", STARTTIME, ZonedDateTime.of(2017, 12, 14, 0, 0, 0, 0, ZONEID)), true),
            Mirror.MirrorConfig(InfChMirror("pol", STARTTIME, ZonedDateTime.of(2018, 2, 15, 0, 0, 0, 0, ZONEID)), true),
            Mirror.MirrorConfig(InfChMirror("cbts", STARTTIME, ZonedDateTime.of(2018, 1, 15, 0, 0, 0, 0, ZONEID)), true),
            Mirror.MirrorConfig(InfChMirror("thestorm", STARTTIME, ZonedDateTime.of(2018, 1, 15, 0, 0, 0, 0, ZONEID)), true),
            Mirror.MirrorConfig(InfChMirror("greatawakening", STARTTIME), true),
            Mirror.MirrorConfig(InfChMirror("qresearch", STARTTIME), true),
            Mirror.MirrorConfig(InfChMirror("comms", STARTTIME), true),

            // QCodeFag
            Mirror.MirrorConfig(QCodeFagMirror("pol", Mirror.Source.FourChan, "pol4chanPosts", STARTTIME), false),
            Mirror.MirrorConfig(QCodeFagMirror("cbts", Mirror.Source.InfChan, "cbtsNonTrip8chanPosts", STARTTIME), false),
            Mirror.MirrorConfig(QCodeFagMirror("cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts", STARTTIME), false),
            Mirror.MirrorConfig(QCodeFagMirror("pol", Mirror.Source.InfChan, "polTrip8chanPosts", STARTTIME), false),
            Mirror.MirrorConfig(QCodeFagMirror("thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts", STARTTIME), false),
            Mirror.MirrorConfig(QCodeFagMirror("greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts", STARTTIME), false),
            Mirror.MirrorConfig(QCodeFagMirror("qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts", STARTTIME), false),

            // QAnonMap
            Mirror.MirrorConfig(QAnonMapMirror("pol", Mirror.Source.FourChan, "pol4chanPosts", STARTTIME), false),
            Mirror.MirrorConfig(QAnonMapMirror("cbts", Mirror.Source.InfChan, "cbtsNonTrip8chanPosts", STARTTIME), false),
            Mirror.MirrorConfig(QAnonMapMirror("cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts", STARTTIME), false),
            Mirror.MirrorConfig(QAnonMapMirror("pol", Mirror.Source.InfChan, "polTrip8chanPosts", STARTTIME), false),
            Mirror.MirrorConfig(QAnonMapMirror("thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts", STARTTIME), false),
            Mirror.MirrorConfig(QAnonMapMirror("greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts", STARTTIME), false),
            Mirror.MirrorConfig(QAnonMapMirror("qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts", STARTTIME), false),
            Mirror.MirrorConfig(QAnonMapMirror("qresearch", Mirror.Source.InfChan, "qresearchNonTrip8chanPosts", STARTTIME), false)
        ))
    }

    fun LoadEvents() {
        if(events.isEmpty()) {
            mirrors.forEach {
                if (it.primarySource) {
                    events.addAll(it.mirror.MirrorSearch())
                } else {
                    it.mirror.MirrorSearch().forEach { event ->
                        MergeEvent(events, event, it.mirror.dataset)
                    }
                }
            }

            events.sortBy { it.Timestamp().toEpochSecond() }

            // Enumerate id's
            events.forEachIndexed { index, event ->
                event.UID = index.toString()
            }
        }
    }

    fun LoadReferenceCache() {
        File(Settings.CACHEDIR + File.separator + "refcache-min.json").bufferedReader().use {
            refs = Gson().fromJson(it, ReferenceCache::class.java)
        }
    }

    fun LoadCatalog() {
        if(threads.isEmpty()) {
            mirrors.forEach {
                if (it.primarySource) {
                    threads.addAll(it.mirror.MirrorSearch(Mirror.SearchParameters(Mirror.SearchOperand.OP())))
                } else {
                    it.mirror.MirrorSearch(Mirror.SearchParameters(Mirror.SearchOperand.OP())).forEach { event ->
                        MergeEvent(threads, event, it.mirror.dataset)
                    }
                }
            }

            threads.sortBy { -it.Timestamp().toEpochSecond() }

            // Enumerate id's
            threads.forEachIndexed { index, thread ->
                thread.UID = index.toString()
            }
        }
    }

    fun ExportJson() {
        LoadEvents()
        val minimalGson = Gson()
        val prettyGson = GsonBuilder().setPrettyPrinting().create()
        val timestamp = ZonedDateTime.now(ZONEID).format(FORMATTER)
        File("$outputDirectory/data").mkdirs()
        File("$outputDirectory/data/qtmerge-qtposts.json").writeText(prettyGson.toJson(mapOf(
                Pair("qtmerge", VERSION),
                Pair("timestamp", timestamp),
                Pair("qtposts", events.sortedBy { it.Timestamp().toEpochSecond() }))
        ))
        File("$outputDirectory/data/qtmerge-qtposts-min.json").writeText(minimalGson.toJson(mapOf(
                Pair("qtmerge", VERSION),
                Pair("timestamp", timestamp),
                Pair("qtposts", events.sortedBy { it.Timestamp().toEpochSecond() }))
        ))
        File("$outputDirectory/data/qtmerge-abbreviations.json").writeText(prettyGson.toJson(mapOf(
                Pair("qtmerge", VERSION),
                Pair("timestamp", timestamp),
                Pair("abbreviations", Abbreviations.dict.toSortedMap())
        )))
        File("$outputDirectory/data/qtmerge-abbreviations-min.json").writeText(minimalGson.toJson(mapOf(
                Pair("qtmerge", VERSION),
                Pair("timestamp", timestamp),
                Pair("abbreviations", Abbreviations.dict.toSortedMap())
        )))
    }

    fun MakeHeader(prefix: String = "..", title : String = "", bodyStyle : String = "", includeViz : Boolean = false) : String = """
        |<!doctype html>
        |<html lang="en">
        |   <head>
        |       <meta charset="utf-8">
        |       <title>qtmerge v$VERSION${if(title.isNotEmpty()) " $title" else ""}</title>
        |       <link rel="stylesheet" type="text/css" href="$prefix/styles/reset.css">
        |       <link rel="stylesheet" type="text/css" href="$prefix/styles/screen.css" media="screen">
        |       <link rel="stylesheet" type="text/css" href="$prefix/styles/print.css" media="print">
        |       <link rel="stylesheet" type="text/css" href="$prefix/qtmerge/qtmerge.css">
        |       <link rel="stylesheet" type="text/css" href="$prefix/libs/jquery-ui-1.12.1/jquery-ui.min.css">
        |       <script type="text/javascript" src="$prefix/scripts/jquery-3.3.1.min.js"></script>
        |       <script type="text/javascript" src="$prefix/libs/jquery-ui-1.12.1/jquery-ui.min.js"></script>
        |       <script type="text/javascript" src="$prefix/scripts/jquery.jeditable.mini.js"></script>
        |       <script type="text/javascript" src="$prefix/scripts/jquery.highlight-5.js"></script>
        |       <script type="text/javascript" src="$prefix/scripts/underscore-min.js"></script>
        |       <script type="text/javascript" src="$prefix/scripts/moment.min.js"></script>
        |       <script type="text/javascript" src="$prefix/scripts/moment-timezone-with-data-2012-2022.js"></script>
        |       ${if(includeViz) """<script type="text/javascript" src="$prefix/scripts/svg-pan-zoom-3.5.2.min.js"></script><script type="text/javascript" src="$prefix/scripts/viz-1.8.1.js"></script>""" else ""}
        |       <!--
        |         -- qtmerge v$VERSION
        |         -- https://anonsw.github.com/qtmerge/
        |         -->
        |   </head>
        |   <body${if(bodyStyle.isNotEmpty()) " style=\"$bodyStyle\"" else ""}>
        |   ${if(title.isNotEmpty()) "<h1>qtmerge v$VERSION $title</h1>" else ""}
        """.trimMargin()

    fun ExportHtml() {
        LoadEvents()
        LoadReferenceCache()
        LoadCatalog()
        File(outputDirectory).mkdirs()

        println("Total Events: ${events.size}")
        println("Total Threads: ${threads.size}")

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
        dsout.append(MakeHeader("..", "Datasets", "margin-top:0;"))
        dsout.append("""
            |   <a href="./">&Lt; qtmerge</a>
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
            |       <li>https://github.com/qanonmap/ &rarr; https://qanonmap.github.io/, https://qanon.pub/</li>
            |       <li>? &rarr; https://thestoryofq.com/</li>
            |       <li>https://github.com/anonsw/ &rarr; https://anonsw.github.io/</li>
            |       <li>Others...?</li>
            |   </ul>
            |
            |   <p>Modified raw forms: <a href="data/qtmerge-qtposts.json">JSON</a> | <a href="data/qtmerge-qtposts-min.json">Minified JSON</a></p>
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
        out.append(MakeHeader())
        out.append("""
            |   <div id="header">
            |       <div class="timestamp">qtmerge v$VERSION &mdash; Last Updated: ${ZonedDateTime.now(ZONEID).format(FORMATTER)}</div>
            |       <div class="settings">Post Time Offset (days): <span id="postedOffsetPrint">111</span></div>
            |       <div class="datasets">
            |           <b><a href="datasets.html">Datasets</a>:</b>
            |           <a href="https://anonsw.github.io/">anonsw</a> |
            |           <a href="https://qanonposts.com/">qcodefag</a> |
            |           <a href="https://qanon.pub/">qanonmap</a> |
            |           <a href="https://trumptwitterarchive.com/">twitterarchive</a> |
            |           $qmaps
            |       </div>
            |       <div class="derived">
            |           <a href="catalog.html">Thread Catalog</a> | <a href="abbreviations.html">Abbreviations</a> | <b>Work in progress:</b> <i>Dataset Downloads</i> | <i>Thread Maps</i> | <i>Search/Filter</i>
            |       </div>
            |       <div class="menu">
            |          <form>
            |              Post Time Offset (days): <input id="postedOffset" type="text" value="111" disabled="disabled"><!-- TODO: slider instead? -->
            |              <input type="button" value="Print..." onclick="window.print();">
            |              <input id="openScratchPadButton" type="button" value="Open Scratch Pad" disabled="disabled"> <small>(Click posts to add/remove)</small>
            |          </form>
            |       </div>
            |   </div>
            |""".trimMargin())

        out.append("""<table id="events">
            |   <thead>
            |   <tr>
            |       <th>Time (Count)<br>[ID]</th>
            |       <th>Trip (Board)<br>[Datasets]</th>
            |       <th>Link</th>
            |       <th>Text</th>
            |   </tr>
            |   </thead>
            |   <tbody>
            |""".trimMargin())

        var qpostCount = events.count { it.Type() == "Post" } + 1
        var tweetCount = events.count { it.Type() == "Tweet" } + 1
        var isQPost = false
        events.reversed().forEach { event ->
            when(event.Type()) {
                "Post" -> {
                    qpostCount--
                    isQPost = true
                    if(event.Trip() == "Anonymous") {
                        dsout.appendln(MakeEventRow(event, qpostCount, false))
                    }
                }
                "Tweet" -> { tweetCount--; isQPost = false }
            }
            out.appendln(MakeEventRow(event, if(isQPost) qpostCount else tweetCount))
        }

        out.appendln("</tbody></table>")
        out.appendln("""
            |<div id="scratchPad">
            |<div id="scratchHeader">
            |<div class="settings">Post Time Offset (days): <span id="postedOffsetPrintScratch">111</span></div>
            |<div id="scratchActions">
            |<form>
            |<input type="button" value="Clear" onclick="removeScratchEvents();">
            |<input id="printScratchPadButton" type="button" value="Print..." onclick="printScratchPad();" disabled="disabled">
            |<input id="toggleHighlightingCheckbox" type="checkbox" onclick="toggleHighlighting();" checked="checked"> <label for="toggleHighlightingCheckbox">Auto word highlighting</label>
            |<!-- input type="button" value="Generate Map" disabled="disabled" -->
            |</form>
            |</div>
            |<p id="scratchVersion">qtmerge v$VERSION</p>
            |<p id="scratchTimestamp"></p>
            |</div>
            |<table id="scratchTable">
            |   <thead>
            |   <tr>
            |       <th>#</th>
            |       <th>Time (Count)<br>[ID]</th>
            |       <th>Trip (Board)<br>[Datasets]</th>
            |       <th>Link</th>
            |       <th>Text</th>
            |   </tr>
            |   </thead>
            |   <tbody></tbody>
            |</table>
            |<div id="scratchTimes"></div>
            |</div>
            |""".trimMargin())

        out.append("""<script type="text/javascript" src="qtmerge.js"></script>""")
        out.append("</body></html>")
        out.close()

        dsout.appendln("</table></body></html>")
        dsout.close()

        if(threads.size > 0) {
            val qcat = File("$outputDirectory/catalog.html").outputStream().bufferedWriter()
            qcat.append(MakeHeader("..", "Thread Catalog", "margin-top: 0;"))
            qcat.append("<a href=\"./\">&Lt; qtmerge</a><br><p>This catalog is a work in progress.</p>")

            qcat.append("<table id=\"catalog\"><thead><tr><th>Date</th><th>Source</th><th>Board</th><th>Link</th><th>Subject</th></tr></thead><tbody>")
            var lastDate = threads[0].Timestamp()
            threads.forEach {
                var hr = ""
                if (lastDate.dayOfMonth != it.Timestamp().dayOfMonth) {
                    hr = " class=\"first-for-day\""
                    lastDate = it.Timestamp()
                }
                qcat.append("""
                    |<tr$hr><td>${it.Timestamp().format(FORMATTER)}</td><td>${it.Source()}</td><td>${it.Board()}</td><td><a href="${it.Link()}">${it.ID()}</a></td><td>${it.Subject()}</td></tr>
                    """.trimMargin())
            }
            qcat.append("</tbody></table></body></html>")
            qcat.close()
        }

        // Abbreviations
        val aout = File("$outputDirectory/abbreviations.html").outputStream().bufferedWriter()
        aout.append(MakeHeader("..", "Abbreviations", "margin-top:0;"))
        aout.append("""
            |   <a href="./">&Lt; qtmerge</a><br>
            |   <p>
            |   Downloads: <a href="data/qtmerge-abbreviations.json">JSON</a> | <a href="data/qtmerge-abbreviations-min.json">Minified JSON</a>
            |   </p>
            |<table id="abbreviations">
            |<thead>
            |<tr><th>Abbreviation</th><th>Possible Meaning</th></tr>
            |</thead>
            |<tbody>
            """.trimMargin())
        Abbreviations.dict.toSortedMap().forEach { abbr, meanings ->
            aout.append("<tr><td>${escapeHTML(abbr)}</td><td>${escapeHTML(meanings)}</td></tr>")
        }
        aout.append("</tbody></table></body></html>")
        aout.close()

        // Maps
        val event = events.first { it.Type() == "Post" }
        File("$outputDirectory/maps/${event.Source()}/${event.Board()}").mkdirs()
        val mout = File("$outputDirectory/maps/${event.Source()}/${event.Board()}/${event.ID()}.html").outputStream().bufferedWriter()
        mout.append(MakeHeader("../../../..", event.ID(), "margin-top:0;", true))
        mout.appendln("""
            |<a href="../../../../">&Lt; qtmerge</a><br>
            |<textarea id="map">
            |digraph G {
            """.trimMargin())

        val nodeTip = PrepNodeTip(event.Text())
        var nodeLabel = PrepNodeLabel(event.Text())
        if(event.Text().length > 30) {
            nodeLabel = "${PrepNodeLabel(event.Text().substring(0,30))}..."
        }
        mout.appendln("${event.ID()} [style=\"filled,bold\" fillcolor=lightyellow shape=Mrecord URL=\"${event.Link()}\" tooltip=<$nodeTip> label=<<table border=\"0\" cellborder=\"0\" cellpadding=\"0\"><tr><td bgcolor=\"#333333\"><font color=\"#cccccc\">${event.Trip()}</font></td></tr><tr><td balign=\"left\">$nodeLabel</td></tr></table>>]")

        // How to quickly get references? Collect ref ids/threads into search params, pass to Search?

        /*
            refs.refs[event.ReferenceID()]?.references?.forEach {
            }
            */
                /*
            |digraph G {
            |    subgraph cluster_0 {
            |        style=filled;
            |        color=lightgrey;
            |        node [style=filled,color=white];
            |        a0 -> a1 -> a2 -> a3;
            |        label = "process #1";
            |    }
            |
            |    subgraph cluster_1 {
            |        node [style=filled];
            |        b0 -> b1 -> b2 -> b3;
            |        label = "process #2";
            |        color=blue
            |    }
            |    start -> a0;
            |    start -> b0;
            |    a1 -> b3;
            |    b2 -> a3;
            |    a3 -> a0;
            |    a3 -> end;
            |    b3 -> end;
            |
            |    start [shape=Mdiamond];
            |    end [shape=Msquare];
            |}
            */
        mout.append("""
            |}
            |</textarea>
            |<div id="output" style="position:fixed; top: 100px; left : 0; right : 0; bottom : 0;"></div>
            |<script type="text/javascript" src="../../maps.js"></script></body></html>
            """.trimMargin())
        mout.close()

        // Notable posts
        val nout = File("$outputDirectory/notables.html").outputStream().bufferedWriter()
        nout.append(MakeHeader("..", "Notable Posts", "margin-top:0;"))
        nout.append("""
            |   <a href="./">&Lt; qtmerge</a><br>
            |<table id="notablePosts">
            |<thead>
            |</thead>
            |<tbody>
            """.trimMargin())
        nout.close()
    }

    fun PrepNodeTip(value : String) : String = escapeHTML(escapeHTML(value).replace("\n", "&#10;"))
    fun PrepNodeLabel(value : String) : String = escapeHTML(escapeHTML(value)).replace("\n", "<br/>")

    fun MakeEventRow(event: Event, count : Int, emitOffset: Boolean = true) : String {
        val out = StringBuilder()
        val isQPost = event.Type() == "Post"
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
        out.appendln("      <td class=\"e-timestamp\">Posted: ${event.Timestamp().format(FORMATTER)}${if(emitOffset) "<br>Offset: ${event.Timestamp().plusDays(ROLLBACKDAYS).format(FORMATTER)}" else ""}<br><span class=\"count\">(${if(isQPost) "Post" else "Tweet"} #<b>$count</b>)</span><br><span class=\"id\">[${event.ID()}]</span></td>")
        out.appendln("      <td class=\"e-trip\">$trip${if(event.Board().isNotEmpty()) "<br><span class=\"board\">(${event.Board()})</span>" else ""}<br><span class=\"datasets\">[$datasets]</span></td>")
        out.appendln("      <td class=\"e-type\"><a href=\"${event.Link()}\">${event.Type()}</a></td>")
        var images = event.Images().map {
            var str = ""
            if (it.first != null) {
                str += """<a href="${it.first}">"""
            }
            if (it.second != null) {
                str += it.second
            } else if (it.first != null) {
                str += it.first
            }
            if (it.first != null) {
                str += "</a>"
            }
            str
        }.joinToString("<br>")
        if(images.isNotEmpty() && event.Text().isNotEmpty()) {
            images += "<br>"
        }
        var text = event.Text()
        var refurl = event.Link().replaceAfter("#", "")
        if(!refurl.endsWith("#")) {
            refurl += "#"
        }
        // TODO: move the replacement functionality into PostEvent
        text = text.replace(Regex(""">>(\d+)"""), { match ->
            val ref = refs.refs[listOf("Post", event.Source(), event.Board(), match.groupValues[1]).joinToString("-")]
            if(ref != null) {
                "<a href=\"${ref.link}\">&gt;&gt;${match.groupValues[1]}</a>"
            } else {
                "<span class=\"missing\" title=\"Missing Data\">&gt;&gt;${match.groupValues[1]}</span>"
            }
        })
        text = text.replace(Regex(""">>>(/?(\w+)/(\d+))"""), { match ->
            val ref = refs.refs[listOf("Post", event.Source(), match.groupValues[2], match.groupValues[3]).joinToString("-")]
            if(ref != null) {
                "<a href=\"${ref.link}\">&gt;&gt;&gt;${match.groupValues[1]}</a>"
            } else {
                "<span class=\"missing\" title=\"Missing Data\">&gt;&gt;&gt;${match.groupValues[1]}</span>"
            }
        })
        val boxRegex = Regex("""\[([^\]]+)\]""")
        text = text.replace(boxRegex, { matchResult ->
            "[<span class=\"boxed\">${matchResult.groupValues[1]}</span>]"
        })
        val capsRegex = Regex("""\b((?<!@)[A-Z]{2,})+\b""")
        text = text.replace(capsRegex, { matchResult ->
            "<span class=\"caps\">${matchResult.groupValues[1]}</span>"
        })
        val abbrRegex = Regex("""\b(${Abbreviations.dict.keys.joinToString("|")})\b""", RegexOption.MULTILINE)
        text = text.replace(abbrRegex, { matchResult ->
            "<span class=\"abbr\" title=\"${Abbreviations.dict[matchResult.groupValues[1]]}\">${matchResult.groupValues[1]}</span>"
        })
        text = "<div class=\"e-text-line\"><span>" + text.split("\n").joinToString("</span></div><div class=\"e-text-line\"><span>") + "</span></div>"
        out.appendln("      <td class=\"e-text\">$images$text</td>")
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
