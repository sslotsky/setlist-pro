package com.example.setlistpro.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap

@Composable
fun PdfPreview(
    uri: Uri,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    selectOnClick: Boolean = false,
    onSelected: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val bitmap = renderPdfFromUri(context, uri)
    val filename = getFileNameFromUri(context, uri)

    bitmap?.let {
        Box(contentAlignment = Alignment.TopEnd) {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = filename,
                    modifier = Modifier
                        .weight(1f)
                        .combinedClickable(
                            onLongClick = {
                                onSelected(!selected)
                            },
                            onClick = {
                                if (selected) {
                                    onSelected(false)
                                } else if (selectOnClick) {
                                    onSelected(true)
                                }
                            },
                            onDoubleClick = {
                                onSelected(!selected)
                            }
                        )
                )
                Text(filename)
            }
            if (selected)
                Icon(
                    tint = Color(Color.Gray.value),
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "$filename selected",
                    modifier = Modifier.offset(-(16).dp, 8.dp)

                )
        }
    }
}
fun renderPdfFromUri(context: Context, uri: Uri, pageIndex: Int = 0): Bitmap? {
    return try {
        // Open the URI directly as a ParcelFileDescriptor
        val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: return null

        val pdfRenderer = PdfRenderer(fileDescriptor)
        val page = pdfRenderer.openPage(pageIndex)

        // Create a bitmap to render into
        val bitmap = createBitmap(page.width, page.height)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        page.close()
        pdfRenderer.close()
        fileDescriptor.close()

        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String {
    // 1. Define the specific column we want to fetch to save memory/performance
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)

    try {
        val fileName: String? = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            // 2. Idiomatic: Return directly from the 'use' block
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }

        // 3. Return if found, otherwise proceed to fallback
        if (!fileName.isNullOrBlank()) return fileName

    } catch (e: Exception) {
        // 4. Catch security or provider errors to prevent crashes
        Log.e("FileUtils", "Failed to query file name", e)
    }

    // 5. Robust Fallback:
    // decode ensures %20 becomes space, etc.
    // lastPathSegment can be null, so we default to "unknown_file"
    return uri.lastPathSegment?.let { Uri.decode(it) } ?: "unknown_file"
}