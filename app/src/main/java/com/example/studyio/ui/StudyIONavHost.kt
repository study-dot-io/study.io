package com.example.studyio.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studyio.ui.home.HomeViewModel
import com.example.studyio.ui.importer.ImportViewModel
import com.example.studyio.ui.screens.CardCreateScreen
import com.example.studyio.ui.screens.CreateDeckScreen
import com.example.studyio.ui.screens.DeckDetailScreen
import com.example.studyio.ui.screens.HomeScreen
import com.example.studyio.ui.screens.QuizScreen

@Composable
fun StudyIONavHost() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val importViewModel: ImportViewModel = hiltViewModel()
    val isImporting by importViewModel.isImporting.collectAsState()
    val importMessage by importViewModel.importMessage.collectAsState()

    // Launcher for importing Anki APKG files
    val importApkgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                importViewModel.importApkg(
                    context = context,
                    uri = uri,
                    onComplete = { success, _ ->
                        if (success) homeViewModel.loadDecks()
                    }
                )
            }
        }
    )

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                isImporting = isImporting,
                importMessage = importMessage,
                onDeckClick = { deck -> navController.navigate("decks/${deck.id}") },
                onCreateDeck = { navController.navigate("decks/create") },
                onImportApkg = { importApkgLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
                onStudyNowForDeck = { deck -> navController.navigate("quiz/decks/${deck.id}") },
                onDeleteDeck = { deck -> homeViewModel.deleteDeck(deck.id) }
            )
        }
        composable("decks/create") {
            CreateDeckScreen(
                onBackPressed = {
                    navController.popBackStack()
                },
                onDeckCreated = { newDeck ->
                    homeViewModel.createDeck(newDeck) { navController.popBackStack() }
                }
            )
        }
        composable("decks/{id}") { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            DeckDetailScreen(
                deckId = deckId,
                onBack = { navController.popBackStack() },
                onCreateCardPressed = { navController.navigate("decks/$deckId/createCard") }
            )
        }
        composable("decks/{deckId}/createCard") { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId")?.toLongOrNull() ?: return@composable
            CardCreateScreen(
                deckId = deckId,
                onDeckSelected = { newDeckId ->
                    // We want to navigate back to the selected deck's detail screen
                    navController.popBackStack("decks/${deckId}", inclusive = true)
                    navController.navigate("decks/$newDeckId")
                    navController.navigate("decks/$newDeckId/createCard")
                },
                onBackPressed = { navController.popBackStack() },
                onCreatePressed = { navController.popBackStack() }
            )
        }
        composable("quiz/decks/{deckId}") { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId")?.toLongOrNull() ?: return@composable
            QuizScreen(
                deckId = deckId,
                onQuizComplete = { navController.popBackStack("home", inclusive = false) }
            )
        }
    }
}
