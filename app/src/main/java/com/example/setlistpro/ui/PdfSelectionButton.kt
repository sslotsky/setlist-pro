package com.example.setlistpro.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable

@Composable
fun PdfSelectionButton(
    onPdfSelected: (List<Uri>) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uri: List<Uri>? ->
        uri?.let {
            onPdfSelected(it)
        }
    }
    SmallFloatingActionButton(
        onClick = { launcher.launch(arrayOf("application/pdf")) },
    ) {
        Icon(Icons.Filled.Add, "Import PDF button")
    }
}
