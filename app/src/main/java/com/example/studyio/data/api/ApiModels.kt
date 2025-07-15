package com.example.studyio.data.api

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
