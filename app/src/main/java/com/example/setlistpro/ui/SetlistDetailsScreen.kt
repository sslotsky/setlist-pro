package com.example.setlistpro.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.setlistpro.data.AppDatabase
import kotlinx.coroutines.launch

@Composable
fun SetlistDetailsScreen(
    setlistId: Int,
    navigateToChart: (Int) -> Unit
) {
    val db = AppDatabase.getDatabase(LocalContext.current)

    val existingSetlist by db.setlistDao()
        .getSetlistById(setlistId)
        .collectAsState(initial = null)

    val scope = rememberCoroutineScope()

    if (existingSetlist != null) {
        // Since existingSetlist is nullable in state, we unwrap it here
        val setlist = existingSetlist!!

        SetlistDetails(
            initialName = setlist.name,
            initialUris = setlist.pdfUris,
            mode = Mode.VIEW,
            openChart = navigateToChart,
            onSave = { name, uris ->
                scope.launch {
                    val updatedSetlist = setlist.copy(name = name, pdfUris = uris)
                    db.setlistDao().updateSetlist(updatedSetlist)
                }
            }
        )
    } else {
        // Show loading until the data is fetched from DB
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
