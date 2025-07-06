package com.example.studyio.data.entities

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.Insert
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update

/**
 * Data Access Object for Note entity.
 */
@Dao
interface NoteDao {
    /**
     * Get all notes for a given deckId.
     */
    @Query("SELECT * FROM notes WHERE id IN (SELECT noteId FROM cards WHERE deckId = :deckId)")
    suspend fun getNotesForDeck(deckId: Long): List<Note>
    
    @Query("SELECT * FROM notes WHERE id in (:noteIds)")
    suspend fun getNotesByIds(noteIds: List<Long>): List<Note>

    /**
     * Insert a note and return its new id.
     */
    @Insert
    suspend fun insertNote(note: Note): Long
}

/**
 * Data Access Object for Deck entity.
 */
@Dao
interface DeckDao {
    @Query("SELECT * FROM decks")
    suspend fun getAllDecks(): List<Deck>

    @Insert
    suspend fun insertDeck(deck: Deck): Long

    @Query("DELETE FROM decks WHERE id = :deckId")
    suspend fun deleteDeck(deckId: Long)
}

/**
 * Data Access Object for Card entity.
 */
@Dao
interface CardDao {
    /**
     * Insert a card and return its new id.
     */
    @Insert
    suspend fun insertCard(card: Card): Long

    /**
     * Get all cards for a given deckId.
     */
    @Query("SELECT * FROM cards WHERE deckId = :deckId")
    suspend fun getCardsForDeck(deckId: Long): List<Card>

    /**
     * Get all cards for a given noteId.
     */
    @Query("SELECT * FROM cards WHERE noteId = :noteId")
    suspend fun getCardsForNote(noteId: Long): List<Card>

    /**
     * Update a card's fields.
     */
    @Update
    suspend fun updateCard(card: Card): Int
}


/**
 * The Room database for StudyIO, including Deck, Note, and Card entities.
 */
@Database(entities = [Deck::class, Note::class, Card::class], version = 1)
@TypeConverters(Converters::class)
abstract class StudyioDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
}
