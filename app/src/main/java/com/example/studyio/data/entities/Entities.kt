package com.example.studyio.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

/**
 * Deck entity representing a collection of flashcards.
 * @param id Unique deck ID (matches Anki deck ID if imported)
 * @param name Name of the deck
 * @param description Optional description of the deck
 * @param color Color hex string for UI display
 */
@Entity(tableName = "decks",
    indices = [
        Index(value = ["isSynced"])
    ]
)
data class Deck(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val color: String = "#6366F1", // Default primary color
    val isSynced: Boolean = false, // Indicates if the deck is synced with the server. If we supoprt updating, change to syncedAt
    val isPublic: Boolean = true
)

enum class CardType {
    NEW, LEARNING, REVIEW, RELEARNING
}

/**
 * Card entity representing a reviewable flashcard generated from a note and template.
 * @param id Unique card ID (matches Anki card ID if imported)
 * @param deckId Deck this card belongs to
 * @param type Card type (0=new, 1=learning, 2=review, 3=relearning)
 * @param due Due information (meaning depends on card type)
 * @param tags Space-separated tags for the note
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
    ],
    indices = [
        Index(value = ["deckId"]),
        Index(value = ["isSynced"])
    ]
)
data class Card(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val deckId: String,
    val type: CardType = CardType.NEW, // 0=new, 1=learning, 2=review, 3=relearning
    val due: Long = System.currentTimeMillis() / 1000, // unix timestamp in seconds representing due date
    val front: String = "", // front field value
    val back: String = "", // back field value
    val tags: String = "", // space-separated (might be helpful to have this as a join table in the future)
    val createdAt: Long = System.currentTimeMillis() / 1000, // unix timestamp in seconds
    val isSynced: Boolean = false // Indicates if the card is synced with the server
)

class Converters {
    @TypeConverter
    fun fromCardType(type: CardType): Int {
        return type.ordinal
    }
    
    @TypeConverter
    fun toCardType(type: Int): CardType {
        return CardType.entries.toTypedArray().getOrElse(type) { CardType.NEW }
    }
}

@Entity(
    tableName = "quiz_sessions",
    indices = [Index(value = ["deckId"])],
    foreignKeys = [
        ForeignKey(
            entity = Deck::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuizSession(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val deckId: String,
    val startedAt: Long = System.currentTimeMillis() / 1000,
    val completedAt: Long? = null // Nullable to indicate ongoing sessions
)

@Entity(
    tableName = "quiz_questions",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["cardId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = QuizSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Card::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuizQuestion(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val sessionId: String,
    val cardId: String,
    val rating: Int, // Rating given to the card during the quiz
    val reviewedAt: Long = System.currentTimeMillis() / 1000
)
