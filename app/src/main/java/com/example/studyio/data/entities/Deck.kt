package com.example.studyio.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "decks")
data class Deck(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val cardCount: Int = 0,
    val lastStudied: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
    val color: String = "#6366F1" // Default primary color
) 