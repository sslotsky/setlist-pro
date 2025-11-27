package com.example.setlistpro

import PdfPreview
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.setlistpro.ui.PdfSelectionButton
import com.example.setlistpro.ui.theme.SetListProTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val selectedFileUris = remember { mutableStateListOf<Uri>() }
            val lazyGridState = rememberLazyGridState()
            val reorderableLazyGridState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
                selectedFileUris.add(to.index, selectedFileUris.removeAt(from.index))
            }
            val hapticFeedback = LocalHapticFeedback.current


            SetListProTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        PdfSelectionButton(
                            onPdfSelected = { uris ->
                                uris.forEach { uri ->
                                    if (!selectedFileUris.contains(uri)) {
                                        selectedFileUris.add(uri)
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            contentPadding = PaddingValues(8.dp),
                            state = lazyGridState,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                count = selectedFileUris.size,
                                key = { index -> selectedFileUris.elementAt(index) },
                                itemContent = { index ->
                                    val uri = selectedFileUris.elementAt(index)
                                    ReorderableItem(reorderableLazyGridState, key = uri) { isDragging ->
                                        Box(
                                            modifier = Modifier.aspectRatio(0.7f).draggableHandle(
                                                onDragStarted = {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)

                                                },
                                                onDragStopped = {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                                },
                                            )
                                        ) {
                                            PdfPreview(uri = uri)
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

