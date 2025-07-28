package com.example.studyio.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.DeckState
import com.example.studyio.ui.auth.AuthViewModel
import com.example.studyio.ui.home.DeckTab
import com.example.studyio.ui.home.HomeViewModel
import com.example.studyio.ui.screens.components.DeckCard
import com.example.studyio.ui.screens.components.DeckManagementModal
import com.example.studyio.ui.screens.components.ImportLoadingDialog
import com.example.studyio.ui.screens.components.ImportStatusCard
import com.example.studyio.ui.screens.components.uploadDocumentToLLM
import com.example.studyio.ui.screens.components.CreateLLMDeckAndCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isImporting: Boolean = false,
    importMessage: String = "",
    onDeckClick: (Deck) -> Unit = {},
    onCreateDeck: () -> Unit = {},
    onImportApkg: (() -> Unit)? = null,
    onStudyNowForDeck: (Deck) -> Unit = {},
    onDeleteDeck: (Deck) -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
) {
    var deckToManage by remember { mutableStateOf<Deck?>(null) }
    val authViewModel: AuthViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val user by authViewModel.currentUser.collectAsState()
    val activeDecks by homeViewModel.activeDecks.collectAsState()
    val archivedDecks by homeViewModel.archivedDecks.collectAsState()
    val selectedTab by homeViewModel.selectedTab.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Tab(
                    selected = selectedTab == DeckTab.ACTIVE,
                    onClick = { homeViewModel.setSelectedTab(DeckTab.ACTIVE) },
                    text = { Text("Active Decks") }
                )
                Tab(
                    selected = selectedTab == DeckTab.ARCHIVED,
                    onClick = { homeViewModel.setSelectedTab(DeckTab.ARCHIVED) },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Archived")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            var fabExpanded by remember { mutableStateOf(false) }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            
            // State to hold the LLM result when document is uploaded
            var uploadedDocumentResult by remember { mutableStateOf<Map<String, Any>?>(null) }
            
            val documentLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null && user != null) {
                    scope.launch {
                        val result = uploadDocumentToLLM(user!!, context, uri)
                        if (result != null) {
                            uploadedDocumentResult = result
                        }
                    }
                }
            }
            
            // Once the document is uploaded, create deck and card
            uploadedDocumentResult?.let {
                CreateLLMDeckAndCard(it)
                uploadedDocumentResult = null
            }
            
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
                    DropdownMenuItem(
                        text = { Text("Upload Document to LLM") },
                        onClick = {
                            fabExpanded = false
                            if (user != null) {
                                documentLauncher.launch("*/*")
                            } else {
                                // Navigate to auth screen if user is not signed in
                                onNavigateToAuth()
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            val decksToShow = if (selectedTab == DeckTab.ACTIVE) activeDecks else archivedDecks
            val emptyMessage = if (selectedTab == DeckTab.ACTIVE) 
                "You don't have any active decks yet. Create one to get started!" 
            else 
                "You don't have any archived decks."
            
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
                    // User authentication status
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (user != null) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.secondaryContainer
                        ),
                        onClick = if (user == null) onNavigateToAuth else { {} }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (user != null) "Signed in as:" else "Not signed in",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (user != null) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = if (user != null) 
                                        user!!.email ?: "Unknown user" 
                                    else 
                                        "Tap to sign in for full features",
                                    style = if (user != null) 
                                        MaterialTheme.typography.titleMedium 
                                    else 
                                        MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (user != null) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (user != null) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            if (user != null) {
                                TextButton(
                                    onClick = {
                                        authViewModel.signOut()
                                    }
                                ) {
                                    Text(
                                        "Sign Out",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                    
                    // Section title for decks
                    Text(
                        text = if (selectedTab == DeckTab.ACTIVE) "Your Decks" else "Archived Decks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
                
                // Show message if no decks in this tab
                if (decksToShow.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            ) {
                                Text(
                                    text = emptyMessage,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(decksToShow) { deck ->
                    DeckCard(
                        deck = deck,
                        onClick = { onDeckClick(deck) },
                        onReview = { onStudyNowForDeck(deck) },
                        onLongPress = { deckToManage = deck }
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

        // Deck management dialog
        deckToManage?.let { deck ->
            DeckManagementModal(
                deck = deck,
                user = user,
                onDismiss = { deckToManage = null },
                onNavigateToAuth = onNavigateToAuth,
                onTogglePrivacy = {
                    val updatedDeck = deck.copy(isPublic = !deck.isPublic)
                    homeViewModel.updateDeck(updatedDeck)
                    deckToManage = null
                },
                onToggleArchive = {
                    val newState = if (deck.state == DeckState.ARCHIVED) DeckState.ACTIVE else DeckState.ARCHIVED
                    val updatedDeck = deck.copy(state = newState)
                    homeViewModel.updateDeck(updatedDeck)
                    deckToManage = null
                },
                onDelete = {
                    onDeleteDeck(deck)
                    deckToManage = null
                },
                onUpdateSchedule = { schedule ->
                    val updatedDeck = deck.copy(studySchedule = schedule)
                    homeViewModel.updateDeck(updatedDeck)
                    deckToManage = null
                },
                onShare = { email ->
                    try {
                        sendDeckByEmail(context, deck, email)
                        Toast.makeText(context, "Sharing deck...", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to share deck: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    deckToManage = null // Close modal after sharing attempt
                }
            )
        }
    }

}