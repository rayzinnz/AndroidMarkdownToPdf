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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import rayzinnz.markdowntopdf.ui.theme.MarkdownToPdfTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MarkdownViewModel by viewModels()
    private lateinit var billingManager: BillingManager

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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingManager = BillingManager(this)
        enableEdgeToEdge()
        setContent {
            MarkdownToPdfTheme {
                var showMenu by remember { mutableStateOf(value = false) }
                val isBillingReady by billingManager.isReady.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("Markdown to PDF") },
                            actions = {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Buy the dev a coffee ($3)") },
                                        onClick = {
                                            showMenu = false
                                            billingManager.launchPurchaseFlow()
                                        },
                                        enabled = isBillingReady
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
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

    override fun onDestroy() {
        super.onDestroy()
        billingManager.endConnection()
    }
}
