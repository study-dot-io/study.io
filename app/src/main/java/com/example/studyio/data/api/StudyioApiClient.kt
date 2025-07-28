package com.example.studyio.data.api

import android.util.Log
import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.Deck
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API client for StudyIO backend
 * This class demonstrates how to make authenticated API calls using Firebase ID tokens
 */
@Singleton
class StudyioApiClient @Inject constructor() {
    
    private val baseUrl = "http://127.0.0.1:5001"
    
    private val auth = FirebaseAuth.getInstance()
    
    // Create HTTP client with logging for debugging
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val apiService = retrofit.create(StudyioApiService::class.java)
    
    /**
     * Get Firebase ID token for the current user
     */
    private suspend fun getFirebaseIdToken(): String? {
        return try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (_: Exception) {
            null
        }
    }
    
    /**
     * Health check - no authentication required
     */
    suspend fun healthCheck(): ApiResult<HealthResponse> {
        return try {
            val response = apiService.healthCheck()
            if (response.isSuccessful) {
                response.body()?.let { 
                    ApiResult.Success(it) 
                } ?: ApiResult.Error("Empty response")
            } else {
                ApiResult.Error("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }
    
    /**
     * Verify token endpoint - demonstrates POST request with token
     */
    suspend fun verifyToken(): ApiResult<AuthResponse> {
        val token = getFirebaseIdToken()
        if (token == null) {
            return ApiResult.Error("No Firebase token available - user not authenticated")
        }
        
        return try {
            val response = apiService.verifyToken(TokenVerificationRequest(token))
            if (response.isSuccessful) {
                response.body()?.let { 
                    ApiResult.Success(it) 
                } ?: ApiResult.Error("Empty response")
            } else {
                ApiResult.Error("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     *
     */
    suspend fun syncData(decks: List<Deck>, cards: List<Card>): ApiResult<SyncResponse> {
        val token = getFirebaseIdToken()
        if (token == null) {
            return ApiResult.Error("No Firebase token available - user not authenticated")
        }

        return try {
            val request = SyncRequest(
                token = token,
                decks = decks,
                cards = cards
            )
            val response = apiService.sync(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error("Empty response body")
                }
            } else {
                ApiResult.Error("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }


    /**
     * Upload document and generate flashcards
     */
    suspend fun generateFlashcards(fileName: String, base64File: String): ApiResult<FlashcardGenerationResponse> {
        val token = getFirebaseIdToken()
        if (token == null) {
            return ApiResult.Error("No Firebase token available - user not authenticated")
        }

        return try {
            val request = DocumentUploadRequest(
                loginToken = token,
                fileName = fileName,
                file = base64File
            )
            val response = apiService.generateFlashcards(request)
            Log.d("StudyioApiClient", "Response from generateFlashcards: $response")

            if (response.isSuccessful) {
                response.body()?.let { 
                    Log.d("StudyioApiClient", "Flashcard generation successful, cards: ${it.data.cards.size}")
                    ApiResult.Success(it) 
                } ?: ApiResult.Error("Empty response")
            } else {
                ApiResult.Error("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * MAIN EXAMPLE: Access protected route with Firebase authentication
     */
    suspend fun getProtectedData(): ApiResult<AuthResponse> {
        // Step 1: Get Firebase ID token from current user
        val token = getFirebaseIdToken()
        if (token == null) {
            return ApiResult.Error("No Firebase token available - user not authenticated")
        }
        
        return try {
            // Step 2: Make API call with Authorization header
            // Format: "Bearer <firebase_id_token>"
            val response = apiService.getProtectedData("Bearer $token")
            
            // Step 3: Handle response
            if (response.isSuccessful) {
                response.body()?.let { 
                    ApiResult.Success(it) 
                } ?: ApiResult.Error("Empty response")
            } else {
                // Handle different error cases
                when (response.code()) {
                    401 -> ApiResult.Error("Authentication failed - token invalid or expired")
                    403 -> ApiResult.Error("Access forbidden")
                    404 -> ApiResult.Error("Endpoint not found")
                    500 -> ApiResult.Error("Server error")
                    else -> ApiResult.Error("HTTP ${response.code()}: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }
}

/**
 * Sealed class to represent API results
 */
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    
    inline fun <R> map(transform: (T) -> R): ApiResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
    }
    
    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (String) -> Unit): ApiResult<T> {
        if (this is Error) action(message)
        return this
    }
}
