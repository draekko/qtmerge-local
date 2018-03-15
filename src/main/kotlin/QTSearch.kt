import controllers.mirror.InfChMirror
import controllers.mirror.Mirror
import models.events.Event

fun main(args: Array<String>) {
    QTSearch().Search()
}

// Searches local mirror for data and creates reports
class QTSearch {
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
                listOf(Mirror.SearchOperand.Content(regex), Mirror.SearchOperand.Condition(Mirror.SearchOperator.AndNot,
                        listOf(Mirror.SearchOperand.OP(), Mirror.SearchOperand.Content(Regex(""".*NEW Q archive.*""")))))))
        events.addAll(InfChMirror("greatawakening").MirrorSearch(params))
        events.addAll(InfChMirror("qresearch").MirrorSearch(params))
        events.addAll(InfChMirror("comms").MirrorSearch(params))
        println("Loaded ${events.size} events.")

        println("Creating links...")
        // Create links
        events.sortedBy { it.Timestamp() }.forEach { event ->
            println("=".repeat(80))
            println(event.ReferenceID())
            println(event.Text())
        }
    }
}