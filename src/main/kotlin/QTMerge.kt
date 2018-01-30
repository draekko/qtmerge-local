import com.google.gson.Gson
import com.google.gson.GsonBuilder
import controllers.importer.QCodeFagImporter
import controllers.importer.TwitterArchiveImporter
import models.Event
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.util.CellRangeAddress
import java.io.File
import java.io.FileOutputStream
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// TODO: Switch to Kotson?

val DATADIR = System.getProperty("user.dir") + "/data"
val RESULTDIR = System.getProperty("user.dir") + "/anonsw.github.io/qtmerge"

fun main(args: Array<String>) {
    QTMerge()
}

class QTMerge {
    var events : MutableList<Event> = arrayListOf()

    init {
        LoadSources()

        CalcOffsets()

        File(RESULTDIR).mkdirs()
        ExportXls()
        ExportJson()
        ExportHtml()
    }

    fun LoadSources() {
        //events.addAll(TwitterArchiveImporter("$DATADIR/Trump").ImportLatest())
        events.addAll(TwitterArchiveImporter("$DATADIR/Trump/2017").ImportLatest())
        events.addAll(TwitterArchiveImporter("$DATADIR/Trump/2018").ImportLatest())
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").Import("cbts", false, "8ch.net", "cbtsNonTrip8chanPosts.json"))
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").Import("cbts", true, "8ch.net", "cbtsTrip8chanPosts.json"))
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").Import("greatawakening", true, "8ch.net", "greatawakeningTrip8chanPosts.json"))
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").Import("pol", false, "4chan.org", "pol4chanPosts.json"))
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").Import("pol", true, "8ch.net", "polTrip8chanPosts.json"))
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").Import("qresearch", true, "8ch.net", "qresearchTrip8chanPosts.json"))
        events.addAll(QCodeFagImporter("$DATADIR/QCodefag.github.io/data").Import("thestorm", true, "8ch.net", "thestormTrip8chanPosts.json"))

        events.sortBy { it.Timestamp().toEpochSecond() }

        // Initialize base class due to Gson ignoring Kotlin default constructor values
        events.forEach {event ->
            event.UID = UUID.randomUUID().toString()
            event.Deltas = arrayListOf()
        }

        println("Total Events: ${events.size}")
    }

    fun CalcOffsets() {
        val qq : MutableList<Event> = arrayListOf()
        var resetQQ = false
        events.forEach { event ->
            if(event.Type() == "QPost") {
                if(resetQQ) { qq.clear(); resetQQ = false }
                qq.add(event)
            } else if(event.Type() == "Tweet") {
                resetQQ = true
                event.Deltas.addAll(qq.reversed())
            }
        }
    }

    fun ExportXls() {
        var lastTweet : ZonedDateTime? = null
        var lastQpost : ZonedDateTime? = null
        val wb = HSSFWorkbook()
        val out = FileOutputStream("$RESULTDIR/qtmerge.xls")
        val sheet = wb.createSheet("Timeline")
        sheet.isDisplayGridlines = false
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
        val linkStyle = wb.createCellStyle()
        val linkFont = wb.createFont()
        linkFont.underline = Font.U_SINGLE
        linkFont.color = IndexedColors.BLUE.index
        linkStyle.setFont(linkFont)
        val headerFont = wb.createFont()
        headerFont.bold = true
        val headerStyle = wb.createCellStyle()
        headerStyle.setFont(headerFont)
        val headerCenterStyle = wb.createCellStyle()
        headerCenterStyle.setFont(headerFont)
        headerCenterStyle.setAlignment(HorizontalAlignment.CENTER)
        val centerStyle = wb.createCellStyle()
        centerStyle.setAlignment(HorizontalAlignment.CENTER)
        val centerBoldStyle = wb.createCellStyle()
        centerBoldStyle.setAlignment(HorizontalAlignment.CENTER)
        centerBoldStyle.setFont(headerFont)
        val leftStyle = wb.createCellStyle()
        leftStyle.setAlignment(HorizontalAlignment.LEFT)
        val centerRedStyle = wb.createCellStyle()
        centerRedStyle.setAlignment(HorizontalAlignment.CENTER)
        val redFont = wb.createFont()
        redFont.color = HSSFColor.RED.index
        centerRedStyle.setFont(redFont)

        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Time")
        header.getCell(0).setCellStyle(headerStyle)
        header.createCell(1).setCellValue("Trip")
        header.getCell(1).setCellStyle(headerStyle)
        header.createCell(2).setCellValue("Link")
        header.getCell(2).setCellStyle(headerStyle)
        header.createCell(3).setCellValue("Preview")
        header.getCell(3).setCellStyle(headerStyle)
        header.createCell(4).setCellValue("Minute")
        header.getCell(4).setCellStyle(headerStyle)
        header.createCell(5).setCellValue("Diffs")
        header.getCell(5).setCellStyle(headerStyle)
        val lastQTS : MutableList<ZonedDateTime?> = mutableListOf()
        var rowIndex = 1
        var maxCol = 5
        var qcount = 1
        events.reversed().forEach {
            val row = sheet.createRow(rowIndex)

            //if(it.Type() == "Tweet" && rowIndex < 10)
            //println("${it.RawTimestamp()} = ${it.Timestamp().format(formatter)}")
            row.createCell(0).setCellValue(it.Timestamp().format(formatter))

            row.createCell(1).setCellValue(it.Trip())

            val refcell = row.createCell(2)
            refcell.setCellValue(it.Type())
            val link = wb.creationHelper.createHyperlink(HyperlinkType.URL)
            link.address = it.Reference()
            refcell.hyperlink = link
            refcell.setCellStyle(linkStyle)

            var text = it.Text().replace("\n", " ")
            if(text.length > 30) {
                text = text.substring(0, 30) + "..."
            }
            row.createCell(3).setCellValue(text)

            row.createCell(4).setCellValue(it.Timestamp().minute.toDouble())
            row.getCell(4).setCellStyle(centerStyle)

            if(it.Type() == "QPost") {
                row.createCell(5).setCellValue(qcount.toDouble())
                row.getCell(5).setCellStyle(centerBoldStyle)
                qcount++
            } else {
                qcount = 1
            }

            when(it.Type()) {
                "Tweet" -> { lastTweet = it.Timestamp() }
                "QPost" -> { lastQpost = it.Timestamp() }
            }
            rowIndex++
        }
        val qtCol : MutableList<ZonedDateTime> = arrayListOf()
        var clearQtCol = false
        rowIndex = events.size
        events.forEach {event ->
            val row = sheet.getRow(rowIndex)
            if(event.Type() == "QPost") {
                if(clearQtCol) { qtCol.clear(); clearQtCol = false }
                qtCol.add(0, event.Timestamp())
                //row.createCell(5).setCellValue(qtCol.size.toDouble())
                //row.getCell(5).setCellStyle(centerBoldStyle)
            } else if(event.Type() == "Tweet") {
                var col = 6
                qtCol.forEachIndexed { idx, time ->
                    //row.createCell(5 + (qtCol.size - idx)).setCellValue((event.Timestamp().minute - time.minute).toDouble())
                    //row.getCell(5 + (qtCol.size - idx)).setCellStyle(centerStyle)
                    row.createCell(col).setCellValue((event.Timestamp().minute - time.minute).toDouble())
                    if (col == 6 && !clearQtCol) {
                        row.getCell(col).setCellStyle(centerRedStyle)
                    } else {
                        row.getCell(col).setCellStyle(centerStyle)
                    }
                    if(col > maxCol) maxCol = col
                    col++
                }
                clearQtCol = true
            }
            rowIndex--
        }
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(2)
        sheet.autoSizeColumn(3)
        sheet.autoSizeColumn(4)
        (5..maxCol).forEach {
            sheet.autoSizeColumn(it)
        }
        (6..maxCol).forEach {
            header.createCell(it).setCellValue((it-5).toDouble())
            header.getCell(it).setCellStyle(headerCenterStyle)
        }
        sheet.createFreezePane(0, 1)
        sheet.setAutoFilter(CellRangeAddress(0, events.size, 0, 4))

        wb.write(out)
        out.close()
    }

