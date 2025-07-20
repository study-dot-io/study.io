package com.example.studyio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

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
    val heatmapData by viewModel.reviewHeatmapData.collectAsState()

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
                .verticalScroll(rememberScrollState()) // Make screen scrollable
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Heatmap Placeholder in a Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column {
                    Text(
                        text = "Review Heatmap",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = "Last 30 days",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6), // 6 columns => 5 rows for 30 days
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((28.dp * 5) + (4.dp * 4) + 16.dp)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    userScrollEnabled = false,
                ) {
                    items(heatmapData.size) { index ->
                        val (_, reviewCount) = heatmapData[index]
                        val intensity = reviewCount.coerceIn(0, 4)
                        val color = when (intensity) {
                            0 -> Color(0xFFEDEDED) // Light gray
                            1 -> Color(0xFFD6E685) // Light green
                            2 -> Color(0xFF8CC665) // Medium green
                            3 -> Color(0xFF44A340) // Dark green
                            else -> Color(0xFF1E6823) // Very dark green
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(color, shape = RoundedCornerShape(6.dp))
                            )
                        }
                    }
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
                    Text("Cards You Struggle With", fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                    Text("Top Decks Used", fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
