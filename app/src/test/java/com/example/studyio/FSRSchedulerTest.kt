package com.example.studyio

import com.example.studyio.data.entities.Card
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class FSRSchedulerTest {
    private val today = LocalDate.of(2025, 7, 4)
    private val epochToday = today.toEpochDay().toInt()
    private val yesterday = today.minusDays(1)
    private val epochYesterday = yesterday.toEpochDay().toInt()
    private val tomorrow = today.plusDays(1)
    private val epochTomorrow = tomorrow.toEpochDay().toInt()

    private fun baseCard(
        id: Long = 1,
        type: Int = 2,
        due: Int = epochToday,
        isActive: Boolean = true
    ) = Card(
        id = id,
        deckId = 1,
        noteId = 1,
        ord = 0,
        type = type,
        queue = 2,
        due = due,
        isActive = isActive
    )

    @Test
    fun testNewCardIsDue() {
        val card = baseCard(type = 0)
        assertTrue(FSRScheduler.isDue(card, today))
    }

    @Test
    fun testLearningCardDueToday() {
        val card = baseCard(type = 1, due = epochToday)
        assertTrue(FSRScheduler.isDue(card, today))
    }

    @Test
    fun testLearningCardDueYesterday() {
        val card = baseCard(type = 1, due = epochYesterday)
        assertTrue(FSRScheduler.isDue(card, today))
    }

    @Test
    fun testLearningCardDueTomorrow() {
        val card = baseCard(type = 1, due = epochTomorrow)
        assertFalse(FSRScheduler.isDue(card, today))
    }

    @Test
    fun testReviewCardDueToday() {
        val card = baseCard(type = 2, due = epochToday)
        assertTrue(FSRScheduler.isDue(card, today))
    }

    @Test
    fun testReviewCardDueYesterday() {
        val card = baseCard(type = 2, due = epochYesterday)
        assertTrue(FSRScheduler.isDue(card, today))
    }

    @Test
    fun testReviewCardDueTomorrow() {
        val card = baseCard(type = 2, due = epochTomorrow)
        assertFalse(FSRScheduler.isDue(card, today))
    }

    @Test
    fun testInactiveCardIsNotDue() {
        val card = baseCard(isActive = false)
        assertFalse(FSRScheduler.isDue(card, today))
    }

    @Test
    fun testMixedCards() {
        val cards = listOf(
            baseCard(id = 1, type = 0), // new, due
            baseCard(id = 2, type = 1, due = epochToday), // learning, due
            baseCard(id = 3, type = 2, due = epochYesterday), // review, due
            baseCard(id = 4, type = 2, due = epochTomorrow), // review, not due
            baseCard(id = 5, type = 1, due = epochTomorrow), // learning, not due
            baseCard(id = 6, type = 2, due = epochToday, isActive = false) // inactive, not due
        )
        val due = FSRScheduler.getDueCards(cards, today)
        val dueIds = due.map { it.id }.toSet()
        assertEquals(setOf(1L, 2L, 3L), dueIds)
    }
}
