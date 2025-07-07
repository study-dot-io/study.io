package com.example.studyio.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDateTime

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
 * @param guid Unique identifier for the note, generated if not provided
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val modelId: Long = 1,
    val fields: String = "", // Fields delimited by 0x1f (unit separator)
    val tags: String = "", // space-separated
    val guid: String = java.util.UUID.randomUUID().toString() // Unique identifier
) {
    companion object {
        fun create(
            modelId: Long = 1,
            fields: List<String> = emptyList(),
            tags: String = "",
            guid: String = java.util.UUID.randomUUID().toString()
        ): Note {
            return Note(
                modelId = modelId,
                fields = fields.joinToString("\u001F"),
                tags = tags.trim(),
                guid = guid
            )
        }
    }
}

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
    val due: Int, // unix timestamp in seconds representing due date
    val interval: Int = 0, // SRS interval (days)
    val reps: Int = 0, // number of reviews
    val lapses: Int = 0, // number of lapses
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
    val difficulty: Double = 5.0, // FSRS difficulty (1-10, default 5.0)
    val stability: Double = 1.0 // FSRS stability (default 1.0)
)

/**
 * Type converters for Room to handle java.time.LocalDateTime.
 */
class Converters {
    @TypeConverter
    fun fromLocalDateTime(value: java.time.LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): java.time.LocalDateTime? = value?.let { java.time.LocalDateTime.parse(it) }
}