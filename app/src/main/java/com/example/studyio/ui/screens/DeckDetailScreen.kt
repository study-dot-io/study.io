package com.example.studyio.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.CardType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(deckId: String, onBack: () -> Unit, onCreateCardPressed: () -> Unit) {
    val viewModel: DeckDetailViewModel = hiltViewModel()
    val cards by viewModel.cards.collectAsState()

    LaunchedEffect(deckId) {
        viewModel.loadCards(deckId)
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
            if (cards.isEmpty()) {
                Text("No cards in this deck.", style = MaterialTheme.typography.bodyLarge)
            } else {
                CardGrid(cards = cards)
            }
        }

    }
}

@Composable
fun CardGrid(cards: List<Card>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(cards.size) { index ->
            val card = cards[index]
            CardItem(card = card)
        }
    }
}

@Composable
fun CardItem(card: Card) {
    val currentTime = System.currentTimeMillis()
    val isDue = card.due <= currentTime
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    // Determine card status color and text
    val (statusColor, statusText) = when {
        card.type == CardType.NEW -> Color(0xFF4CAF50) to "NEW"
        isDue -> Color(0xFFF44336) to "DUE"
        else -> Color(0xFF9E9E9E) to "SCHEDULED"
    }
    
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status indicator row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = CircleShape,
                    color = statusColor
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
            
            Text("Front: ${card.front}", style = MaterialTheme.typography.bodyMedium)
            Text("Back: ${card.back}", style = MaterialTheme.typography.bodyMedium)
            
            if (card.tags.isNotBlank()) {
                Text("Tags: ${card.tags}", style = MaterialTheme.typography.bodySmall)
            }
            
            // Due date information
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Due: ${dateFormat.format(Date(card.due))}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDue) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Debug info: Show actual timestamps
            Text(
                text = "Due time: ${card.due}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "Current: ${currentTime}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "Is Due: ${if (isDue) "YES" else "NO"}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDue) Color(0xFFF44336) else Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }
    }
}