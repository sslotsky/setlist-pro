package com.example.setlistpro

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.setlistpro.data.AppDatabase
import com.example.setlistpro.ui.theme.SetListProTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = AppDatabase.getDatabase(applicationContext)

        setContent {
            // Collect the Flow of setlists as Compose State
            // initial is empty list to prevent null issues while loading
            val setlists by db.setlistDao().getAllSetlists().collectAsState(initial = emptyList())
            val builder: AlertDialog.Builder = AlertDialog.Builder(LocalContext.current)

            SetListProTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        SmallFloatingActionButton(
                            onClick = {
                                val intent = Intent(this@MainActivity, CreateSetlistActivity::class.java)
                                startActivity(intent)
                            },
                        ) {
                            Icon(Icons.Filled.Add, "Create New Setlist")
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "My Setlists",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(setlists) { setlist ->
                                Box(
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                            .clickable {
                                                val intent = Intent(
                                                    this@MainActivity,
                                                    EditSetlistActivity::class.java
                                                )
                                                intent.putExtra("SETLIST_ID", setlist.id)
                                                startActivity(intent)
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = setlist.name,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "${setlist.pdfUris.size} songs",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }

                                    Icon(
                                        tint = Color(Color.Black.value),
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete ${setlist.name}",
                                        modifier = Modifier.offset(-(24).dp, 12.dp).clickable(onClick = {
                                            val dialog = builder
                                                .setMessage("Delete setlist ${setlist.name}?")
                                                .setTitle("Delete Setlist")
                                                .setPositiveButton("Delete") { dialog, _ ->
                                                    lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                        db.setlistDao().deleteSetlist(setlist)
                                                    }
                                                    dialog.dismiss()
                                                }
                                                .setNegativeButton("Cancel") { dialog, _ ->
                                                    dialog.dismiss()
                                                }
                                                .create()

                                            dialog.show()
                                        })
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
