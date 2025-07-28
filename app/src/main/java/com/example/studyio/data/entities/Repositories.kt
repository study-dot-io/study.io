package com.example.studyio.data.entities

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeckRepository @Inject constructor(private val deckDao: DeckDao) {
    suspend fun getAllDecks(): List<Deck> = deckDao.getAllDecks()
    suspend fun getActiveDecks(): List<Deck> = deckDao.getActiveDecks()
    suspend fun getArchivedDecks(): List<Deck> = deckDao.getArchivedDecks()
    suspend fun getDeckById(deckId: String): Deck? = deckDao.getDeckById(deckId)
    suspend fun insertDeck(deck: Deck) = deckDao.insertDeck(deck)
    suspend fun updateDeck(deck: Deck) = deckDao.updateDeck(deck)
    suspend fun softDeleteDeck(deckId: String) {
        getDeckById(deckId)?.let { deck ->
            val updatedDeck = deck.copy(state = DeckState.DELETED)
            updateDeck(updatedDeck)
        }
    }
}

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao,
) {
    suspend fun getCardsByDeckId(deckId: String): List<Card> = cardDao.getCardsByDeckId(deckId)
    suspend fun insertCard(card: Card) = cardDao.insertCard(card)
    suspend fun getDueCards(deckId: String): List<Card> = cardDao.getDueCardsBefore(deckId)
    suspend fun updateCard(card: Card) = cardDao.updateCard(card)
    suspend fun getTotalCardsCreated(): Int = cardDao.getTotalCardsCreated()
    suspend fun getDueCardsCount(deckId: String): Int = cardDao.getDueCardCountBefore(deckId)
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

    suspend fun getLastCompletedSessionDate(deckId: String): Long? {
        return quizSessionDao.getLastCompletedSessionDate(deckId)
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
