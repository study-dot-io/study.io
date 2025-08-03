package com.example.studyio.scheduler

import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.CardDao
import kotlin.math.max
import kotlin.random.Random
import javax.inject.Inject

class FlashcardScheduler @Inject constructor(
    private val cardDao: CardDao
) {
    private val baseIntervalDays = 5.0
    fun updateDifficultyAndDueDate(card: Card, quizRating: Float, alpha: Float = 0.3f): Float {
        val newDifficulty = alpha * quizRating + (1 - alpha) * card.difficulty
        card.difficulty = newDifficulty
        card.lastReviewed = System.currentTimeMillis()

        val normDifficulty = (max(card.difficulty, 1f) - 1f) / 4f
        val cooldownDays = baseIntervalDays * (1 - normDifficulty)

        card.due = (System.currentTimeMillis() + cooldownDays * 24 * 60 * 60 * 1000).toLong()
        return newDifficulty
    }
}