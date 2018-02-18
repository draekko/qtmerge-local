package models.events

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
        var source : String,
        var link : String,
        var threadId : String,
        var images : MutableList<PostEventImage>?,
        var references : MutableList<PostEvent>
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
                    "8chan_$board",
                    "https://8ch.net/$board/res/${infChPost.resto}.html#${infChPost.no}",
                    infChPost.resto.toString(),
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
    }

    override fun Host(): String = host

    override fun Type(): String = "Post"

    override fun ID(): String = id

    override fun Board(): String = source

    override fun Trip(): String = trip?:"<anon>"

    override fun Reference(): String = link

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