package com.example.setlistpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.rememberCoroutineScope
import com.example.setlistpro.data.AppDatabase
import com.example.setlistpro.data.Setlist
import com.example.setlistpro.ui.EditSetlistScreen // Make sure to import your new Composable
import com.example.setlistpro.ui.Mode
import com.example.setlistpro.ui.theme.SetListProTheme
import kotlinx.coroutines.launch

class CreateSetlistActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)

        setContent {
            SetListProTheme {
                val scope = rememberCoroutineScope()

                EditSetlistScreen(
                    initialName = "", // Empty for creation
                    initialUris = emptyList(), // Empty for creation
                    mode = Mode.CREATE,
                    onCancel = { finish() },
                    onSave = { name, uris ->
                        scope.launch {
                            val newSetlist = Setlist(
                                name = name,
                                pdfUris = uris
                            )
                            db.setlistDao().insertSetlist(newSetlist)

                            // Close activity after saving
                            finish()
                        }
                    }
                )
            }
        }
    }
}
