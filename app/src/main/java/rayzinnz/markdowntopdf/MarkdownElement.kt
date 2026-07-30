package rayzinnz.markdowntopdf

sealed class MarkdownElement {
    data class Header(val level: Int, val text: CharSequence) : MarkdownElement()
    data class Paragraph(val text: CharSequence) : MarkdownElement()
    data class ListItem(val text: CharSequence, val level: Int, val ordered: Boolean, val number: Int? = null) : MarkdownElement()
}
