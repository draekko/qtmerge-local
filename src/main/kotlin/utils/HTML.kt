package utils

class HTML {
    companion object {
        fun cleanHTMLText(html : String) : String {
            val emptyPattern = Regex("""<p class="body-line empty "></p>""")
            val referencePattern = Regex("""(<a [^>]+>)?&gt;&gt;(\d+)(</a>)?""")
            val linkPattern = Regex("""<a [^>]+>(.+?)</a>""")
            val quotePattern = Regex("""<p class="body-line ltr quote">&gt;(.+?)</p>""")
            val paragraphPattern = Regex("""<p class="body-line ltr ">(.+?)</p>""")

            return html.replace(emptyPattern, "\n")
                .replace(referencePattern, { m : MatchResult -> ">>${m.groupValues[2]}" })
                .replace(linkPattern, { m : MatchResult -> m.groupValues[1] })
                .replace(quotePattern, { m : MatchResult -> ">${m.groupValues[1]}\n"})
                .replace(paragraphPattern, { m: MatchResult -> "${m.groupValues[1]}\n"})
        }

        // SO: 1265282
        fun escapeHTML(s: String): String {
            val out = StringBuilder(Math.max(16, s.length))
            for (i in 0 until s.length) {
                val c = s[i]
                if (c.toInt() > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                    out.append("&#")
                    out.append(c.toInt())
                    out.append(';')
                } else {
                    out.append(c)
                }
            }
            return out.toString()
        }
    }
}