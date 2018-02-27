package models.events

import models.importer.QCodeFagPost
import models.mirror.InfChPost
import utils.HTML.Companion.cleanHTMLText
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.xml.bind.DatatypeConverter

data class PostEvent(
        var dataset : String,
        var board : String,
        var id : String,
        var userId : String?,
        var timestamp : Long,
        var title : String?,
        var name : String?,
        var email : String?,
        var trip : String?,
        var text : String?,
        var subject : String?,
        var link : String,
        var threadId : String,
        var images : MutableList<PostEventImage>?,
        var postReferences: MutableList<PostEvent>,
        var anonVerifiedAsQ: MutableList<Int>,
        private var references : MutableList<String>,
        private var referenceID : String
) : Event() {
    data class PostEventImage(
            var url : String?,
            var filename : String?
    )

    companion object {
        fun makeReferenceID(link : String) = link //DatatypeConverter.printHexBinary(MD5.digest(link.toByteArray()))

        fun fromInfChPost(dataset : String, board : String, infChPost: InfChPost) : PostEvent {
            val threadId = if(infChPost.resto == 0L) infChPost.no else infChPost.resto
            val link = "https://8ch.net/$board/res/$threadId.html#${infChPost.no}"
            val postEvent = PostEvent(
                    dataset,
                    board,
                    infChPost.no.toString(),
                    infChPost.id,
                    infChPost.time,
                    infChPost.title,
                    infChPost.name,
                    infChPost.email,
                    infChPost.trip,
                    cleanHTMLText(infChPost.com),
                    infChPost.sub,
                    link,
                    infChPost.resto.toString(),
                    mutableListOf(),
                    mutableListOf(),
                    mutableListOf(),
                    mutableListOf(),
                    makeReferenceID(link)
            )

            if(infChPost.tim?.isNotEmpty() == true) {
                postEvent.images!!.add(PostEventImage("https://media.8ch.net/file_store/${infChPost.tim}${infChPost.ext}", infChPost.filename))
            }
            infChPost.extra_files?.forEach {
                postEvent.images!!.add(PostEventImage("https://media.8ch.net/file_store/${it.tim}${it.ext}", it.filename))
            }

            return postEvent
        }

        fun fromQCodeFagPost(dataset : String, board : String, qCodeFagPost: QCodeFagPost) : PostEvent {
            // 1 - https://archive.4plebs.org/pol/thread/148136485/#148136656
            // 2 - https://archive.4plebs.org/pol/thread/148146734/#148147343
            // 3 - https://archive.4plebs.org/pol/thread/148777785/#148779656
            // 4 - https://archive.4plebs.org/pol/thread/148866159/#148872136
            // 5 - https://archive.4plebs.org/pol/thread/149080733/#149083850
            // 6 - https://archive.4plebs.org/pol/thread/150171127/#150171513
            // 7 - https://archive.4plebs.org/pol/thread/150171127/#150171863
            // 8 - https://8ch.net/qresearch/res/387596.html#387700
            val graphicPostIds : Map<String, List<Pair<String, List<Int>>>> = mapOf(
                    Pair("4chan_pol", listOf(
                            Pair("147023341", listOf(            5      )),
                            Pair("147012719", listOf(            5      )),
                            Pair("147104628", listOf(         4, 5      )),
                            Pair("147106598", listOf(         4, 5      )),
                            Pair("147109593", listOf(         4, 5      )),
                            Pair("147166292", listOf(      3, 4, 5      )),
                            Pair("147167304", listOf(      3, 4, 5      )),
                            Pair("147169329", listOf(      3, 4, 5      )),
                            Pair("147170576", listOf(      3, 4, 5      )),
                            Pair("147173287", listOf(      3, 4, 5      )),
                            Pair("147175452", listOf(      3, 4, 5      )),
                            Pair("147181191", listOf(      3, 4, 5      )),
                            Pair("147181801", listOf(      3, 4, 5      )),
                            Pair("147433975", listOf(1, 2, 3, 4, 5      )),
                            Pair("147434025", listOf(1, 2, 3, 4, 5      )),
                            Pair("147437247", listOf(1, 2, 3, 4, 5      )),
                            Pair("147440171", listOf(1, 2, 3, 4, 5      )),
                            Pair("147441378", listOf(1, 2, 3, 4, 5      )),
                            Pair("147443190", listOf(1, 2, 3, 4, 5      )),
                            Pair("147444335", listOf(1, 2, 3, 4, 5      )),
                            Pair("147444934", listOf(1, 2, 3, 4, 5      )),
                            Pair("147445681", listOf(1, 2, 3, 4, 5      )),
                            Pair("147446992", listOf(1, 2, 3, 4, 5      )),
                            Pair("147448408", listOf(1, 2, 3, 4, 5      )),
                            Pair("147449010", listOf(1, 2, 3, 4, 5      )),
                            Pair("147449624", listOf(1, 2, 3, 4, 5      )),
                            Pair("147450817", listOf(1, 2, 3, 4, 5      )),
                            Pair("147451052", listOf(1, 2, 3, 4, 5      )),
                            Pair("147452214", listOf(1, 2, 3, 4, 5      )),
                            Pair("147453147", listOf(1, 2, 3, 4, 5      )),
                            Pair("147454188", listOf(1, 2, 3, 4, 5      )),
                            Pair("147454631", listOf(1, 2, 3, 4, 5      )),
                            Pair("147455196", listOf(1, 2, 3, 4, 5      )),
                            Pair("147567888", listOf(1, 2, 3, 4, 5      )),
                            Pair("147567928", listOf(1, 2, 3, 4, 5      )),
                            Pair("147581302", listOf(1, 2, 3, 4, 5      )),
                            Pair("147581516", listOf(1, 2, 3, 4, 5      )),
                            Pair("147586045", listOf(1, 2, 3, 4, 5      )),
                            Pair("147588085", listOf(1, 2, 3, 4, 5      )),
                            Pair("147588421", listOf(1, 2, 3, 4, 5      )),
                            Pair("147590619", listOf(1, 2, 3, 4, 5      )),
                            Pair("147591125", listOf(1, 2, 3, 4, 5      )),
                            Pair("147591663", listOf(1, 2, 3, 4, 5      )),
                            Pair("147592019", listOf(1, 2, 3, 4, 5      )),
                            Pair("147632662", listOf(1, 2, 3, 4, 5      )),
                            Pair("147634822", listOf(1, 2, 3, 4, 5      )),
                            Pair("147636035", listOf(1, 2, 3, 4, 5      )),
                            Pair("147640127", listOf(1, 2, 3, 4, 5      )),
                            Pair("147641320", listOf(1, 2, 3, 4, 5      )),
                            Pair("147642680", listOf(1, 2, 3, 4, 5      )),
                            Pair("147643257", listOf(1, 2, 3, 4, 5      )),
                            Pair("147645024", listOf(1, 2, 3, 4, 5      )),
                            Pair("147646189", listOf(1, 2, 3, 4, 5      )),
                            Pair("147646606", listOf(1, 2, 3, 4, 5      )),
                            Pair("147647154", listOf(1, 2, 3, 4, 5      )),
                            Pair("147661217", listOf(1, 2, 3, 4, 5      )),
                            Pair("147661243", listOf(1, 2, 3, 4, 5      )),
                            Pair("147661332", listOf(1, 2, 3, 4, 5      )),
                            Pair("147664082", listOf(1, 2, 3, 4, 5      )),
                            Pair("147679416", listOf(1, 2, 3, 4, 5      )),
                            Pair("147680054", listOf(1, 2, 3, 4, 5      )),
                            Pair("147680749", listOf(1, 2, 3, 4, 5      )),
                            Pair("147681912", listOf(1, 2, 3, 4, 5      )),
                            Pair("147683156", listOf(1, 2, 3, 4, 5      )),
                            Pair("147687684", listOf(1, 2, 3, 4, 5      )),
                            Pair("147689362", listOf(1, 2, 3, 4, 5      )),
                            Pair("147816901", listOf(1, 2, 3, 4, 5      )),
                            Pair("147817468", listOf(1, 2, 3, 4, 5      )),
                            Pair("147819733", listOf(1, 2, 3, 4, 5      )),
                            Pair("147975558", listOf(1, 2, 3, 4, 5      )),
                            Pair("147979863", listOf(1, 2, 3, 4, 5      )),
                            Pair("147981354", listOf(1, 2, 3, 4, 5      )),
                            Pair("147986661", listOf(         4, 5      )),
                            Pair("147987614", listOf(         4, 5      )),
                            Pair("148016618", listOf(1, 2, 3, 4, 5      )),
                            Pair("148016670", listOf(                   )),
                            Pair("148016731", listOf(                   )),
                            Pair("148016769", listOf(                   )),
                            Pair("148016876", listOf(                   )),
                            Pair("148019575", listOf(1, 2, 3, 4, 5      )),
                            Pair("148019905", listOf(1, 2, 3, 4, 5      )),
                            Pair("148020085", listOf(                   )),
                            Pair("148020278", listOf(1, 2, 3, 4, 5      )),
                            Pair("148021501", listOf(1, 2, 3, 4, 5      )),
                            Pair("148022145", listOf(1, 2, 3, 4, 5      )),
                            Pair("148022342", listOf(1, 2, 3, 4, 5      )),
                            Pair("148023976", listOf(1, 2, 3, 4, 5      )),
                            Pair("148025825", listOf(1, 2, 3, 4, 5      )),
                            Pair("148027165", listOf(1, 2, 3, 4, 5      )),
                            Pair("148029633", listOf(1, 2, 3, 4, 5      )),
                            Pair("148029962", listOf(1, 2, 3, 4, 5      )),
                            Pair("148031295", listOf(1, 2, 3, 4, 5      )),
                            Pair("148032210", listOf(1, 2, 3, 4, 5      )),
                            Pair("148032910", listOf(1, 2, 3, 4, 5      )),
                            Pair("148033178", listOf(1, 2, 3, 4, 5      )),
                            Pair("148033932", listOf(1, 2, 3, 4, 5      )),
                            Pair("148139234", listOf(   2, 3, 4, 5      )),
                            Pair("148139484", listOf(   2, 3, 4, 5      )),
                            Pair("148143472", listOf(   2, 3, 4, 5      )),
                            Pair("148143562", listOf(   2, 3, 4, 5      )),
                            Pair("148148004", listOf(         4, 5      )),
                            Pair("148149435", listOf(         4, 5      )),
                            Pair("148152047", listOf(         4, 5      )),
                            Pair("148154137", listOf(         4, 5      )),
                            Pair("148154941", listOf(         4, 5      )),
                            Pair("148155343", listOf(         4, 5      )),
                            Pair("148155609", listOf(         4, 5      )),
                            Pair("148156129", listOf(         4, 5      )),
                            Pair("148156632", listOf(         4, 5      )),
                            Pair("148156937", listOf(         4, 5      )),
                            Pair("148183670", listOf(      3, 4, 5      )),
                            Pair("148185083", listOf(      3, 4, 5      )),
                            Pair("148185905", listOf(      3, 4, 5      )),
                            Pair("148186256", listOf(      3, 4, 5      )),
                            Pair("148189295", listOf(                   )),
                            Pair("148286961", listOf(      3, 4, 5      )),
                            Pair("148287184", listOf(      3, 4, 5      )),
                            Pair("148287236", listOf(      3, 4, 5      )),
                            Pair("148287326", listOf(      3, 4, 5      )),
                            Pair("148287396", listOf(      3, 4, 5      )),
                            Pair("148287473", listOf(      3, 4, 5      )),
                            Pair("148287529", listOf(      3, 4, 5      )),
                            Pair("148289594", listOf(         4, 5      )),
                            Pair("148452545", listOf(      3            )),
                            Pair("148453749", listOf(      3, 4, 5      )),
                            Pair("148455482", listOf(      3, 4, 5      )),
                            Pair("148457032", listOf(      3, 4, 5      )),
                            Pair("150170117", listOf(               6, 7)),
                            Pair("150170181", listOf(               6, 7)),
                            Pair("150171298", listOf(                  7))
                    ))
            )
            val qmapRefs = mutableListOf<Int>()
            if(graphicPostIds.containsKey(qCodeFagPost.source) && (graphicPostIds[qCodeFagPost.source]!!.find { it.first == qCodeFagPost.id } != null)) {
                qmapRefs.addAll(graphicPostIds[qCodeFagPost.source]!!.find { it.first == qCodeFagPost.id }!!.second)
            }
            val postEvent = PostEvent(
                    dataset,
                    board,
                    qCodeFagPost.id,
                    qCodeFagPost.userId,
                    qCodeFagPost.timestamp,
                    qCodeFagPost.title,
                    qCodeFagPost.name,
                    qCodeFagPost.email,
                    qCodeFagPost.trip,
                    qCodeFagPost.text,
                    qCodeFagPost.subject,
                    qCodeFagPost.link,
                    qCodeFagPost.threadId?:"",
                    mutableListOf(),
                    mutableListOf(),
                    qmapRefs,
                    mutableListOf(),
                    makeReferenceID(qCodeFagPost.link)
            )

            if(qCodeFagPost.images?.isNotEmpty() == true) {
                qCodeFagPost.images!!.forEach {
                    postEvent.images!!.add(PostEventImage(it.url, it.filename))
                }
            }

            // Fix extra slashes in link
            postEvent.link = postEvent.link.replace(Regex("""(?<!https?:)/+"""), "/")

            return postEvent
        }

    }

    override fun FindReferences() {
        references.addAll(postReferences.map { it.ReferenceID() })

        // Find post references
        if(!text.isNullOrEmpty()) {
            Regex(""".*>>(\d+).*""").findAll(text ?: "").forEach {
                if(!references.contains(it.groupValues[1])) {
                    references.add(it.groupValues[1])
                }
            }
        }
    }

    override fun Dataset(): String = dataset

    override fun Type(): String = "Post"

    override fun ID(): String = id

    override fun Board(): String = board

    override fun Trip(): String = if(trip.isNullOrEmpty()) "Anonymous" else trip?:""

    override fun Link(): String = link

    override fun ReferenceID(): String = referenceID

    override fun References(): List<String> = references

    override fun RawTimestamp(): String = timestamp.toString()

    override fun Timestamp(): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("US/Eastern"))
    }

    override fun Text(): String = text?:""

    override fun Images(): List<Pair<String?, String?>> {
        if(images == null) return emptyList()
        return images!!.map { Pair(it.url, it.filename) }
    }
}