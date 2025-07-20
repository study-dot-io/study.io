package com.example.studyio.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    onDeckSelected: (String) -> Unit,
) {
    val totalFlashcardsCreated by viewModel.totalCardsCreated.collectAsState()
    val totalFlashcardsReviewed by viewModel.totalCardsReviewed.collectAsState()
    val averageRating by viewModel.averageRating.collectAsState()
    val cardsReviewed by viewModel.cardsReviewed.collectAsState()
    val worstRatedCards by viewModel.worstRatedCards.collectAsState()
    val mostReviewedDecks by viewModel.mostReviewedDecks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Flashcards Stats in a Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Created", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("$totalFlashcardsCreated", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Reviewed", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("$totalFlashcardsReviewed", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Heatmap Placeholder in a Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🔥 Review Heatmap (Placeholder)", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Quiz Performance in a Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Quiz Performance", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "(Last Month)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Average Rating", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text("$averageRating", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Cards Reviewed", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text("$cardsReviewed", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Cards You Struggle With in a Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🟥 Cards You Struggle With", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    worstRatedCards.forEach { (deckId, front, rating) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDeckSelected(deckId) },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(front, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text("⭐ $rating", fontSize = 16.sp, fontWeight = FontWeight.Light)
                        }
                    }
                }
            }

            // Decks Most Reviewed in a Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📚 Top Decks Used", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    mostReviewedDecks.forEach { (deckId, deckName,  reviewCount) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDeckSelected(deckId) },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Deck $deckName", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text("$reviewCount reviews", fontSize = 16.sp, fontWeight = FontWeight.Light)
                        }
                    }
                }
            }
        }
    }
}
