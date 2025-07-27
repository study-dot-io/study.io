package com.example.studyio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
            FriendActivityCard(friend, onReactionSent = { reaction ->
                // Handle reaction here if needed
            })
        }
    }
}

@Composable
fun FriendActivityCard(
    friend: FriendActivity,
    onReactionSent: (reaction: String) -> Unit
) {
    var showEmojiPicker by remember { mutableStateOf(false) }

    val reactions = listOf("👍", "❤️", "😂", "😮", "😢", "👏")
    val reactionCounts = remember { mutableStateMapOf<String, Int>() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
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

                Column(modifier = Modifier.weight(1f)) {
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

                IconButton(
                    onClick = { showEmojiPicker = !showEmojiPicker }
                ) {
                    Icon(
                        imageVector = Icons.Default.AddReaction,
                        contentDescription = "Add Reaction",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (reactionCounts.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    reactionCounts.forEach { (emoji, count) ->
                        if (count > 0) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .padding(end = 4.dp)
                            ) {
                                Text(
                                    text = "$emoji $count",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            if (showEmojiPicker) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 56.dp, bottom = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        reactions.forEach { reaction ->
                            Text(
                                text = reaction,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                modifier = Modifier
                                    .clickable {
                                        reactionCounts[reaction] = (reactionCounts[reaction] ?: 0) + 1
                                        onReactionSent(reaction)
                                        showEmojiPicker = false
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
