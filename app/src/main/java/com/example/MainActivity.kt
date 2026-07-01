package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.SleepDatabase
import com.example.data.SleepRepository
import com.example.ui.SleepFormScreen
import com.example.ui.SleepListScreen
import com.example.ui.SleepViewModel
import com.example.ui.SleepViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room Database & Repository
    val database = SleepDatabase.getDatabase(this)
    val repository = SleepRepository(database.sleepDao())
    val factory = SleepViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, factory)[SleepViewModel::class.java]

    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "list") {
          composable("list") {
            SleepListScreen(
              viewModel = viewModel,
              onAddLogClick = { navController.navigate("form") },
              onEditLogClick = { id -> navController.navigate("form/$id") }
            )
          }
          composable("form") {
            SleepFormScreen(
              viewModel = viewModel,
              logIdToEdit = null,
              onNavigateBack = { navController.popBackStack() }
            )
          }
          composable(
            route = "form/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
          ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id")
            SleepFormScreen(
              viewModel = viewModel,
              logIdToEdit = id,
              onNavigateBack = { navController.popBackStack() }
            )
          }
        }
      }
    }
  }
}
