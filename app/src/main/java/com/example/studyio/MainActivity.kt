package com.example.studyio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.studyio.ui.AppState
import com.example.studyio.ui.Screen
import com.example.studyio.ui.screens.CreateDeckScreen
import com.example.studyio.ui.screens.HomeScreen
import com.example.studyio.ui.theme.StudyIOTheme
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.example.studyio.data.entities.Note
import com.example.studyio.data.entities.StudyioDatabase
import kotlinx.coroutines.launch
import com.example.studyio.data.entities.Card
import java.time.LocalDateTime
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape

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
    var appState by remember { mutableStateOf(AppState()) }
    
    when (val screen = appState.currentScreen) {
        is Screen.Home -> {
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
                }
            )
        }
        is Screen.CreateDeck -> {
            CreateDeckScreen(
                onBackPressed = {
                    appState = appState.navigateTo(Screen.Home)
                },
                onDeckCreated = { newDeck ->
                    appState = appState.addDeck(newDeck).navigateTo(Screen.Home)
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

@Composable
fun DeckDetailScreen(deckId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(
            context,
            StudyioDatabase::class.java,
            "studyio.db"
        ).build()
    }
    val coroutineScope = rememberCoroutineScope()
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var showMockButton by remember { mutableStateOf(false) }

    LaunchedEffect(deckId) {
        coroutineScope.launch {
            notes = db.noteDao().getNotesForDeck(deckId)
            showMockButton = notes.isEmpty()
        }
    }

    fun addMockNotes() {
        coroutineScope.launch {
            // Insert 3 mock notes and cards
            val mockNotes = listOf(
                Note(modelId = 1, fields = "Capital of France\u001FParis", tags = "geography"),
                Note(modelId = 1, fields = "Largest planet\u001FJupiter", tags = "astronomy"),
                Note(modelId = 1, fields = "Fastest land animal\u001FCheetah", tags = "biology")
            )
            val noteIds = mockNotes.map { db.noteDao().insertNote(it) }
            noteIds.forEachIndexed { idx, noteId ->
                db.runInTransaction {
                    db.compileStatement("INSERT INTO cards (deckId, noteId, ord, type, queue, due, interval, reps, lapses, createdAt, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)").apply {
                        bindLong(1, deckId)
                        bindLong(2, noteId)
                        bindLong(3, 0)
                        bindLong(4, 0)
                        bindLong(5, 0)
                        bindLong(6, idx + 1L)
                        bindLong(7, 0)
                        bindLong(8, 0)
                        bindLong(9, 0)
                        bindString(10, LocalDateTime.now().toString())
                        bindLong(11, 1)
                        executeInsert()
                    }
                }
            }
            notes = db.noteDao().getNotesForDeck(deckId)
            showMockButton = false
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Deck Detail", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = onBack) { Text("Back") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (notes.isEmpty()) {
                Text("No notes in this deck.", style = MaterialTheme.typography.bodyLarge)
                if (showMockButton) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { addMockNotes() }) { Text("Add Mock Notes") }
                }
            } else {
                NotesGrid(notes = notes)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesGrid(notes: List<Note>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(notes) { note ->
            NotePreviewCard(note)
        }
    }
}

@Composable
fun NotePreviewCard(note: Note) {
    val fields = note.fields.split('\u001F')
    val preview = if (fields.size > 1) "${fields[0]} — ${fields[1]}" else fields[0]
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = preview,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
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