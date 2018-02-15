package utils

class HTML {
    companion object {
        fun cleanHtmlText(html : String) : String {
            val emptyPattern = Regex("""<p class="body-line empty "></p>""")
            val referencePattern = Regex("""<a [^>]+>&gt;&gt;(\d+)</a>""")
            val linkPattern = Regex("""<a [^>]+>(.+?)</a>""")
            val quotePattern = Regex("""<p class="body-line ltr quote">&gt;(.+?)</p>""")
            val paragraphPattern = Regex("""<p class="body-line ltr ">(.+?)</p>""")

            return html.replace(emptyPattern, "\n")
                .replace(referencePattern, { m : MatchResult -> ">>${m.groupValues[1]}" })
                .replace(linkPattern, { m : MatchResult -> m.groupValues[1] })
                .replace(quotePattern, { m : MatchResult -> ">${m.groupValues[1]}\n"})
                .replace(paragraphPattern, { m: MatchResult -> "${m.groupValues[1]}\n"})
        }
    }
}