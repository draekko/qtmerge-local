package controllers.mirror

import com.google.gson.Gson
import extensions.iterate
import extensions.readBytesDelayed
import models.events.Event
import models.events.TweetEvent
import models.mirror.TwitterArchiveTweet
import settings.Settings.Companion.DATADIR
import settings.Settings.Companion.ZONEID
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

class TwitterArchiveMirror(
        board : String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZONEID)
) : Mirror(board, Source.Twitter, "twitterarchive") {
    var mirrorLayout = MirrorLayout(DATADIR, dataset, "trumptwitterarchive.com", board.toLowerCase())

    override fun Mirror() {
        println(">> mirror: $this")

        if (MakeDirectory(mirrorLayout.root)) {
            val years = (2017..2018)

            if (!MakeDirectory(mirrorLayout.boards)) {
                return
            }
            if (!MakeDirectory(mirrorLayout.files)) {
                return
            }

            years.forEachIndexed { index, year ->
                println("  >> thread: $year: ${index + 1} / ${years.count()} (% ${Math.round(index.toFloat()/years.count()*100)})")
                val tweetURL = URL("http://trumptwitterarchive.com/data/${board.toLowerCase()}/$year.json")
                val tweetFile = File(mirrorLayout.boards + File.separator + "$year.json")

                // Update activity json if necessary
                var shouldUpdate = false
                try {
                    if (tweetFile.iterate(tweetURL.readBytesDelayed())) {
                        println("  Updated tweets for $board")
                        shouldUpdate = true
                    }
                } catch (e: FileNotFoundException) {
                    println("Unable to find tweets for $board: $e")
                    return
                }

                if(shouldUpdate) {
                    val tweets = Gson().fromJson(tweetFile.readText(), Array<TwitterArchiveTweet>::class.java)
                    tweets.forEach { tweet ->
                        MirrorReferences(mirrorLayout.files, tweet.id_str, tweet.text)
                    }
                }
            }
        }
    }

    override fun MirrorReferences() {
        println(">> mirror refs: $this")
        // TODO: implement
    }

    fun MirrorReferences(root : String, id : String, text : String) {
        // TODO:
        //   * Detect/fix broken links
        //   * Download links to tweet id folder:
        //      * Youtube videos
        //      * NewsEvent websites, web scrape
        //      * Twitter references
        //      * Etc.
        //   * Report unhandled links
        //
    }

    override fun MirrorSearch(params: SearchParameters): List<Event> {
        val eventList: MutableList<Event> = arrayListOf()
        val years = (2017..2018)

        println(">> search: $this")
        years.forEachIndexed { index, year ->
            val tweetFile = File(mirrorLayout.boards + File.separator + "$year.json")

            val tweets = Gson().fromJson(tweetFile.readText(), Array<TwitterArchiveTweet>::class.java)
            tweets.forEach { tweet ->
                val tweetEvent = TweetEvent.fromTwitterArchiveTweet(dataset, board, tweetFile.absolutePath, tweet)
                if (tweetEvent.Timestamp().toInstant().isAfter(startTime.toInstant()) &&
                        tweetEvent.Timestamp().toInstant().isBefore(stopTime.toInstant())) {
                    if(params.condition.Search(BoardExceptions(), tweetEvent)) {
                        // Add to event list if it isn't already there
                        if(eventList.find { it.Link() == tweetEvent.Link() } == null) {
                            eventList.add(tweetEvent)
                        }
                    }
                }
            }
        }

        println("  >> Found ${eventList.size} events.")

        return eventList
    }
}
