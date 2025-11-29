package com.example.setlistpro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.Navigation.findNavController
import com.example.setlistpro.data.AppDatabase
import com.example.setlistpro.data.Setlist
import kotlinx.coroutines.launch

@Composable
fun CreateSetlistScreen(onFinish: () -> Unit) {
    val db = AppDatabase.getDatabase(LocalContext.current)

    val scope = rememberCoroutineScope()

    SetlistDetails(
        initialName = "", // Empty for creation
        initialUris = emptyList(), // Empty for creation
        mode = Mode.CREATE,
        onCancel = { onFinish() },
        onSave = { name, uris ->
            scope.launch {
                val newSetlist = Setlist(
                    name = name,
                    pdfUris = uris
                )
                db.setlistDao().insertSetlist(newSetlist)

                onFinish()
            }
        }
    )
}
