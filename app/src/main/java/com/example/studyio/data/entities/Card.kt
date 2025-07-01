package com.example.studyio.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = Deck::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val difficulty: Int = 0, // 0-5 scale
    val interval: Int = 0, // Days until next review
    val easeFactor: Float = 2.5f,
    val dueDate: LocalDateTime? = null,
    val lastReviewed: LocalDateTime? = null,
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true
) 