    fun ExportJson() {
        val gson = GsonBuilder().setPrettyPrinting().create()
        File("$RESULTDIR/qtmerge-pretty.json").writeText(gson.toJson(events.sortedBy { it.Timestamp().toEpochSecond() }))
        File("$RESULTDIR/qtmerge.json").writeText(Gson().toJson(events.sortedBy { it.Timestamp().toEpochSecond() }))
    }

    fun ExportHtml() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
        val out = File("$RESULTDIR/qtmerge.html").outputStream().bufferedWriter()
        out.append("""<!doctype html>
            |<html lang="en">
            |   <head>
            |       <meta charset="utf-8">
            |       <title>qtmerge</title>
            |       <link rel="stylesheet" href="../styles/screen.css">
            |       <script type="text/javascript" src="../scripts/jquery-3.3.1.min.js"></script>
            |   </head>
            |   <body>
            |   <p class="timestamp">Last Updated: ${ZonedDateTime.now(ZoneId.of("US/Eastern")).format(formatter)}</p>
            |   <p class="downloads">
            |       <a href="qtmerge.xls">qtmerge.xls</a> |
            |       <a href="qtmerge.json">qtmerge.json</a> (<a href="qtmerge-pretty.json">qtmerge-pretty.json</a>)
            |   </p>
            |""".trimMargin())

        out.append("""<table>
            |   <tr>
            |       <th>Time</th>
            |       <th>Trip</th>
            |       <th>Link</th>
            |       <th>Text</th>
            |       <th>Minute</th>
            |       <th>Diffs To Last QPost</th>
            |   </tr>
            |""".trimMargin())

        events.reversed().forEach {
            out.appendln("  <tr id=\"${it.UID}\">")
            out.appendln("      <td class=\"e-timestamp\">${it.Timestamp().format(formatter)}</td>")
            out.appendln("      <td class=\"e-trip\">${it.Trip()}</td>")
            out.appendln("      <td class=\"e-type\"><a href=\"${it.Reference()}\">${it.Type()}</a></td>")
            var images = ""
            it.Images().forEach {
                if (it.first != null) {
                    images = """<a href="${it.first}">"""
                }
                if (it.second != null) {
                    images += it.second
                } else if (it.first != null) {
                    images += it.first
                }
                if (it.first != null) {
                    images += "</a>"
                }
            }
            if(images.isNotEmpty() && it.Text().isNotEmpty()) {
                images += "<br>"
            }
            out.appendln("        <td class=\"e-text\">$images${it.Text()}</td>")
            out.appendln("        <td class=\"e-minute\">${it.Timestamp().minute}</td>")
            var diffs = ""
            it.Deltas.forEach { delta ->
                diffs += "<span data-id=\"${delta.UID}\" class=\"delta\">${it.Timestamp().minute - delta.Timestamp().minute}</span> "
            }
            out.appendln("        <td class=\"e-diffs\">$diffs</td>")
            out.appendln("    </tr>")
        }

        out.appendln("</table>")
        out.append("""<script type="text/javascript" src="qtmerge.js"></script>""")
        out.append("</body></html>")
        out.close()
    }
}
