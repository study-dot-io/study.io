package com.example.studyio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studyio.data.entities.Note
import com.example.studyio.data.entities.StudyioDatabase
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.time.LocalDateTime
import java.util.UUID
import com.example.studyio.data.entities.Card

@Composable
fun DeckDetailScreen(deckId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember {
        StudyioDatabase::class.java.let {
            androidx.room.Room.databaseBuilder(
                context,
                it,
                "studyio.db"
            ).fallbackToDestructiveMigration(true).build()
        }
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
            val card = Card(
                deckId = deckId,
                noteId = noteId,
                ord = 0,
                type = 0,
                queue = 0,
                due = 1,
                interval = 0,
                reps = 0,
                lapses = 0
            )
            db.cardDao().insertCard(card)
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
        }
    }
}

@Composable
fun NotesGrid(notes: List<Note>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(notes) { note ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    note.fields.split("\u001F").forEachIndexed { idx, field ->
                        Text("Field ${'$'}idx: ${'$'}field", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (note.tags.isNotBlank()) {
                        Text("Tags: ${'$'}{note.tags}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
} 