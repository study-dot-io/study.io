package com.example.studyio.data.entities

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeckRepository @Inject constructor(private val deckDao: DeckDao) {
    suspend fun getAllDecks(): List<Deck> = deckDao.getAllDecks()
    suspend fun insertDeck(deck: Deck) = deckDao.insertDeck(deck)
    suspend fun deleteDeck(deckId: String) = deckDao.deleteDeck(deckId)
    suspend fun getDueCardsCount(deckId: String): Int = deckDao.getDueCardsCount(deckId)
    suspend fun updateDeck(deck: Deck) = deckDao.updateDeck(deck)
}

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao,
) {
    suspend fun getDueCards(deckId: String, limit: Int = 200): List<Card> = cardDao.getDueCards(deckId, limit)
    suspend fun updateCard(card: Card) = cardDao.updateCard(card)
    suspend fun getTotalCardsCreated(): Int = cardDao.getTotalCardsCreated()
}

@Singleton
class QuizSessionRepository @Inject constructor(private val quizSessionDao: QuizSessionDao) {
    suspend fun getMostReviewedDecks(): List<DeckReviewCount> = quizSessionDao.getMostReviewedDecksSessions()
    suspend fun insertQuizSession(session: QuizSession) {
        quizSessionDao.insertQuizSession(session)
    }

    suspend fun updateQuizSession(session: QuizSession) {
        quizSessionDao.updateQuizSession(session)
    }

    suspend fun getSessionById(sessionId: String): QuizSession? {
        return quizSessionDao.getSessionById(sessionId)
    }

    suspend fun getOngoingSession(deckId: String): QuizSession? {
        return quizSessionDao.getOngoingSession(deckId)
    }
}

@Singleton
class QuizQuestionRepository @Inject constructor(private val quizQuestionDao: QuizQuestionDao) {
    suspend fun getTotalCardsReviewed(): Int = quizQuestionDao.getTotalReviewedQuestions()
    suspend fun getAverageRating(): Float = quizQuestionDao.getAverageRatingQuestions()
    suspend fun getCardsReviewedLastMonth(): Int = quizQuestionDao.getReviewedLastMonthQuestions()
    suspend fun getWorstRatedCards(): List<CardRating> = quizQuestionDao.getWorstRatedQuestions()
    suspend fun insertQuizQuestion(question: QuizQuestion) {
        quizQuestionDao.insertQuizQuestion(question)
    }
    suspend fun getReviewHeatmapData(): List<ReviewHeatmapData> = quizQuestionDao.getReviewHeatmapData()
}
