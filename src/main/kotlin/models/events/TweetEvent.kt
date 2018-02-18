package models.events

import models.mirror.TwitterArchiveTweet
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class TweetEvent(
        val host : String,
        val board : String,
        val id_str : String,
        val text : String?,
        val created_at : String,
        val retweet_count : Long,
        val in_reply_to_user_id_str : String?,
        val favorite_count : Long,
        val is_retweet : Boolean,
        private val references : MutableList<String>
) : Event() {
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy")

        fun fromTwitterArchiveTweet(board : String, tweet: TwitterArchiveTweet) : TweetEvent {
            val tweetEvent = TweetEvent(
                "twitterarchive.com",
                board,
                tweet.id_str,
                tweet.text,
                tweet.created_at,
                tweet.retweet_count,
                tweet.in_reply_to_user_id_str,
                tweet.favorite_count,
                tweet.is_retweet,
                mutableListOf()
            )

            return tweetEvent
        }
    }

    init {
        // TODO: detect references
    }

    override fun Host(): String = host

    override fun Type(): String = "Tweet"

    override fun ID(): String = id_str

    override fun Board(): String = board

    override fun Trip(): String = board

    override fun Link(): String {
        return "http://twitter.com/$board/status/$id_str"
    }

    override fun ReferenceID(): String = "$host-$board-$id_str"

    override fun References(): List<String> = references

    override fun RawTimestamp(): String {
        return created_at
    }

    override fun Timestamp() : ZonedDateTime {
        return ZonedDateTime.parse(created_at, formatter).withZoneSameInstant(ZoneId.of("US/Eastern"))
    }

    override fun Text(): String {
        return text?:""
    }

    override fun Images(): List<Pair<String?, String?>> {
        return emptyList()
    }
}