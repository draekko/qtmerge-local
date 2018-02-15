package models

import utils.HTML.Companion.cleanHtmlText
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class QPostImage(
        var url : String?,
        var filename : String?
)

data class QPost(
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
        var images : MutableList<QPostImage>?,
        var references : MutableList<QPost>
) : Event() {
    companion object {
        fun fromInfChPost(board : String, post : InfChPost) : QPost {
            val qPost = QPost(
                post.no.toString(),
                post.id,
                post.time,
                post.title,
                post.name,
                post.email,
                post.trip,
                cleanHtmlText(post.com),
                post.sub,
                "8chan_$board",
                "https://8ch.net/$board/res/${post.resto}.html#${post.no}",
                post.resto.toString(),
                mutableListOf(),
                mutableListOf()
            )

            if(post.tim?.isNotEmpty() == true) {
                qPost.images!!.add(QPostImage("https://media.8ch.net/file_store/${post.tim}${post.ext}", post.filename))
            }
            post.extra_files?.forEach {
                qPost.images!!.add(QPostImage("https://media.8ch.net/file_store/${it.tim}${it.ext}", it.filename))
            }

            return qPost
        }
    }

    override fun Type(): String = "QPost"

    override fun ID(): String {
        return id
    }

    override fun Board(): String = source

    override fun Trip(): String {
        return trip?:""
    }

    override fun Reference(): String {
        return link
    }

    override fun RawTimestamp(): String {
        return timestamp.toString()
    }

    override fun Timestamp(): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("US/Eastern"))
    }

    override fun Text(): String {
        return text?:""
    }

    override fun Images(): List<Pair<String?, String?>> {
        if(images == null) return emptyList()
        return images!!.map { Pair(it.url, it.filename) }
    }
}