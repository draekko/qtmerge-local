package models.mirror

data class TwitterArchiveTweet(
    var source : String,
    var id_str : String,
    var text : String,
    var created_at : String,
    var retweet_count : Long,
    var in_reply_to_user_id_str : String?,
    var favorite_count : Long,
    var is_retweet : Boolean
)
