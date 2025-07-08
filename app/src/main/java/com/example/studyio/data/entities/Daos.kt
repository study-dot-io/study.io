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
}


@Database(entities = [Deck::class, Card::class], version = 1)
@TypeConverters(Converters::class)
abstract class StudyioDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
}
