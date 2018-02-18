package models.events

import models.importer.QCodeFagPost
import models.mirror.InfChPost
import utils.HTML.Companion.cleanHTMLText
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class PostEvent(
        var host : String,
        var board : String,
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
        var postReferences: MutableList<PostEvent>,
        private var references : MutableList<String>
) : Event() {
    data class PostEventImage(
            var url : String?,
            var filename : String?
    )

    companion object {
        fun fromInfChPost(board : String, infChPost: InfChPost) : PostEvent {
            val postEvent = PostEvent(
                    "8ch.net",
                    board,
                    infChPost.no.toString(),
                    infChPost.id,
                    infChPost.time,
                    infChPost.title,
                    infChPost.name,
                    infChPost.email,
                    infChPost.trip,
                    cleanHTMLText(infChPost.com),
                    infChPost.sub,
                    "https://8ch.net/$board/res/${infChPost.resto}.html#${infChPost.no}",
                    infChPost.resto.toString(),
                    mutableListOf(),
                    mutableListOf(),
                    mutableListOf()
            )

            if(infChPost.tim?.isNotEmpty() == true) {
                postEvent.images!!.add(PostEventImage("https://media.8ch.net/file_store/${infChPost.tim}${infChPost.ext}", infChPost.filename))
            }
            infChPost.extra_files?.forEach {
                postEvent.images!!.add(PostEventImage("https://media.8ch.net/file_store/${it.tim}${it.ext}", it.filename))
            }

            return postEvent
        }

        fun fromQCodeFagPost(board : String, qCodeFagPost: QCodeFagPost) : PostEvent {
            val postEvent = PostEvent(
                    "github.com",
                    board,
                    qCodeFagPost.id,
                    qCodeFagPost.userId,
                    qCodeFagPost.timestamp,
                    qCodeFagPost.title,
                    qCodeFagPost.name,
                    qCodeFagPost.email,
                    qCodeFagPost.trip,
                    qCodeFagPost.text,
                    qCodeFagPost.subject,
                    qCodeFagPost.link, // https://github.com/QCodefag/QCodefag.github.io/tree/master/data
                    //"https://8ch.net/$board/res/${qCodeFagPost.resto}.html#${qCodeFagPost.no}",
                    qCodeFagPost.threadId?:"",
                    mutableListOf(),
                    mutableListOf(),
                    mutableListOf()
            )

            if(qCodeFagPost.images?.isNotEmpty() == true) {
                qCodeFagPost.images!!.forEach {
                    postEvent.images!!.add(PostEventImage(it.url, it.filename))
                }
            }

            return postEvent
        }

    }

    init {
        references.addAll(postReferences.map { it.ReferenceID() })
        // TODO: detect other references
    }

    override fun Host(): String = host

    override fun Type(): String = "Post"

    override fun ID(): String = id

    override fun Board(): String = board

    override fun Trip(): String = trip?:"<anon>"

    override fun Link(): String = link

    override fun ReferenceID(): String = "$host-$board-$id"

    override fun References(): List<String> = references

    override fun RawTimestamp(): String = timestamp.toString()

    override fun Timestamp(): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("US/Eastern"))
    }

    override fun Text(): String = text?:""

    override fun Images(): List<Pair<String?, String?>> {
        if(images == null) return emptyList()
        return images!!.map { Pair(it.url, it.filename) }
    }
}