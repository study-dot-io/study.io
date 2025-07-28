package com.example.studyio.ui.screens.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.DeckState
import com.example.studyio.ui.home.HomeViewModel
import com.example.studyio.ui.screens.sendDeckByEmail
import com.example.studyio.utils.StudyScheduleUtils
import com.google.firebase.auth.FirebaseUser

@Composable
fun DeckManagementModal(
    deck: Deck,
    onDismiss: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onUpdateSchedule: (Int) -> Unit,
    onNavigateToAuth: () -> Unit = {},
    user: FirebaseUser? = null,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var shareDeckEmailPrompt by remember { mutableStateOf(false) }
    var selectedDeckForShare by remember { mutableStateOf<Deck?>(null) }
    var emailToShare by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage ${deck.name}") },
        text = {
            Column {
                Text("Choose an action for this deck:")
                Spacer(modifier = Modifier.height(16.dp))

                // Share/Public/Private section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            val updatedDeck = deck.copy(isPublic = !deck.isPublic)
                            homeViewModel.updateDeck(updatedDeck)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (deck.isPublic) "Make Private" else "Make Public")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedButton(
                        onClick = {
                            if (user == null) {
                                Toast.makeText(context, "You must be signed in to share a deck.", Toast.LENGTH_SHORT).show()
                                onNavigateToAuth()
                            } else if (!deck.isPublic) {
                                Toast.makeText(context, "The deck must be public to be shared.", Toast.LENGTH_SHORT).show()
                            } else {
                                selectedDeckForShare = deck
                                shareDeckEmailPrompt = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Share")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Schedule button
                OutlinedButton(
                    onClick = { showScheduleDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Modify Schedule")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Archive button
                OutlinedButton(
                    onClick = onArchive,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (deck.state == DeckState.ARCHIVED) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (deck.state == DeckState.ARCHIVED) "Unarchive Deck" else "Archive Deck")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Delete button
                OutlinedButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Deck")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    // Schedule selection dialog
    if (showScheduleDialog) {
        ScheduleSelectionDialog(
            currentSchedule = StudyScheduleUtils.getDayIndicesFromBitmask(deck.studySchedule),
            onDismiss = { showScheduleDialog = false },
            onConfirm = { selectedDays ->
                val schedule = StudyScheduleUtils.createScheduleBitmask(selectedDays)
                onUpdateSchedule(schedule)
                showScheduleDialog = false
            }
        )
    }

    // Delete confirmation
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Deck") },
            text = { Text("Are you sure you want to permanently delete '${deck.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Email sharing dialog
    if (shareDeckEmailPrompt && selectedDeckForShare != null) {
        AlertDialog(
            onDismissRequest = { shareDeckEmailPrompt = false },
            title = { Text("Share Deck") },
            text = {
                OutlinedTextField(
                    value = emailToShare,
                    onValueChange = { emailToShare = it },
                    label = { Text("Enter recipient email") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    sendDeckByEmail(context, selectedDeckForShare!!, emailToShare)
                    shareDeckEmailPrompt = false
                    emailToShare = ""
                }) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    shareDeckEmailPrompt = false
                    emailToShare = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ScheduleSelectionDialog(
    currentSchedule: List<Int>,
    onDismiss: () -> Unit,
    onConfirm: (List<Int>) -> Unit
) {
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
    val selectedDays = remember { mutableStateListOf<Int>().apply { addAll(currentSchedule) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Study Days") },
        text = {
            Column {
                Text("Choose which days you want to study this deck:")
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    daysOfWeek.forEachIndexed { index, day ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(40.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    if (selectedDays.contains(index)) {
                                        selectedDays.remove(index)
                                    } else {
                                        selectedDays.add(index)
                                    }
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (selectedDays.contains(index)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedDays.contains(index)) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedDays.toList()) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
