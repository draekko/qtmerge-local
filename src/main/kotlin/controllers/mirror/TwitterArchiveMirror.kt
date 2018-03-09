package controllers.mirror

import com.google.gson.Gson
import extensions.iterate
import extensions.readBytesDelayed
import models.events.Event
import models.events.TweetEvent
import models.mirror.TwitterArchiveTweet
import settings.Settings.Companion.ZONEID
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

class TwitterArchiveMirror(
        outputDirectory : String,
        board : String,
        val startTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZONEID),
        val stopTime : ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZONEID)
) : Mirror(outputDirectory, board, Source.Twitter, "twitterarchive") {

    override fun Mirror() {
        println(">> mirror: $this")

        val mirrorRoot = mirrorDirectory + File.separator + dataset
        if (MakeDirectory(mirrorRoot)) {
            val boardRoot = mirrorRoot + File.separator + "boards" + File.separator + board.toLowerCase()
            val filesRoot = mirrorRoot + File.separator + "files"
            val years = (2017..2018)

            if (!MakeDirectory(boardRoot)) {
                return
            }
            if (!MakeDirectory(filesRoot)) {
                return
            }

            years.forEachIndexed { index, year ->
                println("  >> thread: $year: ${index + 1} / ${years.count()} (% ${Math.round(index.toFloat()/years.count()*100)})")
                val tweetURL = URL("http://trumptwitterarchive.com/data/${board.toLowerCase()}/$year.json")
                val tweetFile = File(boardRoot + File.separator + "$year.json")

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
                        MirrorReferences(filesRoot, tweet.id_str, tweet.text)
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
        val mirrorRoot = mirrorDirectory + File.separator + dataset
        val boardRoot = mirrorRoot + File.separator + "boards" + File.separator + board.toLowerCase()
        val years = (2017..2018)

        println(">> search: $this")
        years.forEachIndexed { index, year ->
            val tweetFile = File(boardRoot + File.separator + "$year.json")

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

        return eventList
    }
}
