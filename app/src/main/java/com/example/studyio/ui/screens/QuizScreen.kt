package com.example.studyio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studyio.FSRScheduler
import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.StudyioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun QuizScreen(
    deckId: Long,
    db: StudyioDatabase,
    onQuizComplete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var dueCards by remember { mutableStateOf<List<Card>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var showBack by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(false) }
    var notesById by remember { mutableStateOf<Map<Long, com.example.studyio.data.entities.Note>>(emptyMap()) }
    val today = LocalDate.now()

    fun rateCard(
        card: Card,
        rating: Int,
        today: LocalDate,
        db: StudyioDatabase,
        onNext: () -> Unit
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val stability = card.stability
            val difficulty = card.difficulty
            val retrievability = FSRScheduler.forgettingCurve(card.interval.toDouble(), stability)
            val newDifficulty = FSRScheduler.nextDifficulty(difficulty, rating)
            val newStability = if (rating == 1) {
                FSRScheduler.nextForgetStability(difficulty, stability, retrievability)
            } else {
                FSRScheduler.nextRecallStability(difficulty, stability, retrievability, rating)
            }
            val newInterval = FSRScheduler.nextIntervalFSRS(newStability)
            db.cardDao().updateCard(
                card.copy(
                    interval = newInterval,
                    due = today.plusDays(newInterval.toLong()).toEpochDay().toInt(),
                    difficulty = newDifficulty,
                    stability = newStability
                )
            )
            onNext()
        }
    }

    // Load due cards and notes for this deck
    LaunchedEffect(deckId) {
        coroutineScope.launch(Dispatchers.IO) {
            val allCards = db.cardDao().getCardsForDeck(deckId)
            val due = FSRScheduler.getDueCards(allCards, today)
            val notes = db.noteDao().getNotesForDeck(deckId)
            notesById = notes.associateBy { it.id }
            dueCards = due
        }
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
        isComplete = true
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
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                ReviewButton("Again", onClick = {
                    rateCard(card, 1, today, db) {
                        showBack = false
                        currentIndex++
                        if (currentIndex >= dueCards.size) isComplete = true
                    }
                })
                ReviewButton("Hard", onClick = {
                    rateCard(card, 2, today, db) {
                        showBack = false
                        currentIndex++
                        if (currentIndex >= dueCards.size) isComplete = true
                    }
                })
                ReviewButton("Good", onClick = {
                    rateCard(card, 3, today, db) {
                        showBack = false
                        currentIndex++
                        if (currentIndex >= dueCards.size) isComplete = true
                    }
                })
                ReviewButton("Easy", onClick = {
                    rateCard(card, 4, today, db) {
                        showBack = false
                        currentIndex++
                        if (currentIndex >= dueCards.size) isComplete = true
                    }
                })
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