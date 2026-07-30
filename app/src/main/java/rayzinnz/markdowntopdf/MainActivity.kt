package rayzinnz.markdowntopdf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import rayzinnz.markdowntopdf.ui.theme.MarkdownToPdfTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MarkdownViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarkdownToPdfTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MarkdownScreen(
                            viewModel = viewModel,
                            onSavePdf = {
                                val generator = viewModel.getPdfGenerator()
                                val elements = viewModel.getParsedElements()
                                val settings = viewModel.settings.value
                                val document = generator.generatePdf(elements, settings)
                                FileUtils.savePdfToDownloads(this@MainActivity, document, "Converted.pdf")
                                document.close()
                            }
                        )
                    }
                }
            }
        }
    }
}
