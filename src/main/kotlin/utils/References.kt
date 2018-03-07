package utils

fun FindAngleReferences(text : String) : List<Pair<String, String>> {
    val refs : MutableList<Pair<String,String>> = mutableListOf()

    if(!text.isEmpty()) {
        Regex(""".*>>(\d+).*""").findAll(text).forEach {
            val result = Pair("", it.groupValues[1])
            if(refs.find { it.first == result.first && it.second == result.second } == null) {
                refs.add(result)
            }
        }

        Regex(""".*>>>/?(\w+)/(\d+).*""").findAll(text).forEach {
            val result = Pair(it.groupValues[1], it.groupValues[2])
            if(refs.find { it.first == result.first && it.second == result.second } == null) {
                refs.add(result)
            }
        }
    }

    return refs
}

fun FindLinkReferences(text : String) : List<String> {
    val refs : MutableList<String> = mutableListOf()



    return refs
}