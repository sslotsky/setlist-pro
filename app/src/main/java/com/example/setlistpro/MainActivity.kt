package com.example.setlistpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.setlistpro.ui.ChartScreen
import com.example.setlistpro.ui.CreateSetlistScreen
import com.example.setlistpro.ui.SetlistDetailsScreen
import com.example.setlistpro.ui.SetlistsScreen
import com.example.setlistpro.ui.theme.SetListProTheme
import kotlinx.serialization.Serializable

@Serializable
object BrowseSetlists
@Serializable
object CreateSetlist
@Serializable
data class SetlistDetails(val id: Int)
@Serializable
data class Chart(val id: Int, val chartIndex: Int)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            SetListProTheme {
                NavHost(navController = navController, startDestination = BrowseSetlists) {
                    composable<BrowseSetlists> {
                        SetlistsScreen(
                            goToCreate = { navController.navigate(CreateSetlist) },
                            goToDetails = { id -> navController.navigate(SetlistDetails(id)) }
                        )
                    }
                    composable<SetlistDetails> { backStackEntry ->
                        val details: SetlistDetails = backStackEntry.toRoute()
                        SetlistDetailsScreen(details.id, navigateToChart = { index ->
                            navController.navigate(Chart(details.id, index))
                        })
                    }
                    composable<CreateSetlist> {
                        CreateSetlistScreen(
                            onFinish = { navController.popBackStack() }
                        )
                    }

                    composable<Chart> { backStackEntry ->
                        val chart: Chart = backStackEntry.toRoute()
                        ChartScreen(chart.id, chart.chartIndex)
                    }
                }
            }
        }
    }
}
