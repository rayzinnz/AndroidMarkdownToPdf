package rayzinnz.markdowntopdf

data class PdfSettings(
    val baseFontSize: Float = 12f,
    val marginPoints: Float = 56.7f, // ~2cm
    val pageWidthPoints: Int = 595, // A4 Width
    val pageHeightPoints: Int = 842 // A4 Height
)
