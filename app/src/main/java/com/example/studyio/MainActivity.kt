package com.example.studyio

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.studyio.ui.theme.StudyIOTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "StudyIO-MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Log build version at startup -- sanity check to make sure code change was applied (gradle clean -> gradle build)
        Log.i(TAG, "StudyIO Starting - 3:05PM LAST CHANGED")

        setContent {
            StudyIOTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudyIOApp()
                }
            }
        }
    }
}

@Composable
fun StudyIOApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val importViewModel: ImportViewModel = hiltViewModel()
    val decks by homeViewModel.decks.collectAsState()
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
                decks = decks,
                dueCards = 0, // TODO: wire up real values
                todayReviews = 0,
                totalCards = 0,
                totalDecks = decks.size,
                isImporting = isImporting,
                importMessage = importMessage,
                onDeckClick = { deck -> navController.navigate("decks/${deck.id}") },
                onCreateDeck = { navController.navigate("decks/create") },
                onStudyNow = {
                    val firstDeckId = decks.firstOrNull()?.id
                    if (firstDeckId != null) navController.navigate("quiz/decks/${firstDeckId}")
                },
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
                onCreateCardPressed = { navController.navigate("decks/${deckId}/cards/create") }
            )
        }
        composable("quiz/decks/{id}") { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            QuizScreen(
                deckId = deckId,
                onQuizComplete = { navController.popBackStack() } ,
            )
        }
        composable("decks/{deckId}/cards/create"){
                backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: error("missing deck id")
            CardCreateScreen(
                deckId = deckId.toLong(),
                onDeckSelected = { newDeckId ->
                    navController.popBackStack("decks/${deckId}", inclusive = true)
                    navController.navigate("decks/${newDeckId}")
                    navController.navigate("decks/${newDeckId}/cards/create")
                },
                onBackPressed = {
                    navController.popBackStack()
                },
                onCreatePressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudyIOAppPreview() {
    StudyIOTheme {
        StudyIOApp()
    }
}