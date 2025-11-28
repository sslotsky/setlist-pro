package com.example.setlistpro

import android.app.AlertDialog
import android.content.Intent
import com.example.setlistpro.ui.PdfPreview
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.setlistpro.data.AppDatabase
import com.example.setlistpro.data.Setlist
import com.example.setlistpro.ui.PdfSelectionButton
import com.example.setlistpro.ui.theme.SetListProTheme
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState


class CreateSetlistActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = AppDatabase.getDatabase(applicationContext)

        setContent {
            val selectedFileUris = remember { mutableStateListOf<Uri>() }
            val pressedFileUris = remember { mutableStateListOf<Uri>() }

            val lazyGridState = rememberLazyGridState()
            val reorderableLazyGridState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
                selectedFileUris.add(to.index, selectedFileUris.removeAt(from.index))
            }
            val hapticFeedback = LocalHapticFeedback.current
            val setlistName = remember { mutableStateOf("text") }
            val canSubmit by remember {
                derivedStateOf {
                    setlistName.value.isNotEmpty() && selectedFileUris.isNotEmpty()
                }
            }

            val builder: AlertDialog.Builder = AlertDialog.Builder(LocalContext.current)
            builder
                .setMessage("Are you sure you want to remove the selected charts?")
                .setTitle("Remove charts")
                .setPositiveButton("Yes") { dialog, _ ->
                    pressedFileUris.forEach { uri ->
                        selectedFileUris.remove(uri)
                    }
                    pressedFileUris.clear()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }

            val dialog: AlertDialog = builder.create()
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            SetListProTheme {
                BackHandler(
                    enabled = pressedFileUris.isNotEmpty(),
                    onBack = {
                        pressedFileUris.clear()
                    }
                )
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomAppBar(
                            actions = {
                                IconButton(
                                    enabled = canSubmit,
                                    onClick = {
                                        println("submitting")
                                        scope.launch {
                                            val newSetlist = Setlist(
                                                name = setlistName.value,
                                                pdfUris = selectedFileUris.toList()
                                            )
                                            db.setlistDao().insertSetlist(newSetlist)

                                            // Close activity or show success message
                                            finish()
                                        }
                                    },
                                ) {
                                    Icon(Icons.Filled.Done, contentDescription = "Save set list")
                                }
                                if (pressedFileUris.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            dialog.show()
                                        },
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete selected charts")
                                    }
                                }
                            },
                            floatingActionButton = {
                                PdfSelectionButton(
                                    onPdfSelected = { uris ->
                                        uris.forEach { uri ->
                                            if (!selectedFileUris.contains(uri)) {
                                                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                context.contentResolver.takePersistableUriPermission(uri, takeFlags)

                                                selectedFileUris.add(uri)
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    },
                ) { innerPadding ->
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {

                        TextField(
                            value = setlistName.value,
                            onValueChange = { newText ->
                                setlistName.value = newText
                            },
                            label = { Text("Setlist Name") },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),

                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            contentPadding = PaddingValues(8.dp),
                            state = lazyGridState,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                count = selectedFileUris.size,
                                key = { index -> selectedFileUris.elementAt(index) },
                                itemContent = { index ->
                                    val uri = selectedFileUris.elementAt(index)
                                    ReorderableItem(reorderableLazyGridState, key = uri) { isDragging ->
                                        Box(
                                            modifier = Modifier
                                                .aspectRatio(0.7f)
                                                .draggableHandle(
                                                    onDragStarted = {
                                                        hapticFeedback.performHapticFeedback(
                                                            HapticFeedbackType.GestureThresholdActivate
                                                        )

                                                    },
                                                    onDragStopped = {
                                                        hapticFeedback.performHapticFeedback(
                                                            HapticFeedbackType.GestureEnd
                                                        )
                                                    },
                                                )
                                                .alpha(if (isDragging) 0.7f else 1.0f)
                                        ) {
                                            PdfPreview(uri = uri, onSelected = { isSelected ->
                                                if (isSelected && !pressedFileUris.contains(uri)) {
                                                    pressedFileUris.add(uri)
                                                } else if (!isSelected && pressedFileUris.contains(uri)) {
                                                    pressedFileUris.remove(uri)
                                                }
                                            }, selected = pressedFileUris.contains(uri), selectOnClick = pressedFileUris.isNotEmpty())
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

