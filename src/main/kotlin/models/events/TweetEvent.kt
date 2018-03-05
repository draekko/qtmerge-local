package models.events

import controllers.mirror.Mirror
import models.mirror.TwitterArchiveTweet
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TweetEvent(
        datasets : MutableList<String>,
        board : String,
        source : Mirror.Source,
        val id_str : String,
        val text : String?,
        val created_at : String,
        val retweet_count : Long,
        val in_reply_to_user_id_str : String?,
        val favorite_count : Long,
        val is_retweet : Boolean,
        val retweet_board: String,
        private val references : MutableList<String>
) : Event(datasets, board, source) {
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy")

        fun fromTwitterArchiveTweet(dataset : String, board : String, tweet: TwitterArchiveTweet) : TweetEvent {
            val tweetEvent = TweetEvent(
                mutableListOf(dataset),
                board,
                Mirror.Source.Twitter,
                tweet.id_str,
                tweet.text,
                tweet.created_at,
                tweet.retweet_count,
                tweet.in_reply_to_user_id_str,
                tweet.favorite_count,
                tweet.is_retweet,
                if(tweet.is_retweet) tweet.text.substring(4, tweet.text.indexOf(':')) else "",
                mutableListOf()
            )

            return tweetEvent
        }
    }

    override fun Type(): String = "Tweet"

    override fun ID(): String = id_str

    override fun ThreadID() : String = in_reply_to_user_id_str?:""

    override fun Trip(): String = if(is_retweet) retweet_board else board

    override fun Link(): String {
        return "http://twitter.com/$board/status/$id_str"
    }

    override fun ReferenceID(): String = Link()

    override fun References(): List<String> = references

    override fun FindReferences() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun RawTimestamp(): String {
        return created_at
    }

    override fun Timestamp() : ZonedDateTime {
        return ZonedDateTime.parse(created_at, formatter).withZoneSameInstant(ZoneId.of("US/Eastern"))
    }

    override fun Subject(): String = ""

    override fun Text(): String {
        return text?:""
    }

    override fun Images(): List<Pair<String?, String?>> {
        return emptyList()
    }
}