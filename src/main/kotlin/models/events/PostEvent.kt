package models.events

import models.importer.QCodeFagPost
import models.mirror.InfChPost
import utils.HTML.Companion.cleanHTMLText
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.xml.bind.DatatypeConverter

data class PostEvent(
        var dataset : String,
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
        private var references : MutableList<String>,
        private var referenceID : String
) : Event() {
    data class PostEventImage(
            var url : String?,
            var filename : String?
    )

    companion object {
        fun makeReferenceID(link : String) = link //DatatypeConverter.printHexBinary(MD5.digest(link.toByteArray()))

        fun fromInfChPost(dataset : String, board : String, infChPost: InfChPost) : PostEvent {
            val link = "https://8ch.net/$board/res/${infChPost.resto}.html#${infChPost.no}"
            val postEvent = PostEvent(
                    dataset,
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
                    link,
                    infChPost.resto.toString(),
                    mutableListOf(),
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

            return postEvent
        }

        fun fromQCodeFagPost(dataset : String, board : String, qCodeFagPost: QCodeFagPost) : PostEvent {
            val postEvent = PostEvent(
                    dataset,
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
                    qCodeFagPost.link,
                    qCodeFagPost.threadId?:"",
                    mutableListOf(),
                    mutableListOf(),
                    mutableListOf(),
                    makeReferenceID(qCodeFagPost.link)
            )

            if(qCodeFagPost.images?.isNotEmpty() == true) {
                qCodeFagPost.images!!.forEach {
                    postEvent.images!!.add(PostEventImage(it.url, it.filename))
                }
            }

            return postEvent
        }

    }

    override fun FindReferences() {
        references.addAll(postReferences.map { it.ReferenceID() })

        // Find post references
        if(!text.isNullOrEmpty()) {
            Regex(""".*>>(\d+).*""").findAll(text ?: "").forEach {
                if(!references.contains(it.groupValues[1])) {
                    references.add(it.groupValues[1])
                }
            }
        }
    }

    override fun Dataset(): String = dataset

    override fun Type(): String = "Post"

    override fun ID(): String = id

    override fun Board(): String = board

    override fun Trip(): String = trip?:"<anon>"

    override fun Link(): String = link

    override fun ReferenceID(): String = referenceID

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