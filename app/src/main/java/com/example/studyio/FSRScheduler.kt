package com.example.studyio

import com.example.studyio.data.entities.Card
import java.time.LocalDate
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * FSRS-based scheduler for determining due cards in a deck.
 * All decks use a fixed 90% retention rate and global parameters.
 * All code is based on the FSRS4Anki scheduler implementation (MIT licensed): https://github.com/open-spaced-repetition/fsrs4anki/blob/main/fsrs4anki_scheduler.js
 */
object FSRScheduler {
    // FSRS parameters from https://github.com/open-spaced-repetition/fsrs4anki/blob/main/fsrs4anki_scheduler.js
    private val w = doubleArrayOf(
        0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194, 0.001, 1.8722, 0.1666,
        0.796, 1.4835, 0.0614, 0.2629, 1.6483, 0.6014, 1.8729, 0.5425, 0.0912, 0.0658, 0.1542
    )
    private const val REQUEST_RETENTION = 0.9
    private const val MAXIMUM_INTERVAL = 36500
    private val DECAY = -w[20]
    private val FACTOR = 0.9.pow(1 / DECAY) - 1

    /**
     * Returns the list of cards due for study today from the given list.
     * @param cards List of cards in the deck
     * @param today The current date (default: LocalDate.now())
     */
    fun getDueCards(cards: List<Card>, today: LocalDate = LocalDate.now()): List<Card> {
        return cards.filter { isDue(it, today) }
    }

    /**
     * Determines if a card is due for study today.
     * @param card The card to check
     * @param today The current date
     */
    fun isDue(card: Card, today: LocalDate): Boolean {
        if (!card.isActive) return false
        // New cards: due is the position in the new queue (show if due <= today)
        if (card.type == 0) {
            return true // All new cards are considered due
        }
        // Learning/relearning cards: due is the timestamp (days since epoch)
        if (card.type == 1 || card.type == 3) {
            val dueDate = epochDayToDate(card.due)
            return !dueDate.isAfter(today)
        }
        // Review cards: due is the day scheduled for next review
        if (card.type == 2) {
            val dueDate = epochDayToDate(card.due)
            return !dueDate.isAfter(today)
        }
        return false
    }

    private fun epochDayToDate(epochDay: Int): LocalDate {
        // Anki stores due as days since 1970-01-01
        return LocalDate.ofEpochDay(epochDay.toLong())
    }

    // --- FSRS Core Algorithm Functions ---
    fun forgettingCurve(elapsedDays: Double, stability: Double): Double {
        return (1 + FACTOR * elapsedDays / stability).pow(DECAY)
    }

    fun nextIntervalFSRS(stability: Double): Int {
        val interval = stability / FACTOR * (REQUEST_RETENTION.pow(1 / DECAY) - 1)
        return interval.roundToInt().coerceAtLeast(1).coerceAtMost(MAXIMUM_INTERVAL)
    }

    fun nextDifficulty(d: Double, rating: Int): Double {
        val deltaD = -w[6] * (rating - 3)
        val nextD = d + linearDamping(deltaD, d)
        return constrainDifficulty(meanReversion(initDifficulty(4), nextD))
    }

    fun nextRecallStability(d: Double, s: Double, r: Double, rating: Int): Double {
        val hardPenalty = if (rating == 2) w[15] else 1.0
        val easyBonus = if (rating == 4) w[16] else 1.0
        return (s * (1 + exp(w[8]) * (11 - d) * s.pow(-w[9]) * (exp((1 - r) * w[10]) - 1) * hardPenalty * easyBonus)).coerceAtLeast(0.1)
    }

    fun nextForgetStability(d: Double, s: Double, r: Double): Double {
        val sMin = s / exp(w[17] * w[18])
        return minOf(w[11] * d.pow(-w[12]) * ((s + 1).pow(w[13]) - 1) * exp((1 - r) * w[14]), sMin).coerceAtLeast(0.1)
    }

    fun nextShortTermStability(s: Double, rating: Int): Double {
        var sinc = exp(w[17] * (rating - 3 + w[18])) * s.pow(-w[19])
        if (rating >= 3) sinc = sinc.coerceAtLeast(1.0)
        return (s * sinc).coerceAtLeast(0.1)
    }

    // --- Card State Management ---
    fun isNew(card: Card): Boolean = card.type == 0
    fun isLearning(card: Card): Boolean = card.type == 1 || card.type == 3
    fun isReview(card: Card): Boolean = card.type == 2

    data class CardMemoryState(
        var difficulty: Double,
        var stability: Double
    )

    fun initStates(): CardMemoryState {
        // Use 'good' as default for new cards
        return CardMemoryState(
            difficulty = initDifficulty(3),
            stability = initStability(3)
        )
    }

    fun initDifficulty(rating: Int): Double {
        return constrainDifficulty(w[4] - exp(w[5] * (rating - 1)) + 1)
    }

    fun initStability(rating: Int): Double {
        return w[rating - 1].coerceAtLeast(0.1)
    }

    fun constrainDifficulty(difficulty: Double): Double {
        return difficulty.coerceIn(1.0, 10.0)
    }

    fun linearDamping(deltaD: Double, oldD: Double): Double {
        return deltaD * (10 - oldD) / 9
    }

    fun meanReversion(init: Double, current: Double): Double {
        return w[7] * init + (1 - w[7]) * current
    }

    // --- Fuzzing System ---
    fun applyFuzz(ivl: Int, seed: Int = 0): Int {
        if (ivl < 2) return ivl
        val minIvl = (ivl * 0.95 - 1).roundToInt().coerceAtLeast(2)
        val maxIvl = (ivl * 1.05 + 1).roundToInt()
        val fuzz = (seed % (maxIvl - minIvl + 1)) + minIvl
        return fuzz
    }
}
