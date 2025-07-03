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
import com.example.studyio.ui.screens.DeckDetailScreen
import com.example.studyio.ui.theme.StudyIOTheme
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.example.studyio.data.entities.Note
import com.example.studyio.data.entities.StudyioDatabase
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.room.Room
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.net.Uri
import java.util.zip.ZipInputStream
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.json.JSONObject
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult

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

@Composable
fun DeckDetailScreen(deckId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(
            context,
            StudyioDatabase::class.java,
            "studyio.db"
        ).fallbackToDestructiveMigration(true).build()
    }
    val coroutineScope = rememberCoroutineScope()
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var field1 by remember { mutableStateOf("") }
    var field2 by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    LaunchedEffect(deckId) {
        coroutineScope.launch {
            notes = db.noteDao().getNotesForDeck(deckId)
        }
    }

    fun addNote() {
        coroutineScope.launch(Dispatchers.IO) {
            val note = Note(
                modelId = 1L,
                fields = listOf(field1, field2).joinToString("\u001F"),
                tags = tags.trim(),
                guid = UUID.randomUUID().toString()
            )
            val noteId = db.noteDao().insertNote(note)
            db.runInTransaction {
                db.compileStatement("INSERT INTO cards (deckId, noteId, ord, type, queue, due, interval, reps, lapses, createdAt, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)").apply {
                    bindLong(1, deckId)
                    bindLong(2, noteId)
                    bindLong(3, 0)
                    bindLong(4, 0)
                    bindLong(5, 0)
                    bindLong(6, 1L)
                    bindLong(7, 0)
                    bindLong(8, 0)
                    bindLong(9, 0)
                    bindString(10, LocalDateTime.now().toString())
                    bindLong(11, 1)
                    executeInsert()
                }
            }
            notes = db.noteDao().getNotesForDeck(deckId)
            showDialog = false
            field1 = ""
            field2 = ""
            tags = ""
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box {
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
                } else {
                    NotesGrid(notes = notes)
                }
            }
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Add Note") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = field1,
                                onValueChange = { field1 = it },
                                label = { Text("Field 1") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = field2,
                                onValueChange = { field2 = it },
                                label = { Text("Field 2") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = tags,
                                onValueChange = { tags = it },
                                label = { Text("Tags (space-separated)") },
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { addNote() },
                            enabled = field1.isNotBlank() || field2.isNotBlank()
                        ) { Text("Add") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                    }
                )
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

fun importAnkiApkg(context: Context, db: StudyioDatabase, uri: Uri) {
    val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    appScope.launch {
        // 1. Unzip .apkg
        val zipInput = ZipInputStream(context.contentResolver.openInputStream(uri))
        var collectionFile: File? = null
        while (true) {
            val entry = zipInput.nextEntry ?: break
            if (entry.name == "collection.anki2") {
                collectionFile = File.createTempFile("collection", ".anki2", context.cacheDir)
                collectionFile.outputStream().use { zipInput.copyTo(it) }
            }
            zipInput.closeEntry()
        }
        zipInput.close()
        if (collectionFile == null) return@launch

        // 2. Open SQLite
        val ankiDb = SQLiteDatabase.openDatabase(collectionFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

        // 3. Read col table for decks
        val colCursor = ankiDb.rawQuery("SELECT * FROM col", null)
        val decksMap = mutableMapOf<Long, Pair<String, String?>>()
        if (colCursor.moveToFirst()) {
            val decksJson = colCursor.getString(colCursor.getColumnIndexOrThrow("decks"))
            val decksObj = JSONObject(decksJson)
            for (key in decksObj.keys()) {
                val deckObj = decksObj.getJSONObject(key)
                val id = deckObj.getLong("id")
                val name = deckObj.getString("name")
                val desc = deckObj.optString("desc", null)
                decksMap[id] = name to desc
                // Insert deck
                db.deckDao().insertDeck(
                    com.example.studyio.data.entities.Deck(
                        id = id,
                        name = name,
                        description = desc
                    )
                )
            }
        }
        colCursor.close()

        // 4. Read notes
        val notesCursor = ankiDb.rawQuery("SELECT id, mid, flds, tags, guid FROM notes", null)
        val noteIdMap = mutableMapOf<Long, Long>() // Anki note id -> local note id
        while (notesCursor.moveToNext()) {
            val id = notesCursor.getLong(0)
            val modelId = notesCursor.getLong(1)
            val fields = notesCursor.getString(2)
            val tags = notesCursor.getString(3)
            val guid = notesCursor.getString(4)
            val note = com.example.studyio.data.entities.Note(
                id = id,
                modelId = modelId,
                fields = fields,
                tags = tags,
                guid = guid
            )
            db.noteDao().insertNote(note)
            noteIdMap[id] = id
        }
        notesCursor.close()

        // 5. Read cards
        val cardsCursor = ankiDb.rawQuery("SELECT id, nid, did, ord, type, queue, due, ivl, reps, lapses FROM cards", null)
        while (cardsCursor.moveToNext()) {
            val deckId = cardsCursor.getLong(2)
            val noteId = cardsCursor.getLong(1)
            val card = com.example.studyio.data.entities.Card(
                deckId = deckId,
                noteId = noteId,
                ord = cardsCursor.getInt(3),
                type = cardsCursor.getInt(4),
                queue = cardsCursor.getInt(5),
                due = cardsCursor.getInt(6),
                interval = cardsCursor.getInt(7),
                reps = cardsCursor.getInt(8),
                lapses = cardsCursor.getInt(9)
            )
            db.cardDao().insertCard(card)
        }
        cardsCursor.close()
        ankiDb.close()
        collectionFile.delete()
    }
} 