package rayzinnz.markdowntopdf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import rayzinnz.markdowntopdf.ui.theme.MarkdownToPdfTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MarkdownViewModel by viewModels()

    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let {
            val generator = viewModel.getPdfGenerator()
            val elements = viewModel.getParsedElements()
            val settings = viewModel.settings.value
            val document = generator.generatePdf(elements, settings)
            FileUtils.savePdfToUri(this, document, it)
            document.close()
            
            // Persist the filename if it was changed in the dialog
            it.lastPathSegment?.let { path ->
                val fileName = path.substringAfterLast("/")
                if (fileName.isNotEmpty()) {
                    viewModel.updateLastFileName(fileName)
                }
            }
        }
    }

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
                                createDocumentLauncher.launch(viewModel.lastFileName.value)
                            }
                        )
                    }
                }
            }
        }
    }
}
