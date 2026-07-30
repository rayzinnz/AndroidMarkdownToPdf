package rayzinnz.markdowntopdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.File
import java.io.FileOutputStream

class MarkdownPdfGenerator(private val context: Context) {

    fun generatePdf(elements: List<MarkdownElement>, settings: PdfSettings): PdfDocument {
        val document = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(settings.pageWidthPoints, settings.pageHeightPoints, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        
        var currentY = settings.marginPoints
        val contentWidth = settings.pageWidthPoints - 2 * settings.marginPoints

        elements.forEach { element ->
            val layout = createLayout(element, settings, contentWidth.toInt())
            val elementHeight = layout.height.toFloat()

            // Check if element fits on current page
            if (currentY + elementHeight > settings.pageHeightPoints - settings.marginPoints) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(settings.pageWidthPoints, settings.pageHeightPoints, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = settings.marginPoints
            }

            canvas.save()
            canvas.translate(settings.marginPoints, currentY)
            layout.draw(canvas)
            canvas.restore()

            currentY += elementHeight + (settings.baseFontSize * 0.5f) // Spacing between elements
        }

        document.finishPage(page)
        return document
    }

    fun generatePreviewBitmaps(elements: List<MarkdownElement>, settings: PdfSettings): List<Bitmap> {
        val pdfDocument = generatePdf(elements, settings)
        val tempFile = File(context.cacheDir, "preview.pdf")
        pdfDocument.writeTo(FileOutputStream(tempFile))
        pdfDocument.close()

        val bitmaps = mutableListOf<Bitmap>()
        val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            // Use higher resolution for preview? A4 is ~600x840 points.
            // Screen density might matter. Let's try 2x for better quality.
            val bitmap = Bitmap.createBitmap(settings.pageWidthPoints * 2, settings.pageHeightPoints * 2, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.scale(2f, 2f)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmaps.add(bitmap)
            page.close()
        }
        renderer.close()
        pfd.close()
        return bitmaps
    }

    private fun createLayout(element: MarkdownElement, settings: PdfSettings, width: Int): StaticLayout {
        val textPaint = TextPaint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        val (text, fontSize, isBold) = when (element) {
            is MarkdownElement.Header -> {
                val size = when (element.level) {
                    1 -> settings.baseFontSize * 2f
                    2 -> settings.baseFontSize * 1.5f
                    3 -> settings.baseFontSize * 1.25f
                    else -> settings.baseFontSize * 1.1f
                }
                Triple(element.text, size, true)
            }
            is MarkdownElement.Paragraph -> {
                Triple(element.text, settings.baseFontSize, false)
            }
            is MarkdownElement.ListItem -> {
                val prefix = if (element.ordered) "${element.number}. " else "• "
                Triple(prefix + element.text, settings.baseFontSize, false)
            }
        }

        textPaint.textSize = fontSize
        textPaint.isFakeBoldText = isBold

        return StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.2f)
            .setIncludePad(false)
            .build()
    }
}
