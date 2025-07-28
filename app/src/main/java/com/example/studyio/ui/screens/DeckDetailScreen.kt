package com.example.studyio.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyio.data.api.ApiResult
import com.example.studyio.data.api.GeneratedCard
import com.example.studyio.data.entities.Card
import com.example.studyio.ui.screens.components.DocumentUploadViewModel
import com.example.studyio.ui.screens.components.ProposedCardsDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(deckId: String, onBack: () -> Unit, onCreateCardPressed: () -> Unit) {
    val viewModel: DeckDetailViewModel = hiltViewModel()
    val documentUploadViewModel: DocumentUploadViewModel = hiltViewModel()
    val cards by viewModel.cards.collectAsState()
    val deck by viewModel.deck.collectAsState()
    
    var fabExpanded by remember { mutableStateOf(false) }
    var proposedCards by remember { mutableStateOf<List<GeneratedCard>?>(null) }
    var isGeneratingCards by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user = FirebaseAuth.getInstance().currentUser

    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        Log.d("DeckDetailScreen", "Document launcher triggered with uri: $uri")
        if (uri != null && deck != null && user != null) {
            Log.d("DeckDetailScreen", "Starting document upload for deck: ${deck?.name}")
            scope.launch {
                isGeneratingCards = true
                Log.d("DeckDetailScreen", "Set isGeneratingCards to true")
                
                try {
                    val result = documentUploadViewModel.generateCardsFromDocument(
                        context = context,
                        uri = uri
                    )
                    
                    Log.d("DeckDetailScreen", "Received result from generateCardsFromDocument: ${result.size} cards")
                    result.forEach { card ->
                        Log.d("DeckDetailScreen", "Card: front='${card.front}', back='${card.back}'")
                    }
                    
                    if (result.isNotEmpty()) {
                        proposedCards = result
                        Log.d("DeckDetailScreen", "Set proposedCards to result with ${result.size} cards")
                    } else {
                        Log.w("DeckDetailScreen", "No cards were generated from document")
                        Toast.makeText(context, "No cards could be generated from this document", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("DeckDetailScreen", "Error generating cards from document", e)
                    Toast.makeText(context, "Error processing document: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isGeneratingCards = false
                    Log.d("DeckDetailScreen", "Set isGeneratingCards to false")
                }
            }
        } else if (user == null) {
            Log.w("DeckDetailScreen", "User not authenticated")
            Toast.makeText(context, "Please sign in to upload documents", Toast.LENGTH_SHORT).show()
        } else if (deck == null) {
            Log.w("DeckDetailScreen", "Deck is null")
            Toast.makeText(context, "Deck not loaded yet, please try again", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(deckId) {
        viewModel.loadCards(deckId)
        viewModel.loadDeck(deckId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deck?.name ?: "Deck Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
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
                        text = { Text("Create Card") },
                        onClick = {
                            fabExpanded = false
                            onCreateCardPressed()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Upload Document") },
                        onClick = {
                            fabExpanded = false
                            if (user != null) {
                                documentLauncher.launch("*/*")
                            } else {
                                Toast.makeText(context, "Please sign in to upload documents", Toast.LENGTH_SHORT).show()
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isGeneratingCards) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Generating flashcards from document...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            if (cards.isEmpty()) {
                Text("No cards in this deck.", style = MaterialTheme.typography.bodyLarge)
            } else {
                CardGrid(cards = cards)
            }
        }
    }
    
    // Proposed cards dialog
    Log.d("DeckDetailScreen", "Checking proposedCards state: ${proposedCards?.size} cards")
    proposedCards?.let { cards ->
        Log.d("DeckDetailScreen", "Showing ProposedCardsDialog with ${cards.size} cards")
        ProposedCardsDialog(
            proposedCards = cards,
            onAccept = { selectedCards ->
                Log.d("DeckDetailScreen", "User accepted ${selectedCards.size} cards")
                scope.launch {
                    val result = documentUploadViewModel.addCardsToDeck(selectedCards, deckId)
                    when (result) {
                        is ApiResult.Success -> {
                            Log.d("DeckDetailScreen", "Cards added successfully: ${result.data}")
                            Toast.makeText(context, result.data, Toast.LENGTH_SHORT).show()
                            viewModel.loadCards(deckId) // Refresh the cards list
                        }
                        is ApiResult.Error -> {
                            Log.e("DeckDetailScreen", "Error adding cards: ${result.message}")
                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                    proposedCards = null
                    Log.d("DeckDetailScreen", "Reset proposedCards to null")
                }
            },
            onDismiss = {
                Log.d("DeckDetailScreen", "User dismissed ProposedCardsDialog")
                proposedCards = null
            }
        )
    } ?: run {
        Log.d("DeckDetailScreen", "proposedCards is null, dialog not shown")
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CardGrid(cards: List<Card>) {
    val flippedStates = remember(cards) {
        mutableStateMapOf<String, Boolean>().apply {
            cards.forEach { put(it.id, false) }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(cards, key = { it.id }) { card ->
            val isFlipped = flippedStates[card.id] == true

            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .clickable { flippedStates[card.id] = !isFlipped },
                colors = CardDefaults.cardColors(
                    containerColor = if (isFlipped)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                AnimatedContent(
                    targetState = isFlipped,
                    transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                    label = "CardFlip"
                ) { flipped ->
                    Box(
                        modifier = Modifier
                            .height(140.dp)
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (flipped) "Back" else "Front",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                        )

                        Text(
                            text = if (flipped) card.back else card.front,
                            style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )

                        if (card.tags.isNotBlank()) {
                            Text(
                                text = "Tags: ${card.tags}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
