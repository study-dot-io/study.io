package com.example.studyio

import org.junit.Assert.*
import org.junit.Test

class FSRSchedulerCoreTest {
    @Test
    fun testForgettingCurve() {
        val stability = 5.0
        val elapsed = 2.0
        val retention = FSRScheduler.forgettingCurve(elapsed, stability)
        assertTrue(retention in 0.0..1.0)
    }

    @Test
    fun testNextIntervalFSRS() {
        val stability = 10.0
        val interval = FSRScheduler.nextIntervalFSRS(stability)
        assertTrue(interval > 0)
    }

    @Test
    fun testNextDifficulty() {
        val d = 5.0
        val rating = 4 // easy
        val nextD = FSRScheduler.nextDifficulty(d, rating)
        assertTrue(nextD in 1.0..10.0)
    }

    @Test
    fun testNextRecallStability() {
        val d = 5.0
        val s = 10.0
        val r = 0.9
        val rating = 3 // good
        val nextS = FSRScheduler.nextRecallStability(d, s, r, rating)
        assertTrue(nextS > 0.0)
    }

    @Test
    fun testNextForgetStability() {
        val d = 5.0
        val s = 10.0
        val r = 0.5
        val nextS = FSRScheduler.nextForgetStability(d, s, r)
        assertTrue(nextS > 0.0)
    }

    @Test
    fun testNextShortTermStability() {
        val s = 2.0
        val rating = 2 // hard
        val nextS = FSRScheduler.nextShortTermStability(s, rating)
        assertTrue(nextS > 0.0)
    }

    @Test
    fun testIsNewLearningReview() {
        val cardNew = com.example.studyio.data.entities.Card(id=1, deckId=1, noteId=1, ord=0, type=0, queue=0, due=0)
        val cardLearning = com.example.studyio.data.entities.Card(id=2, deckId=1, noteId=1, ord=0, type=1, queue=1, due=0)
        val cardReview = com.example.studyio.data.entities.Card(id=3, deckId=1, noteId=1, ord=0, type=2, queue=2, due=0)
        assertTrue(FSRScheduler.isNew(cardNew))
        assertTrue(FSRScheduler.isLearning(cardLearning))
        assertTrue(FSRScheduler.isReview(cardReview))
    }

    @Test
    fun testInitStates() {
        val state = FSRScheduler.initStates()
        assertTrue(state.difficulty in 1.0..10.0)
        assertTrue(state.stability > 0.0)
    }

    @Test
    fun testInitDifficultyAndStability() {
        val d = FSRScheduler.initDifficulty(3)
        val s = FSRScheduler.initStability(3)
        assertTrue(d in 1.0..10.0)
        assertTrue(s > 0.0)
    }

    @Test
    fun testConstrainDifficulty() {
        assertEquals(1.0, FSRScheduler.constrainDifficulty(0.5), 0.0001)
        assertEquals(10.0, FSRScheduler.constrainDifficulty(15.0), 0.0001)
        assertEquals(5.0, FSRScheduler.constrainDifficulty(5.0), 0.0001)
    }

    @Test
    fun testLinearDamping() {
        val result = FSRScheduler.linearDamping(2.0, 5.0)
        assertTrue(result > 0.0)
    }

    @Test
    fun testMeanReversion() {
        val result = FSRScheduler.meanReversion(3.0, 7.0)
        assertTrue(result > 0.0)
    }

    @Test
    fun testApplyFuzz() {
        val ivl = 10
        val fuzzed = FSRScheduler.applyFuzz(ivl, seed = 42)
        assertTrue(fuzzed in 2..(ivl * 1.05 + 1).toInt())
    }
}

