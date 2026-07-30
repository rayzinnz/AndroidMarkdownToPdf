package rayzinnz.markdowntopdf

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarkdownViewModel(application: Application) : AndroidViewModel(application) {
    private val parser = MarkdownParser()
    private val generator = MarkdownPdfGenerator(application)

    private val _markdownText = MutableStateFlow("")
    val markdownText: StateFlow<String> = _markdownText.asStateFlow()

    private val _settings = MutableStateFlow(PdfSettings())
    val settings: StateFlow<PdfSettings> = _settings.asStateFlow()

    private val _previewBitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val previewBitmaps: StateFlow<List<Bitmap>> = _previewBitmaps.asStateFlow()

    fun onMarkdownChange(text: String) {
        _markdownText.value = text
        updatePreview()
    }

    fun onZoomChange(fontSize: Float) {
        _settings.value = _settings.value.copy(baseFontSize = fontSize)
        updatePreview()
    }

    fun updatePreview() {
        viewModelScope.launch {
            val elements = parser.parse(_markdownText.value)
            val bitmaps = generator.generatePreviewBitmaps(elements, _settings.value)
            _previewBitmaps.value = bitmaps
        }
    }

    fun getPdfGenerator() = generator
    fun getParsedElements() = parser.parse(_markdownText.value)
}
