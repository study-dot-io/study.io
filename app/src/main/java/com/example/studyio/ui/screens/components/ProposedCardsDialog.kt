package com.example.studyio.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.studyio.data.api.GeneratedCard

@Composable
fun ProposedCardsDialog(
    proposedCards: List<GeneratedCard>,
    onAccept: (List<GeneratedCard>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCards by remember { 
        mutableStateOf(proposedCards.associateWith { true })
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Proposed Flashcards",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Column {
                        TextButton(
                            onClick = {
                                selectedCards = selectedCards.mapValues { true }
                            }
                        ) {
                            Text("Select All")
                        }
                        TextButton(
                            onClick = {
                                selectedCards = selectedCards.mapValues { false }
                            }
                        ) {
                            Text("Deselect All")
                        }
                    }
                }
                
                Text(
                    text = "Review and select the cards you want to add to your deck:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Cards list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(proposedCards) { card ->
                        ProposedCardItem(
                            card = card,
                            isSelected = selectedCards[card] == true,
                            onSelectionChanged = { isSelected ->
                                selectedCards = selectedCards.toMutableMap().apply {
                                    this[card] = isSelected
                                }.toMap()
                            }
                        )
                    }
                }
                
                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val cardsToAdd = selectedCards.filter { it.value }.keys.toList()
                            if (cardsToAdd.isNotEmpty()) {
                                onAccept(cardsToAdd)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedCards.values.any { it }
                    ) {
                        val selectedCount = selectedCards.values.count { it }
                        Text("Add $selectedCount Card${if (selectedCount != 1) "s" else ""}")
                    }
                }
            }
        }
    }
}

@Composable
fun ProposedCardItem(
    card: GeneratedCard,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                // Front of card
                Text(
                    text = "Front:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Back of card
                Text(
                    text = "Back:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = card.back,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
