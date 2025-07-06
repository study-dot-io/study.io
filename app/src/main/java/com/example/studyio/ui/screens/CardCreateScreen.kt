package com.example.studyio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.studyio.data.DatabaseProvider
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.Note
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardCreateScreen(
    deckId: Long,
    onDeckSelected: (Long) -> Unit,
    onBackPressed: () -> Unit,
    onCreatePressed: () -> Unit
) {
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val isFormValid = remember(frontText, backText) {
        frontText.trim().isNotEmpty() && backText.trim().isNotEmpty()
    }

    val context = LocalContext.current
    val db = remember {
        DatabaseProvider.getDatabase(context)
    }

    var availableDecks by remember { mutableStateOf<List<Deck>>(emptyList()) }
    var deck by remember { mutableStateOf<Deck?>(null) }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(deckId) {
        coroutineScope.launch {
            availableDecks = db.deckDao().getAllDecks()
            deck = availableDecks.find{deck -> deck.id == deckId} ?: error("current deck does not exist")
        }
    }

    fun createCard() {
        coroutineScope.launch {
            try {
                // Create the note first
                val newNote = Note.create(
                    modelId = 1L, // Basic card model
                    fields = listOf(frontText.trim(), backText.trim()),
                    tags = tags.trim()
                )

                // Insert the note and get its ID
                val noteId = db.noteDao().insertNote(newNote)

                // Create a card from the note
                val newCard = com.example.studyio.data.entities.Card(
                    deckId = deckId,
                    noteId = noteId,
                    ord = 0, // First template (front/back)
                    type = 0, // New card
                    queue = 0, // New card queue
                    due = 1 // New cards start with due=1
                )

                // Insert the card
                db.cardDao().insertCard(newCard)
            } catch (e: Exception) {
                println("Error creating card: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create New Card",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
            ) {
                TextField(
                    value = deck?.name ?: "Loading",
                    onValueChange = {}, // Read-only field
                    readOnly = true,
                    label = { Text("Select Deck") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    availableDecks.forEach { deck ->
                        DropdownMenuItem(
                            text = { Text(deck.name) },
                            onClick = {
                                onDeckSelected(deck.id)
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Front text input
            Column {
                Text(
                    text = "Front",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = frontText,
                    onValueChange = { frontText = it },
                    placeholder = {
                        Text("Enter front text...")
                    },
                    minLines = 3,
                    maxLines = 7,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Back text input
            Column {
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = backText,
                    onValueChange = { backText = it },
                    placeholder = {
                        Text("Enter back text...")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 7,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Tags text input
            Column {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    placeholder = {
                        Text("Enter Tags (space-separated)...")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 7,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            // Create Button
            Button(
                onClick = {
                    createCard()
                    onCreatePressed()
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create Card",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

