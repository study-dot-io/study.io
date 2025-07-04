package com.example.studyio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studyio.data.entities.Note
import com.example.studyio.data.entities.Card
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
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

    // Use Scaffold for proper screen structure and dialog overlay
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deck Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 16.dp) // Add horizontal padding for content
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            if (notes.isEmpty()) {
                Text("No cards in this deck.", style = MaterialTheme.typography.bodyLarge)
            } else {
                NotesGrid(notes = notes)
            }
        }

        // Show dialog for adding a new card; triggered by the FloatingActionButton
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Card") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = field1,
                            onValueChange = { field1 = it },
                            label = { Text("Front") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = field2,
                            onValueChange = { field2 = it },
                            label = { Text("Back") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tags,
                            onValueChange = { tags = it },
                            label = { Text("Tags (space-separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        coroutineScope.launch {
                            try {
                                // Create the note first
                                val newNote = Note.create(
                                    modelId = 1L, // Basic card model
                                    fields = listOf(field1.trim(), field2.trim()),
                                    tags = tags.trim()
                                )

                                // Insert the note and get its ID
                                val noteId = db.noteDao().insertNote(newNote)

                                // Create a card from the note
                                val newCard = Card(
                                    deckId = deckId,
                                    noteId = noteId,
                                    ord = 0, // First template (front/back)
                                    type = 0, // New card
                                    queue = 0, // New card queue
                                    due = 1 // New cards start with due=1
                                )

                                // Insert the card
                                db.cardDao().insertCard(newCard)

                                // Refresh the notes list
                                notes = db.noteDao().getNotesForDeck(deckId)

                                // Close dialog and clear fields
                                showDialog = false
                                field1 = ""
                                field2 = ""
                                tags = ""
                            } catch (e: Exception) {
                                // Handle error - you might want to show a toast or error message
                                println("Error creating card: ${e.message}")
                            }
                        }
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
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
                    val fields = note.fields.split("\u001F")
                    if (fields.size >= 2) {
                        Text("Front: ${fields[0]}", style = MaterialTheme.typography.bodyMedium)
                        Text("Back: ${fields[1]}", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        // Fallback for malformed fields
                        note.fields.split("\u001F").forEachIndexed { idx, field ->
                            Text("Field $idx: $field", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (note.tags.isNotBlank()) {
                        Text("Tags: ${note.tags}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}