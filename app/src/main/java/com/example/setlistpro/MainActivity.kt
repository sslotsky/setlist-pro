package com.example.setlistpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.setlistpro.ui.PdfSelectionScreen
import com.example.setlistpro.ui.theme.SetListProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SetListProTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PdfSelectionScreen(
                        modifier = Modifier.padding(innerPadding),
                        onPdfSelected = { uri ->
                            // Handle the selected URI here
                            println("PDF selected: $uri")
                        }
                    )
                }
            }
        }
    }
}
