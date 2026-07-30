package rayzinnz.markdowntopdf

sealed class MarkdownElement {
    data class Header(val level: Int, val text: String) : MarkdownElement()
    data class Paragraph(val text: String) : MarkdownElement()
    data class ListItem(val text: String, val level: Int, val ordered: Boolean, val number: Int? = null) : MarkdownElement()
}
