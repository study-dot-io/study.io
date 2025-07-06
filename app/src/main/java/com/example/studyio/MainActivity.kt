package com.example.studyio

import android.content.Context
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.studyio.data.importAnkiApkgFromStream
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studyio.ui.home.HomeViewModel
import com.example.studyio.ui.screens.CreateDeckScreen
import com.example.studyio.ui.screens.DeckDetailScreen
import com.example.studyio.ui.screens.HomeScreen
import com.example.studyio.ui.screens.QuizScreen
import com.example.studyio.ui.theme.StudyIOTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "StudyIO-MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Log build version at startup
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
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val decks by homeViewModel.decks.collectAsState()
    var isImporting by remember { mutableStateOf(false) }
    var importMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val importApkgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                isImporting = true
                importMessage = "Preparing import..."
                importAnkiApkg(
                    context = context,
                    uri = uri,
                    onProgress = { message ->
                        importMessage = message
                    },
                    onComplete = { success, message ->
                        isImporting = false
                        if (success) {
                            homeViewModel.loadDecks()
                        }
                        importMessage = ""
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
                onBackPressed = { navController.navigate("home") },
                onDeckCreated = { newDeck ->
                    homeViewModel.createDeck(newDeck) { navController.navigate("home") }
                }
            )
        }
        composable("decks/{id}") { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            DeckDetailScreen(
                deckId = deckId,
                onBack = { navController.navigate("home") }
            )
        }
        composable("quiz/decks/{id}") { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            QuizScreen(
                deckId = deckId,
                db = null, // TODO: refactor QuizScreen to use ViewModel/Repository
                onQuizComplete = { navController.navigate("home") }
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

fun importAnkiApkg(
    context: Context,
    uri: Uri,
    onProgress: (String) -> Unit = {},
    onComplete: (Boolean, String) -> Unit = { _, _ -> }
) {
    val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    appScope.launch {
        try {
            onProgress("Opening file...")

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                onProgress("Reading APKG file...")

                importAnkiApkgFromStream(
                    inputStream = inputStream,
                    db = db,
                    tempDir = context.cacheDir,
                    onProgress = onProgress
                )

                onComplete(true, "Import completed successfully!")
            } ?: run {
                onComplete(false, "Failed to open selected file")
            }
        } catch (e: Exception) {
            onComplete(false, "Import failed: ${e.message}")
        }
    }
}