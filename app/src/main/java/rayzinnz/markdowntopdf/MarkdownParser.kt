package rayzinnz.markdowntopdf

import org.commonmark.node.*
import org.commonmark.parser.Parser

class MarkdownParser {
    private val parser = Parser.builder().build()

    fun parse(markdown: String): List<MarkdownElement> {
        val document = parser.parse(markdown)
        val elements = mutableListOf<MarkdownElement>()
        
        var node = document.firstChild
        while (node != null) {
            when (node) {
                is Heading -> {
                    elements.add(MarkdownElement.Header(node.level, collectText(node)))
                }
                is Paragraph -> {
                    elements.add(MarkdownElement.Paragraph(collectText(node)))
                }
                is BulletList -> {
                    processList(node, elements, false)
                }
                is OrderedList -> {
                    processList(node, elements, true)
                }
            }
            node = node.next
        }
        return elements
    }

    private fun processList(listNode: Node, elements: MutableList<MarkdownElement>, ordered: Boolean) {
        var listItem = listNode.firstChild
        var count = 1
        while (listItem != null) {
            if (listItem is org.commonmark.node.ListItem) {
                // For simplicity, we only take the first paragraph of the list item
                val text = collectText(listItem)
                elements.add(MarkdownElement.ListItem(text, 1, ordered, if (ordered) count++ else null))
            }
            listItem = listItem.next
        }
    }

    private fun collectText(node: Node): String {
        val sb = StringBuilder()
        node.accept(object : AbstractVisitor() {
            override fun visit(text: Text) {
                sb.append(text.literal)
            }
            override fun visit(softLineBreak: SoftLineBreak) {
                sb.append(" ")
            }
            override fun visit(hardLineBreak: HardLineBreak) {
                sb.append("\n")
            }
        })
        return sb.toString()
    }
}
