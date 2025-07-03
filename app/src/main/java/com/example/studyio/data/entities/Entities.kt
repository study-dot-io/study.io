package com.example.studyio.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Index

/**
 * Deck entity representing a collection of flashcards.
 * @param id Unique deck ID (matches Anki deck ID if imported)
 * @param name Name of the deck
 * @param description Optional description of the deck
 * @param color Color hex string for UI display
 */
@Entity(tableName = "decks")
data class Deck(
    @PrimaryKey
    val id: Long,
    val name: String,
    val description: String? = null,
    val color: String = "#6366F1" // Default primary color
)

/**
 * Note entity representing the raw information for one or more cards.
 * @param id Unique note ID (matches Anki note ID if imported)
 * @param modelId Note type/model ID (for field/template mapping)
 * @param fields Field values, delimited by 0x1f (unit separator)
 * @param tags Space-separated tags for the note
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val modelId: Long,
    val fields: String, // 0x1f-delimited string
    val tags: String // space-separated
)

/**
 * Card entity representing a reviewable flashcard generated from a note and template.
 * @param id Unique card ID (matches Anki card ID if imported)
 * @param deckId Deck this card belongs to
 * @param noteId Note this card is generated from
 * @param ord Ordinal/index of the template used for this card
 * @param type Card type (0=new, 1=learning, 2=review, 3=relearning)
 * @param queue Scheduling state (see Anki docs)
 * @param due Due information (meaning depends on card type)
 * @param interval SRS interval (days)
 * @param reps Number of reviews
 * @param lapses Number of lapses
 * @param createdAt Creation timestamp
 * @param isActive Whether the card is active
 */
@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = Deck::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["deckId"]),
        Index(value = ["noteId"])
    ]
)
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val noteId: Long,
    val ord: Int, // template ordinal
    val type: Int, // 0=new, 1=learning, 2=review, 3=relearning
    val queue: Int, // scheduling state
    val due: Int, // due info
    val interval: Int = 0, // SRS interval (days)
    val reps: Int = 0, // number of reviews
    val lapses: Int = 0, // number of lapses
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    val isActive: Boolean = true
)

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

    /**
     * Insert a note and return its new id.
     */
    @Insert
    suspend fun insertNote(note: Note): Long
}

/**
 * Type converters for Room to handle java.time.LocalDateTime.
 */
class Converters {
    @TypeConverter
    fun fromLocalDateTime(value: java.time.LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): java.time.LocalDateTime? = value?.let { java.time.LocalDateTime.parse(it) }
}

/**
 * The Room database for StudyIO, including Deck, Note, and Card entities.
 */
@Database(entities = [Deck::class, Note::class, Card::class], version = 1)
@TypeConverters(Converters::class)
abstract class StudyioDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    // Add DAOs for Deck and Card as needed
} 