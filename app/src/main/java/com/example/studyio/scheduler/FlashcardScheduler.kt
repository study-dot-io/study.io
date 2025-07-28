package com.example.studyio.scheduler

import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.CardDao
import kotlin.math.max
import kotlin.random.Random
import javax.inject.Inject

class FlashcardScheduler @Inject constructor(
    private val cardDao: CardDao
) {
    private val baseIntervalDays = 3.0

    /**
     * Update the card's difficulty using the slider rating.
     * The new difficulty is computed using an exponential moving average.
     * Also updates the lastReviewed field.
     */
    fun updateDifficulty(card: Card, quizRating: Float, alpha: Float = 0.3f): Float {
        val newDifficulty = alpha * quizRating + (1 - alpha) * card.difficulty
        card.difficulty = newDifficulty
        card.lastReviewed = System.currentTimeMillis()
        return newDifficulty
    }

    /**
     * Checks if a card is eligible for review based on the elapsed time and its difficulty.
     */
    fun isEligible(card: Card, currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        val normDifficulty = (max(card.difficulty, 1f) - 1f) / 4f
        val cooldownDays = baseIntervalDays * (1 - normDifficulty)
        val daysSinceLastReview = if (card.lastReviewed > 0L)
            (currentTimeMs - card.lastReviewed).toDouble() / (1000 * 60 * 60 * 24)
        else 100.0
        return daysSinceLastReview >= cooldownDays
    }

    /**
     * Schedule quiz cards for a particular deck.
     * If the number of eligible cards exceeds a threshold, use weighted random sampling.
     */
    suspend fun scheduleQuiz(quizSize: Int, threshold: Int = 20, deckId: String): List<Card> {
        val allCards = cardDao.getCardsByDeckId(deckId)
        val currentTime = System.currentTimeMillis()
        val eligibleCards = allCards.filter { isEligible(it, currentTime) }

        if (eligibleCards.size <= quizSize) return eligibleCards

        return if (eligibleCards.size > threshold)
            weightedRandomSample(eligibleCards, quizSize)
        else
            eligibleCards.take(quizSize)
    }

    /**
     * Performs weighted random sampling where the card's difficulty acts as its weight.
     */
    private fun weightedRandomSample(cards: List<Card>, sampleSize: Int): List<Card> {
        val mutableCandidates = cards.toMutableList()
        val selected = mutableListOf<Card>()
        repeat(sampleSize.coerceAtMost(mutableCandidates.size)) {
            val totalWeight = mutableCandidates.sumOf { it.difficulty.toDouble() }
            val rnd = Random.nextDouble() * totalWeight
            var cumulative = 0.0
            var chosenIndex = 0
            for ((index, card) in mutableCandidates.withIndex()) {
                cumulative += card.difficulty
                if (cumulative >= rnd) {
                    chosenIndex = index
                    break
                }
            }
            selected.add(mutableCandidates[chosenIndex])
            mutableCandidates.removeAt(chosenIndex)
        }
        return selected
    }
}