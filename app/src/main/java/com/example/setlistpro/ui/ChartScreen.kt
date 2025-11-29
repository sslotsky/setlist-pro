package com.example.setlistpro.ui

import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.setlistpro.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ChartScreen(
    id: Int,
    chartIndex: Int
) {
    val db = AppDatabase.getDatabase(LocalContext.current)

    // 1. Collect the setlist from the DB
    val setlistState by db.setlistDao().getSetlistById(id).collectAsState(initial = null)
    val setlist = setlistState

    if (setlist != null && setlist.pdfUris.isNotEmpty()) {
        // 2. Calculate safe initial index
        val initialPage = chartIndex.coerceIn(0, setlist.pdfUris.lastIndex)

        // 3. Setup Pager for swiping left/right between charts
        val pagerState = rememberPagerState(
            initialPage = initialPage,
            pageCount = { setlist.pdfUris.size }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            // Optional: Add padding so you can see the edge of the next chart
            pageSpacing = 16.dp
        ) { page ->
            // 4. Display the specific PDF
            PdfViewer(uri = setlist.pdfUris[page])
        }
    } else {
        // Loading or Empty State
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun PdfViewer(uri: Uri) {
    val context = LocalContext.current

    // State to hold the list of bitmaps (one for each page of the PDF)
    var pages by remember(uri) { mutableStateOf<List<android.graphics.Bitmap>?>(null) }

    // Asynchronously render PDF
    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                    val renderer = PdfRenderer(descriptor)
                    val bitmaps = mutableListOf<android.graphics.Bitmap>()

                    for (i in 0 until renderer.pageCount) {
                        renderer.openPage(i).use { page ->
                            // Create bitmap for the page
                            // Using explicit config to ensure quality/compatibility
                            val bitmap = android.graphics.Bitmap.createBitmap(
                                page.width,
                                page.height,
                                android.graphics.Bitmap.Config.ARGB_8888
                            )

                            // Render content onto bitmap
                            page.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )
                            bitmaps.add(bitmap)
                        }
                    }
                    pages = bitmaps
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (pages != null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(pages!!) { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "PDF Page",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp) // Divider space between pages
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
