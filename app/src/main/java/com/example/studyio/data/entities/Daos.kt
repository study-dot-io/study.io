package com.example.studyio.data.entities

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.Insert
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks")
    suspend fun getAllDecks(): List<Deck>

    @Insert
    suspend fun insertDeck(deck: Deck)

    @Query("DELETE FROM decks WHERE id = :deckId")
    suspend fun deleteDeck(deckId: String)
    
    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND due <= strftime('%s', 'now')")
    suspend fun getDueCardsCount(deckId: String): Int

    @Update
    suspend fun updateDeck(deck: Deck)
}

@Dao
interface CardDao {
    @Insert
    suspend fun insertCard(card: Card)
    
    @Query("SELECT * FROM cards WHERE id IN (:cardIds)")
    suspend fun getCardsById(cardIds: List<String>): List<Card>
    
    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY createdAt DESC")
    suspend fun getCardsByDeckId(deckId: String): List<Card>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND due <= strftime('%s', 'now') ORDER BY due ASC LIMIT :limit")
    suspend fun getDueCards(deckId: String, limit: Int): List<Card>

    @Update
    suspend fun updateCard(card: Card)

    @Query("SELECT COUNT(*) FROM cards")
    suspend fun getTotalCardsCreated(): Int
}

@Dao
interface QuizSessionDao {
    @Insert
    suspend fun insertQuizSession(session: QuizSession)

    @Query("SELECT * FROM quiz_sessions WHERE deckId = :deckId")
    suspend fun getSessionsByDeckId(deckId: String): List<QuizSession>

    @Query("SELECT * FROM quiz_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): QuizSession?

    @Update
    suspend fun updateQuizSession(session: QuizSession)

    @Query("SELECT deckId, decks.name as deckName, COUNT(*) as reviewCount FROM quiz_sessions JOIN decks ON quiz_sessions.deckId=decks.id GROUP BY quiz_sessions.deckId ORDER BY reviewCount DESC LIMIT 5")
    suspend fun getMostReviewedDecksSessions(): List<DeckReviewCount>

    @Query("SELECT * FROM quiz_sessions WHERE deckId = :deckId AND completedAt IS NULL")
    suspend fun getOngoingSession(deckId: String): QuizSession?
}

@Dao
interface QuizQuestionDao {
    @Insert
    suspend fun insertQuizQuestion(question: QuizQuestion)

    @Query("SELECT COUNT(*) FROM quiz_questions")
    suspend fun getTotalReviewedQuestions(): Int

    @Query("SELECT AVG(rating) FROM quiz_questions")
    suspend fun getAverageRatingQuestions(): Float

    @Query("SELECT COUNT(*) FROM quiz_questions WHERE reviewedAt >= strftime('%s', 'now', '-1 month')")
    suspend fun getReviewedLastMonthQuestions(): Int

    @Query("SELECT cards.deckId AS deckId, cards.front as front, AVG(quiz_questions.rating) as avgRating FROM quiz_questions JOIN cards ON quiz_questions.cardId = cards.id GROUP BY quiz_questions.cardId ORDER BY avgRating ASC LIMIT 5")
    suspend fun getWorstRatedQuestions(): List<CardRating>

    @Query("SELECT reviewedAt, COUNT(*) as reviewCount FROM quiz_questions GROUP BY strftime('%Y-%m-%d', datetime(reviewedAt, 'unixepoch')) LIMIT 30")
    suspend fun getReviewHeatmapData(): List<ReviewHeatmapData>
}

// Define data classes for the custom query results
data class DeckReviewCount(
    val deckId: String,
    val deckName: String,
    val reviewCount: Int
)

data class CardRating(
    val deckId: String,
    val front: String,
    val avgRating: Float
)

data class ReviewHeatmapData(
    val reviewedAt: Long,
    val reviewCount: Int
)

@Database(entities = [Deck::class, Card::class, QuizSession::class, QuizQuestion::class], version = 1)
@TypeConverters(Converters::class)
abstract class StudyioDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
    abstract fun quizSessionDao(): QuizSessionDao
    abstract fun quizQuestionDao(): QuizQuestionDao
}
