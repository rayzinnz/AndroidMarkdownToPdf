package rayzinnz.markdowntopdf

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarkdownParserTest {
    private val parser = MarkdownParser()

    @Test
    fun testParseHeaders() {
        val markdown = "# Header 1\n## Header 2"
        val elements = parser.parse(markdown)
        assertEquals(2, elements.size)
        assertEquals("Header 1", (elements[0] as MarkdownElement.Header).text.toString())
        assertEquals("Header 2", (elements[1] as MarkdownElement.Header).text.toString())
    }

    @Test
    fun testParseParagraphWithBold() {
        val markdown = "This is **bold** text."
        val elements = parser.parse(markdown)
        assertEquals(1, elements.size)
        val text = (elements[0] as MarkdownElement.Paragraph).text
        assertEquals("This is bold text.", text.toString())
    }

    @Test
    fun testParseList() {
        val markdown = "- Item 1\n- Item 2"
        val elements = parser.parse(markdown)
        assertEquals(2, elements.size)
        assertEquals("Item 1", (elements[0] as MarkdownElement.ListItem).text.toString())
        assertEquals("Item 2", (elements[1] as MarkdownElement.ListItem).text.toString())
    }
}
