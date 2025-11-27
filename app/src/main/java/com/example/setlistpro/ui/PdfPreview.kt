import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.createBitmap

@Composable
fun PdfPreview(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap = renderPdfFromUri(context, uri)

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "PDF Page",
            modifier = modifier
        )
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