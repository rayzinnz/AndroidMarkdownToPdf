package rayzinnz.markdowntopdf

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class MarkdownViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val parser = MarkdownParser()
    private val generator = MarkdownPdfGenerator(application)
    private val storageManager = StorageManager(application)

    val markdownText: StateFlow<String> = savedStateHandle.getStateFlow("markdown_text", "")

    private val baseFontSize: StateFlow<Float> = savedStateHandle.getStateFlow("base_font_size", 12f)
    
    val bottomMarginCm: StateFlow<Float> = savedStateHandle.getStateFlow("bottom_margin_cm", 2.0f)

    private val _lastFileName = MutableStateFlow(storageManager.getLastFileName())
    val lastFileName: StateFlow<String> = _lastFileName.asStateFlow()

    val settings: StateFlow<PdfSettings> = combine(baseFontSize, bottomMarginCm) { fontSize, margin ->
        PdfSettings(
            baseFontSize = fontSize,
            bottomMarginPoints = margin * 28.3465f
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PdfSettings(baseFontSize = baseFontSize.value, bottomMarginPoints = bottomMarginCm.value * 28.3465f))

    private val _previewBitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val previewBitmaps: StateFlow<List<Bitmap>> = _previewBitmaps.asStateFlow()

    init {
        combine(markdownText, settings) { text, currentSettings ->
            text to currentSettings
        }.onEach { (text, currentSettings) ->
            if (text.isNotEmpty()) {
                val elements = parser.parse(text)
                val bitmaps = generator.generatePreviewBitmaps(elements, currentSettings)
                _previewBitmaps.value = bitmaps
            } else {
                _previewBitmaps.value = emptyList()
            }
        }.launchIn(viewModelScope)
    }

    fun onMarkdownChange(text: String) {
        savedStateHandle["markdown_text"] = text
    }

    fun onZoomChange(fontSize: Float) {
        savedStateHandle["base_font_size"] = fontSize
    }

    fun onBottomMarginChange(marginCm: Float) {
        savedStateHandle["bottom_margin_cm"] = marginCm
    }

    fun updateLastFileName(name: String) {
        storageManager.setLastFileName(name)
        _lastFileName.value = name
    }

    fun getPdfGenerator() = generator
    fun getParsedElements() = parser.parse(markdownText.value)
}
