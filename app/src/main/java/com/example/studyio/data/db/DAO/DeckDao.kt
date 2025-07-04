package com.example.studyio.data.db.DAO
import androidx.room.*
import com.example.studyio.data.model.Deck

@Dao
interface DeckDao {
    @Insert
    suspend fun insert(deck: Deck)

    @Query("SELECT * FROM decks")
    suspend fun getAllDecks(): List<Deck>

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeckById(id: Long): Deck?

    // Insert more queries as required
    @Delete
    suspend fun delete(deck: Deck)
}