package com.example.studyio.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.studyio.data.entities.Deck
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyio.ui.home.HomeViewModel
import com.example.studyio.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    decks: List<Deck>,
    isImporting: Boolean = false,
    importMessage: String = "",
    onDeckClick: (Deck) -> Unit = {},
    onCreateDeck: () -> Unit = {},
    onImportApkg: (() -> Unit)? = null,
    onStudyNowForDeck: (Deck) -> Unit = {},
    onDeleteDeck: (Deck) -> Unit = {},
    onSignOut: (() -> Unit)? = null,
) {
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }
    
    val authViewModel: AuthViewModel = hiltViewModel()
    val user by authViewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "StudyIO",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            var fabExpanded by remember { mutableStateOf(false) }
            Box {
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Actions")
                }
                DropdownMenu(
                    expanded = fabExpanded,
                    onDismissRequest = { fabExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Create Deck") },
                        onClick = {
                            fabExpanded = false
                            onCreateDeck()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Import Anki Deck (.apkg)") },
                        onClick = {
                            fabExpanded = false
                            if (onImportApkg != null) onImportApkg()
                        },
                        enabled = !isImporting
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (user != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "👋 Welcome, ${user?.displayName ?: user?.email ?: "User"}!",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            authViewModel.signOut()
                            if (onSignOut != null) onSignOut()
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Sign Out")
                    }
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Import status card
                if (isImporting) {
                    item {
                        ImportStatusCard(
                            message = importMessage
                        )
                    }
                }

                item {
                    Text(
                        text = "Your Decks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(decks) { deck ->
                    DeckCard(
                        deck = deck,
                        onClick = { onDeckClick(deck) },
                        onReview = { onStudyNowForDeck(deck) },
                        onLongPress = { deckToDelete = deck }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                }
            }
        }

        // Loading overlay dialog
        if (isImporting) {
            Dialog(
                onDismissRequest = { /* Prevent dismissal during import */ },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                ImportLoadingDialog(message = importMessage)
            }
        }

        // Delete deck confirmation dialog
        deckToDelete?.let { deck ->
            AlertDialog(
                onDismissRequest = { deckToDelete = null },
                title = { Text("Delete Deck") },
                text = { Text("Are you sure you want to delete the deck '${deck.name}'?") },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteDeck(deck)
                        deckToDelete = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deckToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ImportLoadingDialog(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Importing Anki Deck",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ImportStatusCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.tertiary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Importing...",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun DeckCard(
    deck: Deck,
    onClick: () -> Unit,
    onReview: () -> Unit,
    onLongPress: () -> Unit // Replace onDelete with onLongPress
) {
    val viewModel: HomeViewModel = hiltViewModel()
    
    val cardCountMap by viewModel.cardCountMap.collectAsState()
    // check keys in the deck count map
    Log.d("DeckCard", "Deck Count Map: ${cardCountMap.keys.joinToString(", ")}")

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(deck.color.toColorInt()))
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Deck info (clickable for deck details)
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                deck.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Due Cards: ${cardCountMap[deck.id] ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            IconButton(onClick = onReview, enabled = (cardCountMap[deck.id] ?: 0) > 0) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Review")
            }
        }
    }
}