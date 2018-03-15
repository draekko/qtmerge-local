import com.google.gson.Gson
import com.google.gson.GsonBuilder
import controllers.mirror.*
import models.events.Event
import models.events.Event.Companion.MergeEvent
import models.mirror.ReferenceCache
import settings.Settings
import java.io.File
import java.time.Instant
import java.time.ZonedDateTime

fun main(args: Array<String>) {
    QTCache().UpdateCache()
}

// Updates the reference cache
class QTCache {
    val events: MutableList<Event> = mutableListOf()

    val mirrors = arrayListOf(
        // Twitter Archive
        Mirror.MirrorConfig(TwitterArchiveMirror("realDonaldTrump", Settings.STARTTIME), true),

        // Anonsw
        Mirror.MirrorConfig(FourPlebsMirror("pol", Settings.STARTTIME, ZonedDateTime.of(2017, 12, 14, 0, 0, 0, 0, Settings.ZONEID)), true),
        Mirror.MirrorConfig(InfChMirror("pol", Settings.STARTTIME, ZonedDateTime.of(2018, 2, 15, 0, 0, 0, 0, Settings.ZONEID)), true),
        Mirror.MirrorConfig(InfChMirror("cbts", Settings.STARTTIME, ZonedDateTime.of(2018, 1, 15, 0, 0, 0, 0, Settings.ZONEID)), true),
        Mirror.MirrorConfig(InfChMirror("thestorm", Settings.STARTTIME, ZonedDateTime.of(2018, 1, 15, 0, 0, 0, 0, Settings.ZONEID)), true),
        Mirror.MirrorConfig(InfChMirror("greatawakening", Settings.STARTTIME), true),
        Mirror.MirrorConfig(InfChMirror("qresearch", Settings.STARTTIME), true),
        Mirror.MirrorConfig(InfChMirror("comms", Settings.STARTTIME), true),

        // QCodeFag
        Mirror.MirrorConfig(QCodeFagMirror("pol", Mirror.Source.FourChan, "pol4chanPosts", Settings.STARTTIME), false),
        Mirror.MirrorConfig(QCodeFagMirror("cbts", Mirror.Source.InfChan, "cbtsNonTrip8chanPosts", Settings.STARTTIME), false),
        Mirror.MirrorConfig(QCodeFagMirror("cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts", Settings.STARTTIME), false),
        Mirror.MirrorConfig(QCodeFagMirror("pol", Mirror.Source.InfChan, "polTrip8chanPosts", Settings.STARTTIME), false),
        Mirror.MirrorConfig(QCodeFagMirror("thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts", Settings.STARTTIME), false),
        Mirror.MirrorConfig(QCodeFagMirror("greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts", Settings.STARTTIME), false),
        Mirror.MirrorConfig(QCodeFagMirror("qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts", Settings.STARTTIME), false),

        // QAnonMap
        Mirror.MirrorConfig(QAnonMapMirror("pol", Mirror.Source.FourChan, "pol4chanPosts", Settings.STARTTIME), false),
        Mirror.MirrorConfig(QAnonMapMirror("cbts", Mirror.Source.InfChan, "cbtsNonTrip8chanPosts", Settings.STARTTIME), false),
        Mirror.MirrorConfig(QAnonMapMirror("cbts", Mirror.Source.InfChan, "cbtsTrip8chanPosts", Settings.STARTTIME), false),
        Mirror.MirrorConfig(QAnonMapMirror("pol", Mirror.Source.InfChan, "polTrip8chanPosts", Settings.STARTTIME), false),
        Mirror.MirrorConfig(QAnonMapMirror("thestorm", Mirror.Source.InfChan, "thestormTrip8chanPosts", Settings.STARTTIME), false),
        Mirror.MirrorConfig(QAnonMapMirror("greatawakening", Mirror.Source.InfChan, "greatawakeningTrip8chanPosts", Settings.STARTTIME), false),
        Mirror.MirrorConfig(QAnonMapMirror("qresearch", Mirror.Source.InfChan, "qresearchTrip8chanPosts", Settings.STARTTIME), false),
        Mirror.MirrorConfig(QAnonMapMirror("qresearch", Mirror.Source.InfChan, "qresearchNonTrip8chanPosts", Settings.STARTTIME), false)
    )

    fun UpdateCache() {
        val gson = GsonBuilder().setPrettyPrinting().create()

        mirrors.forEach {
            if (it.primarySource) {
                events.addAll(it.mirror.MirrorSearch(Mirror.SearchParameters(Mirror.SearchOperand.All())))
            } else {
                it.mirror.MirrorSearch().forEach { event ->
                    MergeEvent(events, event, it.mirror.dataset)
                }
            }
        }

        events.sortBy { it.Timestamp().toEpochSecond() }

        println("Exporting event cache...")
        File(Settings.CACHEDIR + File.separator + "eventcache.json").bufferedWriter().use {
            gson.toJson(events, it)
        }
        File(Settings.CACHEDIR + File.separator + "eventcache-min.json").bufferedWriter().use {
            Gson().toJson(events, it)
        }

        val refCache = ReferenceCache()

        // Collect reference data
        println("Collecting references...")
        println(Instant.now())
        events.forEachIndexed { index, event ->
            if(index.rem(10000) == 0) {
                println("  $index / ${events.size} (% ${Math.round(index.toFloat() / events.size * 100)})")
            }
            val ref = ReferenceCache.Reference(event.ReferenceID(), event.Link(), event.Type(), event.Source(), event.Board(), event.ID())
            event.FindReferences().forEach { refPair ->
                when(refPair.first) {
                    ReferenceCache.ReferenceType.URL -> ref.references.add(refPair.second)
                    ReferenceCache.ReferenceType.BoardPost -> {
                        if(refCache.refs.containsKey(refPair.second)) {
                            ref.references.add(refPair.second)
                        }
                    }
                    ReferenceCache.ReferenceType.SitePost -> {
                        if(refCache.refs.containsKey(refPair.second)) {
                            ref.references.add(refPair.second)
                        }
                    }
                }
            }
            refCache.refs[event.ReferenceID()] = ref
        }
        println(Instant.now())

        println("Writing reference cache to disk...")
        File(Settings.CACHEDIR + File.separator + "refcache.json").bufferedWriter().use {
            gson.toJson(refCache, it)
        }
        File(Settings.CACHEDIR + File.separator + "refcache-min.json").bufferedWriter().use {
            Gson().toJson(refCache, it)
        }
        println("All done")
    }
}