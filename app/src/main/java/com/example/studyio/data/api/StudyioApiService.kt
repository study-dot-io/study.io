package com.example.studyio.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit API service interface for StudyIO backend
 */
interface StudyioApiService {
    
    /**
     * Health check endpoint
     */
    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
    
    /**
     * Verify Firebase ID token
     */
    @POST("verify-token")
    suspend fun verifyToken(
        @Body request: TokenVerificationRequest
    ): Response<AuthResponse>
    
    /**
     * Access protected route - requires Firebase ID token in Authorization header
     * This demonstrates the typical pattern for authenticated API calls
     */
    @GET("protected")
    suspend fun getProtectedData(
        @Header("Authorization") authorization: String
    ): Response<AuthResponse>
}
