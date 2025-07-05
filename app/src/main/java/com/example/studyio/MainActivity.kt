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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    // Load decks from the database
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            decks = db.deckDao().getAllDecks()
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
                        importAnkiApkg(context, db, uri)
                    }
                }
            )
            HomeScreen(
                decks = appState.decks,
                dueCards = appState.dueCards,
                todayReviews = appState.todayReviews,
                totalCards = appState.totalCards,
                totalDecks = appState.totalDecks,
                onDeckClick = { deck ->
                    navController.navigate("decks/${deck.id}")
                },
                onCreateDeck = {
                    navController.navigate("decks/create")
                },
                onStudyNow = {
                    val firstDeckId = appState.decks.firstOrNull()?.id
                    if (firstDeckId != null) {
                        navController.navigate("quiz/decks/${firstDeckId}")
                    }
                },
                onImportApkg = {
                    importApkgLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                },
                onStudyNowForDeck = { deck ->
                    navController.navigate("quiz/decks/${deck.id}")
                }
            )
        }
        composable("decks/create") {
            CreateDeckScreen(
                onBackPressed = {
                    navController.navigate("home") // popBackStack could be used
                },
                onDeckCreated = { newDeck ->
                    coroutineScope.launch(Dispatchers.IO) {
                        db.deckDao().insertDeck(newDeck)
                        decks = db.deckDao().getAllDecks()
                    }
                    navController.navigate("home")
                }
            )
        }
        composable("decks/{id}"){
            backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("id") ?: error("missing deck id")
            DeckDetailScreen(
                deckId = deckId.toLong(),
                onBack = { navController.navigate("home") }
            )
        }
        composable("quiz/decks/{id}"){
                backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("id") ?: error("missing deck id")
            QuizScreen(
                deckId = deckId.toLong(),
                db = db,
                onQuizComplete = {
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

fun importAnkiApkg(context: Context, db: StudyioDatabase, uri: Uri) {
    val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    appScope.launch {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            importAnkiApkgFromStream(inputStream, db, context.cacheDir)
        }
    }
}
