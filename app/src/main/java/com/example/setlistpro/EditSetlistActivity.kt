package com.example.setlistpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.setlistpro.data.AppDatabase
import com.example.setlistpro.ui.EditSetlistScreen
import com.example.setlistpro.ui.theme.SetListProTheme
import kotlinx.coroutines.launch

class EditSetlistActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val setlistId = intent.getIntExtra("SETLIST_ID", -1)

        setContent {
            SetListProTheme {
                // Collect the setlist flow as state
                // You will need to ensure your DAO has a query: @Query("SELECT * FROM setlists WHERE id = :id")
                val existingSetlist by db.setlistDao()
                    .getSetlistById(setlistId)
                    .collectAsState(initial = null)

                val scope = rememberCoroutineScope()

                if (existingSetlist != null) {
                    // Since existingSetlist is nullable in state, we unwrap it here
                    val setlist = existingSetlist!!

                    EditSetlistScreen(
                        initialName = setlist.name,
                        initialUris = setlist.pdfUris,
                        onSave = { name, uris ->
                            scope.launch {
                                val updatedSetlist = setlist.copy(name = name, pdfUris = uris)
                                db.setlistDao().updateSetlist(updatedSetlist)
                                finish()
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
        }
    }
}
