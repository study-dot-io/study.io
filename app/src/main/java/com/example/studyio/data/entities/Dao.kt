package com.example.studyio.data.entities

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.Insert
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE id IN (SELECT noteId FROM cards WHERE deckId = :deckId)")
    suspend fun getNotesForDeck(deckId: Long): List<Note>
    
    @Query("SELECT * FROM notes WHERE id in (:noteIds)")
    suspend fun getNotesByIds(noteIds: List<Long>): List<Note>

    @Insert
    suspend fun insertNote(note: Note): Long
}

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks")
    suspend fun getAllDecks(): List<Deck>

    @Insert
    suspend fun insertDeck(deck: Deck): Long

    @Query("DELETE FROM decks WHERE id = :deckId")
    suspend fun deleteDeck(deckId: Long)
}

@Dao
interface CardDao {
    @Insert
    suspend fun insertCard(card: Card): Long

    @Query("SELECT * FROM cards WHERE deckId = :deckId LIMIT :limit")
    suspend fun getCardsForDeck(deckId: Long, limit: Int): List<Card>

    @Query("SELECT * FROM cards WHERE noteId = :noteId")
    suspend fun getCardsForNote(noteId: Long): List<Card>

    @Update
    suspend fun updateCard(card: Card): Int
}


@Database(entities = [Deck::class, Note::class, Card::class], version = 1)
@TypeConverters(Converters::class)
abstract class StudyioDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
}
