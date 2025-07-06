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
import com.example.studyio.data.DatabaseProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(deckId: Long, onBack: () -> Unit, onCreateCardPressed: () -> Unit) {
    val context = LocalContext.current
    val db = remember {
        DatabaseProvider.getDatabase(context)
    }
    val coroutineScope = rememberCoroutineScope()
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }

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
                onClick = onCreateCardPressed
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