package rayzinnz.markdowntopdf

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MarkdownScreen(
    viewModel: MarkdownViewModel = viewModel(),
    onSavePdf: () -> Unit
) {
    val markdownText by viewModel.markdownText.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val bottomMarginCm by viewModel.bottomMarginCm.collectAsState()
    val previewBitmaps by viewModel.previewBitmaps.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = markdownText,
            onValueChange = { viewModel.onMarkdownChange(it) },
            label = { Text("Markdown Text") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                val text = clipboardManager.getText()?.text
                if (text != null) {
                    viewModel.onMarkdownChange(text)
                }
            }) {
                Text("Paste")
            }

            Button(onClick = onSavePdf) {
                Text("Save PDF")
            }
        }

        Text("Zoom (Base Font Size: ${"%.1f".format(settings.baseFontSize)})")
        Slider(
            value = settings.baseFontSize,
            onValueChange = { viewModel.onZoomChange(it) },
            valueRange = 8f..20f,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Bottom Margin: ${"%.1f".format(bottomMarginCm)} cm")
        Slider(
            value = bottomMarginCm,
            onValueChange = { viewModel.onBottomMarginChange(it) },
            valueRange = 0.5f..4.0f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("PDF Preview", style = MaterialTheme.typography.titleMedium)
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(previewBitmaps) { bitmap ->
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "PDF Page",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
