package com.example.studyio.data.api

import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.Deck
import com.google.gson.annotations.SerializedName

/**
 * Data classes for API responses
 */
data class AuthResponse(
    @SerializedName("authenticated")
    val authenticated: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("uid")
    val uid: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("error")
    val error: String? = null
)

data class TokenVerificationRequest(
    @SerializedName("token")
    val token: String
)

data class HealthResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("service")
    val service: String
)

data class SyncRequest (
    @SerializedName("token")
    val token: String,
    @SerializedName("decks")
    val decks: List<Deck>,
    @SerializedName("cards")
    val cards: List<Card>,
)

data class DocumentUploadRequest(
    @SerializedName("login_token")
    val loginToken: String,
    @SerializedName("file_name")
    val fileName: String,
    @SerializedName("file")
    val file: String
)

data class FlashcardGenerationResponse(
    @SerializedName("data")
    val data: FlashcardData
)

data class FlashcardData(
    @SerializedName("cards")
    val cards: List<GeneratedCard>
)

data class GeneratedCard(
    @SerializedName("front")
    val front: String,
    @SerializedName("back")
    val back: String
)


data class SyncResponse(
    @SerializedName("decks")
    val decks: List<Deck>,
    @SerializedName("cards")
    val cards: List<Card>,
)