package com.example.studyio.data.db.DAO
import androidx.room.*
import com.example.studyio.data.model.ReviewCards

@Dao
interface ReviewCardsDao {
    @Insert
    suspend fun insertCard(card: ReviewCards)

    @Query("SELECT * FROM ReviewCards WHERE deckId = :deckId")
    suspend fun getAllCards(deckId: Long): List<ReviewCards>
    @Delete
    suspend fun deleteCard(card: ReviewCards)

}