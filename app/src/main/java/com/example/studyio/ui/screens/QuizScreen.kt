package com.example.studyio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyio.ui.quiz.QuizViewModel
import kotlin.math.roundToInt

@Composable
fun QuizScreen(
    deckId: String,
    onQuizComplete: () -> Unit
) {
    val viewModel: QuizViewModel = hiltViewModel()
    val dueCards by viewModel.dueCards.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isComplete by viewModel.isComplete.collectAsState()
    var showBack by remember { mutableStateOf(false) }

    LaunchedEffect(deckId) {
        viewModel.loadQuiz(deckId)
    }

    if (isComplete || dueCards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Quiz complete!", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = {
                    viewModel.completeQuizSession()
                    onQuizComplete()
                }, modifier = Modifier.padding(top = 24.dp)) {
                    Text("Return Home")
                }
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
                val front = card.front
                val back = card.back
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

            var sliderPosition by remember { mutableStateOf(3f) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rate difficulty",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "Difficulty: ${sliderPosition.toInt()}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = {
                        viewModel.rateCard(sliderPosition.roundToInt())
                        viewModel.insertQuizQuestion(cardId = card.id, rating = sliderPosition.toInt())
                        showBack = false
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Submit Rating")
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