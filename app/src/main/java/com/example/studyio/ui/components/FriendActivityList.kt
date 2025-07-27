package com.example.studyio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.graphics.Color

data class FriendActivity(
    val initials: String,
    val name: String,
    val activities: List<String>
)

@Composable
fun FriendActivityList(
    friends: List<FriendActivity>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(friends) { friend ->
            FriendActivityCard(friend, onReactionSent = { activity, reaction ->
                // Handle reaction here
            })
        }
    }
}

@Composable
fun FriendActivityCard(
    friend: FriendActivity,
    onReactionSent: (activity: String?, reaction: String) -> Unit
) {
    var expandedCardMenu by remember { mutableStateOf(false) }
    val reactions = listOf("👍", "❤️", "😂", "😮", "😢", "👏")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* handle card click if needed */ },
                onLongClick = {
                    expandedCardMenu = true
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.initials,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = friend.name,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                friend.activities.forEach { activity ->
                    Text(
                        text = "• $activity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    DropdownMenu(
        expanded = expandedCardMenu,
        onDismissRequest = { expandedCardMenu = false }
    ) {
        reactions.forEach { reaction ->
            DropdownMenuItem(
                text = { Text(reaction, fontSize = MaterialTheme.typography.bodyLarge.fontSize) },
                onClick = {
                    onReactionSent(null, reaction) // null means card-level reaction
                    expandedCardMenu = false
                }
            )
        }
    }
}
