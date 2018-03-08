package models.events

import controllers.mirror.FourChanMirror
import controllers.mirror.InfChMirror
import controllers.mirror.Mirror
import models.importer.QCodeFagPost
import models.mirror.FourChanPost
import models.mirror.InfChPost
import settings.Settings.Companion.ZONEID
import utils.HTML.Companion.cleanHTMLText
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class PostEvent(
        datasets : MutableList<String>,
        board : String,
        source : Mirror.Source,
        mirrorFile: String,
        var id : String,
        var userId : String?,
        var timestamp : Long,
        var title : String?,
        var name : String?,
        var email : String?,
        var trip : String?,
        var text : String?,
        var subject : String?,
        var link : String,
        var threadId : String,
        var images : MutableList<PostEventImage>?,
        var inGraphics: MutableList<String>,
        private var referenceID : String
) : Event("Post", datasets, board, source, mirrorFile) {

    data class PostEventImage(
        var url : String?,
        var filename : String?
    )

    companion object {
        data class Graphic(
            var source : Mirror.Source,
            var id : String,
            var anon : Boolean,
            var links : List<String>,
            var conflinks : List<String>,
            var confnotes : List<String>,
            var notes : String = ""
        )
        val GRAPHICS = listOf(
            Graphic(Mirror.Source.FourChan,"148016876", true,
                    listOf("https://archive.4plebs.org/pol/thread/147978093/#147985268", "https://archive.4plebs.org/pol/thread/148016618/#148016876"),
                    listOf("https://archive.4plebs.org/pol/thread/148016618/#148016876"),
                    listOf("Graphic is right."),
                    "Q reposted graphic without modification"),
            Graphic(Mirror.Source.FourChan,"148028820", true,
                    listOf("https://archive.4plebs.org/pol/thread/148019103/#148028820"),
                    listOf("https://archive.4plebs.org/pol/thread/148019103/#148029633"),
                    listOf("Thank you Anon.")),
            Graphic(Mirror.Source.FourChan,"148136656", true,
                    listOf("https://archive.4plebs.org/pol/thread/148136485/#148136656"),
                    listOf("https://archive.4plebs.org/pol/thread/148136485/#148139234"),
                    listOf("Re-read complete crumb graphic (confirmed good).")),
            Graphic(Mirror.Source.FourChan,"148147343", true,
                    listOf("https://archive.4plebs.org/pol/thread/148146734/#148147343"),
                    listOf("https://archive.4plebs.org/pol/thread/148146734/#148148004"),
                    listOf("Graphic confirmed.")),
            Graphic(Mirror.Source.FourChan,"148779656", false,
                    listOf("https://archive.4plebs.org/pol/thread/148767870/#148768131", "https://archive.4plebs.org/pol/thread/148777785/#148779656"),
                    listOf("https://archive.4plebs.org/pol/thread/148777785/#148779656"),
                    listOf("Attached gr[A]phic is correct."),
                    "Q reposted graphic without modification"),
            Graphic(Mirror.Source.FourChan,"148872136", false,
                    listOf("https://archive.4plebs.org/pol/thread/148866159/#148872136"),
                    listOf("https://archive.4plebs.org/pol/thread/148866159/#148872500"),
                    listOf("Confirmed.\nCorrect.")),
            Graphic(Mirror.Source.FourChan,"149083850", false,
                    listOf("https://archive.4plebs.org/pol/thread/149080733/#149083850"),
                    listOf("https://archive.4plebs.org/pol/thread/150171127/#150172069", "https://archive.4plebs.org/pol/thread/150171127/#150172817"),
                    listOf("QMAP 1/2 confirmed.", "1&2 confirmed."),
                    "1?"),
            Graphic(Mirror.Source.FourChan,"150171513", false,
                    listOf("https://archive.4plebs.org/pol/thread/150171127/#150171513"),
                    listOf("https://archive.4plebs.org/pol/thread/150171127/#150172069", "https://archive.4plebs.org/pol/thread/150171127/#150172817"),
                    listOf("QMAP 1/2 confirmed.", "1&2 confirmed."),
                    "2?"),
            Graphic(Mirror.Source.FourChan,"150171863", false,
                    listOf("https://archive.4plebs.org/pol/thread/150171127/#150171863"),
                    listOf("https://archive.4plebs.org/pol/thread/150171127/#150172069", "https://archive.4plebs.org/pol/thread/150171127/#150172817"),
                    listOf("QMAP 1/2 confirmed.", "1&2 confirmed."),
                    "2?")
        )

        fun makeReferenceID(link : String) = link //DatatypeConverter.printHexBinary(MD5.digest(link.toByteArray()))

        fun fromInfChPost(dataset : String, source: Mirror.Source, board: String, mirrorFile: String, infChPost: InfChPost) : PostEvent {
            val threadId = if(infChPost.resto == 0L) infChPost.no else infChPost.resto
            val link = "https://8ch.net/$board/res/$threadId.html#${infChPost.no}"
            val postEvent = PostEvent(
                    mutableListOf(dataset),
                    board,
                    source,
                    mirrorFile,
                    infChPost.no.toString(),
                    infChPost.id,
                    infChPost.time,
                    infChPost.title,
                    infChPost.name,
                    infChPost.email,
                    infChPost.trip,
                    cleanHTMLText(infChPost.com),
                    infChPost.sub,
                    link,
                    threadId.toString(),
                    mutableListOf(),
                    mutableListOf(),
                    makeReferenceID(link)
            )

            if(infChPost.tim?.isNotEmpty() == true) {
                postEvent.images!!.add(PostEventImage("https://media.8ch.net/file_store/${infChPost.tim}${infChPost.ext}", infChPost.filename))
            }
            infChPost.extra_files?.forEach {
                postEvent.images!!.add(PostEventImage("https://media.8ch.net/file_store/${it.tim}${it.ext}", it.filename))
            }

            // Link graphics/maps
            val graphicMap = when(source) {
                Mirror.Source.InfChan -> {
                    InfChMirror.EXCEPTIONS[board]!!.qgraphics
                }
                else -> listOf()
            }
            if(graphicMap.find { it.first == postEvent.id } != null) {
                postEvent.inGraphics.addAll(graphicMap.find { it.first == postEvent.id }!!.second)
            }

            return postEvent
        }

        fun fromFourChanPost(dataset: String, source: Mirror.Source, board: String, mirrorFile: String, fourChanPost: FourChanPost) : PostEvent {
            val link = "https://archive.4plebs.org/$board/thread/${fourChanPost.thread_num}/#${fourChanPost.num}"
            val postEvent = PostEvent(
                    mutableListOf(dataset),
                    board,
                    source,
                    mirrorFile,
                    fourChanPost.num,
                    fourChanPost.poster_hash,
                    fourChanPost.timestamp,
                    fourChanPost.title,
                    fourChanPost.name,
                    fourChanPost.email,
                    fourChanPost.trip,
                    cleanHTMLText(fourChanPost.comment?:""),
                    "",
                    link,
                    fourChanPost.thread_num,
                    mutableListOf(),
                    mutableListOf(),
                    makeReferenceID(link)
            )

            if(fourChanPost.media != null) {
                postEvent.images!!.add(PostEventImage(fourChanPost.media!!.media_link, fourChanPost.media!!.media_filename))
            }

            val graphicMap = when(source) {
                Mirror.Source.FourChan -> {
                    FourChanMirror.EXCEPTIONS[board]!!.qgraphics
                }
                else -> listOf()
            }
            if(graphicMap.find { it.first == postEvent.id } != null) {
                postEvent.inGraphics.addAll(graphicMap.find { it.first == postEvent.id }!!.second)
            }

            return postEvent
        }

        fun fromQCodeFagPost(dataset : String, source: Mirror.Source, board : String, mirrorFile: String, qCodeFagPost: QCodeFagPost) : PostEvent {
            val postEvent = PostEvent(
                    mutableListOf(dataset),
                    board,
                    source,
                    mirrorFile,
                    qCodeFagPost.id,
                    qCodeFagPost.userId,
                    qCodeFagPost.timestamp,
                    qCodeFagPost.title,
                    qCodeFagPost.name,
                    qCodeFagPost.email,
                    qCodeFagPost.trip,
                    qCodeFagPost.text,
                    qCodeFagPost.subject,
                    qCodeFagPost.link,
                    qCodeFagPost.threadId?:"",
                    mutableListOf(),
                    mutableListOf(),
                    makeReferenceID(qCodeFagPost.link)
            )

            if(qCodeFagPost.images?.isNotEmpty() == true) {
                qCodeFagPost.images!!.forEach {
                    postEvent.images!!.add(PostEventImage(it.url, it.filename))
                }
            }

            // Fix extra slashes in link
            postEvent.link = postEvent.link.replace(Regex("""(?<!https?:)/+"""), "/")

            // Link graphics/maps
            val graphicMap = when(source) {
                Mirror.Source.FourChan -> {
                    FourChanMirror.EXCEPTIONS[board]!!.qgraphics
                }
                else -> listOf()
            }
            if(graphicMap.find { it.first == postEvent.id } != null) {
                postEvent.inGraphics.addAll(graphicMap.find { it.first == postEvent.id }!!.second)
            }

            return postEvent
        }

    }

    // Example with both good and bad ref link in data:
    // "<p class=\"body-line ltr \"><a onclick=\"highlightReply('567454', event);\" href=\"\/qresearch\/res\/567140.html#567454\">&gt;&gt;567454<\/a><\/p><p class=\"body-line ltr \">These peo&gt;&gt;567493<\/p><p class=\"body-line ltr \">ple are stupid.<\/p><p class=\"body-line ltr \">Wait for Russia\/China reports.<\/p><p class=\"body-line ltr \">Sabotage.<\/p><p class=\"body-line ltr \">Investigation.<\/p><p class=\"body-line ltr \">Strike 99999999.<\/p><p class=\"body-line empty \"><\/p><p class=\"body-line ltr \">Q<\/p>"
    override fun FindReferences() : List<Event> {
        /*
        // Find post references
        if(!text.isNullOrEmpty()) {
            Regex(""".*>>(\d+).*""").findAll(text ?: "").forEach {
            }
        }
        */

        return emptyList()
    }

    override fun ID(): String = id

    override fun ThreadID(): String = threadId

    override fun Trip(): String = if(trip.isNullOrEmpty()) "Anonymous" else trip?:""

    override fun Link(): String = link

    override fun ReferenceID(): String = referenceID

    override fun RawTimestamp(): String = timestamp.toString()

    override fun Timestamp(): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZONEID)
    }

    override fun Subject() : String = if(title.isNullOrEmpty()) subject?:"" else title?:""

    override fun Text(): String = text?:""

    override fun Images(): List<Pair<String?, String?>> {
        if(images == null) return emptyList()
        return images!!.map { Pair(it.url, it.filename) }
    }
}