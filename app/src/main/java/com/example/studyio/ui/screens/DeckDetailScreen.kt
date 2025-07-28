package com.example.studyio.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyio.data.entities.Card

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(deckId: String, onBack: () -> Unit, onCreateCardPressed: () -> Unit) {
    val viewModel: DeckDetailViewModel = hiltViewModel()
    val cards by viewModel.cards.collectAsState()

    LaunchedEffect(deckId) {
        viewModel.loadCards(deckId)
    }

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
            FloatingActionButton(onClick = onCreateCardPressed) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
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
            if (cards.isEmpty()) {
                Text("No cards in this deck.", style = MaterialTheme.typography.bodyLarge)
            } else {
                CardGrid(cards = cards)
            }
        }
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
