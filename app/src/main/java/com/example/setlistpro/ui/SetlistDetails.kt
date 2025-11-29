package com.example.setlistpro.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

enum class Mode {
    CREATE, VIEW
}

@Composable
fun SetlistDetails(
    initialName: String = "",
    initialUris: List<Uri> = emptyList(),
    mode: Mode,
    onSave: (String, List<Uri>) -> Unit,
    onCancel: () -> Unit = {},
    openChart: (Int) -> Unit = {}
) {
    val context = LocalContext.current

    // Initialize state with passed-in values
    val setlistName = remember { mutableStateOf(initialName) }
    val selectedFileUris = remember { mutableStateListOf<Uri>().apply { addAll(initialUris) } }
    val (isEditing, setIsEditing) = remember { mutableStateOf(mode == Mode.CREATE) }
    // Selection state for deletion
    val pressedFileUris = remember { mutableStateListOf<Uri>() }

    val lazyGridState = rememberLazyGridState()
    val reorderableLazyGridState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
        selectedFileUris.add(to.index, selectedFileUris.removeAt(from.index))
    }
    val hapticFeedback = LocalHapticFeedback.current

    val canSubmit by remember {
        derivedStateOf {
            setlistName.value.isNotEmpty() && selectedFileUris.isNotEmpty()
        }
    }

    // Delete Confirmation Dialog
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
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
                    if (mode == Mode.VIEW && !isEditing) {
                        IconButton(
                            onClick = {
                                setIsEditing(true)
                            },
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit set list")
                        }
                    }
                    if (mode == Mode.CREATE || isEditing) {
                        IconButton(
                            onClick = {
                                onCancel()

                                if (mode == Mode.VIEW) {
                                    setIsEditing(false)
                                    setlistName.value = initialName
                                    selectedFileUris.clear()
                                    selectedFileUris.addAll(initialUris)
                                    pressedFileUris.clear()
                                }
                            },
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel")
                        }
                        IconButton(
                            enabled = canSubmit,
                            onClick = {
                                onSave(setlistName.value, selectedFileUris.toList())
                                if (mode == Mode.VIEW) {
                                    setIsEditing(false)
                                }
                            },
                        ) {
                            Icon(Icons.Filled.Done, contentDescription = "Save set list")
                        }
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
                    if (isEditing) {
                        PdfSelectionButton(
                            onPdfSelected = { uris ->
                                uris.forEach { uri ->
                                    if (!selectedFileUris.contains(uri)) {
                                        // Handle permission persistence inside the screen or utility
                                        try {
                                            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        selectedFileUris.add(uri)
                                    }
                                }
                            }
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        val titleSize = MaterialTheme.typography.headlineMedium.fontSize.value
        val nameHeight = titleSize * 2

        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (isEditing) {
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
                        .height(nameHeight.dp)

                    )
            } else {
                Text(
                    text = setlistName.value,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(nameHeight.dp)
                    )
            }

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
                        ReorderableItem(reorderableLazyGridState, key = uri, enabled = isEditing) { isDragging ->
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
                                PdfPreview(
                                    uri = uri,
                                    onSelected = { isSelected ->
                                        if (isEditing) {
                                            if (isSelected && !pressedFileUris.contains(uri)) {
                                                pressedFileUris.add(uri)
                                            } else if (!isSelected && pressedFileUris.contains(uri)) {
                                                pressedFileUris.remove(uri)
                                            }
                                        }
                                    },
                                    selected = pressedFileUris.contains(uri),
                                    selectOnClick = pressedFileUris.isNotEmpty(),
                                    onClick = { openChart(index) }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
