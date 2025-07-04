package com.example.studyio.data.repository

import com.example.studyio.data.db.DAO.ReviewCardsDao
import com.example.studyio.data.model.ReviewCards
import javax.inject.Inject

class ReviewCardRepository(private val reviewCardsDao: ReviewCardsDao) {
    suspend fun getAllReviewCards(deckId: Long): List<ReviewCards> {
        return reviewCardsDao.getAllCards(deckId)
    }
    suspend fun insertReviewCard(reviewCard: ReviewCards) {
        return reviewCardsDao.insertCard(reviewCard)
    }
    suspend fun deleteReviewCard(reviewCard: ReviewCards) {
        return reviewCardsDao.deleteCard(reviewCard)
    }

    // Dummy cards
    suspend fun insertDummyCards() {
        val card1 = ReviewCards(deckId = 1, front = "Front 1", back = "Back 1")
        val card2 = ReviewCards(deckId = 1, front = "Front 2", back = "Back 2")
        insertReviewCard(card1)
        insertReviewCard(card2)
    }

}