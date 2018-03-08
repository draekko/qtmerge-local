import controllers.mirror.InfChMirror
import controllers.mirror.Mirror
import models.events.Event
import settings.Settings.Companion.MIRRORDIR
import java.io.File

fun main(args: Array<String>) {
    QTSearch().Search()
}

// Searches local mirror for data and creates reports
class QTSearch(
        mirrorLabel : String = "2018-02-15",
        private val mirrorDirectory: String = MIRRORDIR + File.separator + mirrorLabel
) {
    fun Search(
            //mirrors : List<Mirror>
            // TODO: Options:
            //    shallow copy references
            //    deep copy references
            //    include externals
            //    exclude isolated posts
            //    detect and merge reposts
            //    report type (plain text, csv, graphviz)
    ) {
        val regex = Regex(""".*(anonsw|qtmerge|q\.js).*""", RegexOption.IGNORE_CASE)
        println("Loading events...")
        // Load events that match search criteria and options
        val events : MutableList<Event> = arrayListOf()
        val params = Mirror.SearchParameters(Mirror.SearchOperand.Condition(Mirror.SearchOperator.And,
                listOf(Mirror.SearchOperand.Content(regex), Mirror.SearchOperand.Condition(Mirror.SearchOperator.Not,
                        listOf(Mirror.SearchOperand.OP(), Mirror.SearchOperand.Content(Regex(""".*NEW Q archive.*""")))))))
        events.addAll(InfChMirror(mirrorDirectory, "greatawakening").MirrorSearch(params))
        events.addAll(InfChMirror(mirrorDirectory, "qresearch").MirrorSearch(params))
        println("Loaded ${events.size} events.")

        println("Finding references...")
        // Enumerate events and find references
        val eventRefs : MutableList<Event> = arrayListOf()
        events.forEachIndexed { index, event ->
            event.UID = index.toString()
            event.FindReferences().forEach { event ->
                val existingEvent = events.find { it.ReferenceID() == event.ReferenceID() }
                if(existingEvent != null) {
                    eventRefs.add(existingEvent)
                } else {
                    if(eventRefs.find { it.ReferenceID() == event.ReferenceID() } == null) {
                        eventRefs.add(event)
                    }
                }
            }
        }
        // Enumerate references starting at end of matching events
        eventRefs.forEachIndexed { index, event ->
            event.UID = (events.size + index).toString()
        }
        println("Found references.")

        println("Creating links...")
        // Create links
        events.sortedBy { it.Timestamp() }.forEachIndexed { index, event ->
            println("=".repeat(80))
            println(event.ReferenceID())
            println(event.Text())
        }
    }
}