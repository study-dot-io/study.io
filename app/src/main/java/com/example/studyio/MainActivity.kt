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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.studyio.data.entities.StudyioDatabase
import com.example.studyio.data.entities.buildStudyioDatabase
import com.example.studyio.data.importAnkiApkgFromStream
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studyio.ui.AppState
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

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "StudyIO-MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Log build version at startup
        Log.i(TAG, "StudyIO Starting - 5:03PM LAST CHANGED")
        Log.i(TAG, "App launched successfully")

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
    val db = remember { buildStudyioDatabase(context) }
    val coroutineScope = rememberCoroutineScope()
    var decks by remember { mutableStateOf<List<com.example.studyio.data.entities.Deck>>(emptyList()) }
    var isImporting by remember { mutableStateOf(false) }
    var importMessage by remember { mutableStateOf("") }

    // Load decks from the database
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            Log.d("StudyIO-App", "Loading decks from database...")
            decks = db.deckDao().getAllDecks()
            Log.d("StudyIO-App", "Loaded ${decks.size} decks from database")
        }
    }

    var appState by remember { mutableStateOf(AppState(decks = decks)) }
    val navController = rememberNavController()

    // Keep appState.decks in sync with DB
    LaunchedEffect(decks) {
        appState = appState.copy(decks = decks)
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val importApkgLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument(),
                onResult = { uri: Uri? ->
                    if (uri != null) {
                        Log.i("StudyIO-Import", "Starting import from URI: $uri")
                        isImporting = true
                        importMessage = "Preparing import..."
                        importAnkiApkg(
                            context = context,
                            db = db,
                            uri = uri,
                            onProgress = { message ->
                                importMessage = message
                                Log.d("StudyIO-Import", "Progress: $message")
                            },
                            onComplete = { success, message ->
                                isImporting = false
                                if (success) {
                                    Log.i("StudyIO-Import", "Import completed successfully: $message")
                                    // Refresh decks after successful import
                                    coroutineScope.launch(Dispatchers.IO) {
                                        decks = db.deckDao().getAllDecks()
                                        Log.d("StudyIO-Import", "Refreshed deck list, now have ${decks.size} decks")
                                    }
                                } else {
                                    Log.e("StudyIO-Import", "Import failed: $message")
                                }
                                importMessage = ""
                            }
                        )
                    } else {
                        Log.w("StudyIO-Import", "Import cancelled - no URI selected")
                    }
                }
            )
            HomeScreen(
                decks = appState.decks,
                dueCards = appState.dueCards,
                todayReviews = appState.todayReviews,
                totalCards = appState.totalCards,
                totalDecks = appState.totalDecks,
                isImporting = isImporting,
                importMessage = importMessage,
                onDeckClick = { deck ->
                    Log.d("StudyIO-Navigation", "Navigating to deck: ${deck.name} (ID: ${deck.id})")
                    navController.navigate("decks/${deck.id}")
                },
                onCreateDeck = {
                    Log.d("StudyIO-Navigation", "Navigating to create deck screen")
                    navController.navigate("decks/create")
                },
                onStudyNow = {
                    val firstDeckId = appState.decks.firstOrNull()?.id
                    if (firstDeckId != null) {
                        Log.d("StudyIO-Navigation", "Starting study session for deck ID: $firstDeckId")
                        navController.navigate("quiz/decks/${firstDeckId}")
                    } else {
                        Log.w("StudyIO-Navigation", "No decks available for study session")
                    }
                },
                onImportApkg = {
                    Log.d("StudyIO-Import", "Opening file picker for APKG import")
                    importApkgLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                },
                onStudyNowForDeck = { deck ->
                    Log.d("StudyIO-Navigation", "Starting study session for specific deck: ${deck.name} (ID: ${deck.id})")
                    navController.navigate("quiz/decks/${deck.id}")
                }
            )
        }
        composable("decks/create") {
            CreateDeckScreen(
                onBackPressed = {
                    Log.d("StudyIO-Navigation", "Navigating back from create deck screen")
                    navController.navigate("home") // popBackStack could be used
                },
                onDeckCreated = { newDeck ->
                    Log.i("StudyIO-Deck", "Creating new deck: ${newDeck.name}")
                    coroutineScope.launch(Dispatchers.IO) {
                        db.deckDao().insertDeck(newDeck)
                        decks = db.deckDao().getAllDecks()
                        Log.i("StudyIO-Deck", "Deck created successfully, total decks: ${decks.size}")
                    }
                    navController.navigate("home")
                }
            )
        }
        composable("decks/{id}"){
                backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("id") ?: error("missing deck id")
            Log.d("StudyIO-Navigation", "Viewing deck details for ID: $deckId")
            DeckDetailScreen(
                deckId = deckId.toLong(),
                onBack = {
                    Log.d("StudyIO-Navigation", "Navigating back from deck details")
                    navController.navigate("home")
                }
            )
        }
        composable("quiz/decks/{id}"){
                backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("id") ?: error("missing deck id")
            Log.d("StudyIO-Navigation", "Starting quiz for deck ID: $deckId")
            QuizScreen(
                deckId = deckId.toLong(),
                db = db,
                onQuizComplete = {
                    Log.d("StudyIO-Navigation", "Quiz completed, returning to home")
                    navController.navigate("home")
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

fun importAnkiApkg(
    context: Context,
    db: StudyioDatabase,
    uri: Uri,
    onProgress: (String) -> Unit = {},
    onComplete: (Boolean, String) -> Unit = { _, _ -> }
) {
    val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    appScope.launch {
        try {
            Log.i("StudyIO-Import", "Starting APKG import from URI: $uri")
            onProgress("Opening file...")

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                Log.d("StudyIO-Import", "Input stream opened successfully")
                onProgress("Reading APKG file...")

                importAnkiApkgFromStream(
                    inputStream = inputStream,
                    db = db,
                    tempDir = context.cacheDir,
                    onProgress = onProgress
                )

                Log.i("StudyIO-Import", "APKG import completed successfully")
                onComplete(true, "Import completed successfully!")
            } ?: run {
                Log.e("StudyIO-Import", "Failed to open input stream for URI: $uri")
                onComplete(false, "Failed to open selected file")
            }
        } catch (e: Exception) {
            Log.e("StudyIO-Import", "Import failed with exception", e)
            onComplete(false, "Import failed: ${e.message}")
        }
    }
}