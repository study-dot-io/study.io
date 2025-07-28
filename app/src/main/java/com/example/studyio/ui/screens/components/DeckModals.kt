package com.example.studyio.ui.screens.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.DeckState
import com.example.studyio.utils.StudyScheduleUtils
import com.google.firebase.auth.FirebaseUser

@Composable
fun ShareDeckDialog(
    deckName: String,
    onDismiss: () -> Unit,
    onSend: (email: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    val isEmailValid = remember(email) { email.isNotBlank() && "@" in email }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share '$deckName'") },
        text = {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Recipient's email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSend(email) },
                enabled = isEmailValid
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    deckName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Deck") },
        text = { Text("Are you sure you want to permanently delete '$deckName'? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun DeckManagementModal(
    deck: Deck,
    user: FirebaseUser?,
    onDismiss: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
    onUpdateSchedule: (Int) -> Unit,
    onShare: (email: String) -> Unit,
    onNavigateToAuth: () -> Unit,
) {
    val context = LocalContext.current
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

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

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            deckName = deck.name,
            onDismiss = { showDeleteDialog = false },
            onConfirmDelete = {
                onDelete()
                showDeleteDialog = false
            }
        )
    }

    if (showShareDialog) {
        ShareDeckDialog(
            deckName = deck.name,
            onDismiss = { showShareDialog = false },
            onSend = { email ->
                onShare(email)
                showShareDialog = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage ${deck.name}") },
        text = {
            Column {
                OutlinedButton(onClick = onTogglePrivacy, modifier = Modifier.fillMaxWidth()) {
                    Text(if (deck.isPublic) "Make Private" else "Make Public")
                }
                Spacer(Modifier.height(8.dp))

                // Share Button
                OutlinedButton(
                    onClick = {
                        when {
                            user == null -> onNavigateToAuth()
                            !deck.isPublic -> Toast.makeText(context, "Deck must be public to share.", Toast.LENGTH_SHORT).show()
                            else -> showShareDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Share")
                }
                Spacer(Modifier.height(16.dp))

                // Modify Schedule Button
                OutlinedButton(onClick = { showScheduleDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Modify Schedule")
                }
                Spacer(Modifier.height(8.dp))

                // Archive / Unarchive Button
                OutlinedButton(onClick = onToggleArchive, modifier = Modifier.fillMaxWidth()) {
                    val isArchived = deck.state == DeckState.ARCHIVED
                    Icon(if (isArchived) Icons.Default.Unarchive else Icons.Default.Archive, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isArchived) "Unarchive Deck" else "Archive Deck")
                }
                Spacer(Modifier.height(8.dp))

                // Delete Button
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Deck")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
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




