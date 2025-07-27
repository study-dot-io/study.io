package com.example.studyio.ui.screens.components

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
import androidx.compose.ui.unit.dp
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.DeckState
import com.example.studyio.utils.StudyScheduleUtils

@Composable
fun DeckManagementModal(
    deck: Deck,
    onDismiss: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onUpdateSchedule: (Int) -> Unit
) {
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage ${deck.name}") },
        text = {
            Column {
                Text("Choose an action for this deck:")
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
