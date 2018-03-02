import controllers.mirror.InfChMirror
import controllers.mirror.Mirror
import models.events.Event
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {
    val qtSearch = QTSearch()

    qtSearch.Search()
}

// Searches local mirror for data and creates reports
class QTSearch(
        mirrorLabel : String = "2018-02-15",
        private val inputDirectory : String = DATADIR + File.separator + mirrorLabel
) {
    companion object {
        val ZONEID = ZoneId.of("US/Eastern")
        val VERSION = "2018.2-1"
        val DATADIR = System.getProperty("user.dir") + File.separator + "mirror"
        val STARTTIME : ZonedDateTime = ZonedDateTime.of(2017, 10, 28, 0, 0, 0, 0, ZoneId.of("US/Eastern"))
        val DATEFORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
    }

    fun Search(
            // TODO: Search by Q, trip, name, and/or content
            // TODO: Options:
            //    shallow copy references
            //    deep copy references
            //    include externals
            //    exclude isolated posts
            //    detect and merge reposts
            //    report type (plain text, csv, graphviz)
    ) {
        val regex = Regex(""".*anonsw.*""", RegexOption.IGNORE_CASE)
        println("Loading events...")
        // Load events that match search criteria and options
        val events : MutableList<Event> = arrayListOf()
        val params = Mirror.SearchParameters(content = regex, referenceDepth = Mirror.ReferenceDepth.Shallow, onlyQT = false)
        events.addAll(InfChMirror(inputDirectory, "greatawakening").MirrorSearch(params))
        events.addAll(InfChMirror(inputDirectory, "qresearch").MirrorSearch(params))
        println("Loaded ${events.size} events.")

        println("Finding references...")
        // Number events
        events.forEachIndexed { index, event ->
            event.UID = index.toString()
            event.FindReferences()
        }
        println("Found references.")

        println("Creating links...")
        // Create links
        events.forEachIndexed { index, event ->
            println(event.ReferenceID())
            println(event.References())
            println(event.Text())
        }
    }
}