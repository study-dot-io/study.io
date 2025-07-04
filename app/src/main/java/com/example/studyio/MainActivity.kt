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
import androidx.room.Room
import com.example.studyio.data.entities.StudyioDatabase
import com.example.studyio.data.importAnkiApkgFromStream
import com.example.studyio.ui.AppState
import com.example.studyio.ui.Screen
import com.example.studyio.ui.screens.CreateDeckScreen
import com.example.studyio.ui.screens.DeckDetailScreen
import com.example.studyio.ui.screens.HomeScreen
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
    val db = remember {
        Room.databaseBuilder(
            context,
            StudyioDatabase::class.java,
            "studyio.db"
        ).fallbackToDestructiveMigration(true).build()
    }
    val coroutineScope = rememberCoroutineScope()
    var decks by remember { mutableStateOf<List<com.example.studyio.data.entities.Deck>>(emptyList()) }

    // Load decks from the database
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            decks = db.deckDao().getAllDecks()
        }
    }

    var appState by remember { mutableStateOf(AppState(decks = decks)) }

    // Keep appState.decks in sync with DB
    LaunchedEffect(decks) {
        appState = appState.copy(decks = decks)
    }

    when (val screen = appState.currentScreen) {
        is Screen.Home -> {
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
                    appState = appState.navigateTo(Screen.DeckDetail(deck.id))
                },
                onCreateDeck = {
                    appState = appState.navigateTo(Screen.CreateDeck)
                },
                onStudyNow = {
                    // TODO: Navigate to study screen
                },
                onImportApkg = {
                    importApkgLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                }
            )
        }
        is Screen.CreateDeck -> {
            CreateDeckScreen(
                onBackPressed = {
                    appState = appState.navigateTo(Screen.Home)
                },
                onDeckCreated = { newDeck ->
                    coroutineScope.launch(Dispatchers.IO) {
                        db.deckDao().insertDeck(newDeck)
                        decks = db.deckDao().getAllDecks()
                    }
                    appState = appState.navigateTo(Screen.Home)
                }
            )
        }
        is Screen.DeckDetail -> {
            DeckDetailScreen(
                deckId = screen.deckId,
                onBack = { appState = appState.navigateTo(Screen.Home) }
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
