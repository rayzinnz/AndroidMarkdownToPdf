package rayzinnz.markdowntopdf

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
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
                    elements.add(MarkdownElement.Header(node.level, collectRichText(node)))
                }
                is Paragraph -> {
                    elements.add(MarkdownElement.Paragraph(collectRichText(node)))
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
            if (listItem is ListItem) {
                elements.add(MarkdownElement.ListItem(collectRichText(listItem), 1, ordered, if (ordered) count++ else null))
            }
            listItem = listItem.next
        }
    }

    private fun collectRichText(node: Node): CharSequence {
        val builder = SpannableStringBuilder()
        
        val visitor = object : AbstractVisitor() {
            override fun visit(text: Text) {
                builder.append(text.literal)
            }

            override fun visit(softLineBreak: SoftLineBreak) {
                builder.append(" ")
            }

            override fun visit(hardLineBreak: HardLineBreak) {
                builder.append("\n")
            }

            override fun visit(strongEmphasis: StrongEmphasis) {
                val start = builder.length
                visitChildren(strongEmphasis)
                val end = builder.length
                builder.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            override fun visit(emphasis: Emphasis) {
                val start = builder.length
                visitChildren(emphasis)
                val end = builder.length
                builder.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        
        node.accept(visitor)
        return builder
    }
}
