package com.example.studyio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyio.ui.quiz.QuizViewModel

@Composable
fun QuizScreen(
    deckId: Long,
    onQuizComplete: () -> Unit
) {
    val viewModel: QuizViewModel = hiltViewModel()
    val dueCards by viewModel.dueCards.collectAsState()
    val notesById by viewModel.notesById.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isComplete by viewModel.isComplete.collectAsState()
    var showBack by remember { mutableStateOf(false) }

    LaunchedEffect(deckId) {
        viewModel.loadQuiz(deckId)
    }

    if (isComplete || dueCards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Quiz complete!", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onQuizComplete, modifier = Modifier.padding(top = 24.dp)) {
                Text("Return Home")
            }
        }
        return
    }

    val card = dueCards.getOrNull(currentIndex)
    if (card == null) {
        // Defensive: should not happen
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Card ${currentIndex + 1} of ${dueCards.size}", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Front:", style = MaterialTheme.typography.labelMedium)
                val note = notesById[card.noteId]
                val fields = note?.fields?.split('\u001F') ?: emptyList()
                val front = fields.getOrNull(0) ?: "(No front)"
                val back = fields.getOrNull(1) ?: "(No back)"
                Text(front, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                if (showBack) {
                    Text("Back:", style = MaterialTheme.typography.labelMedium)
                    Text(back, style = MaterialTheme.typography.bodyLarge)
                } else {
                    Button(onClick = { showBack = true }) { Text("Show Answer") }
                }
            }
        }
        if (showBack) {
            Spacer(Modifier.height(16.dp))
            Text("How did you do?", style = MaterialTheme.typography.bodyMedium)
            val intervals = viewModel.getNextIntervalsForCurrentCard()
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                val labels = listOf("Again", "Hard", "Good", "Easy")
                for (i in 0..3) {
                    val label = labels[i]
                    val interval = intervals.getOrNull(i)
                    val intervalText = if (interval != null) " ($interval days)" else ""
                    ReviewButton(label + intervalText, onClick = {
                        viewModel.rateCard(i + 1)
                        showBack = false
                    })
                }
            }
        }
    }
}

@Composable
fun ReviewButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.padding(4.dp)) {
        Text(text)
    }
}