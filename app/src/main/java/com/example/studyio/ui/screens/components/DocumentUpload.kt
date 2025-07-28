package com.example.studyio.ui.screens.components

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.studyio.data.api.ApiResult
import com.example.studyio.data.api.GeneratedCard
import com.example.studyio.data.api.StudyioApiClient
import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// ViewModel to provide StudyioApiClient dependency and handle document upload logic
@HiltViewModel
class DocumentUploadViewModel @Inject constructor(
    val apiClient: StudyioApiClient,
    val cardRepository: CardRepository,
) : ViewModel() {
    
    /**
     * Upload document and generate flashcards, returning proposed cards for user approval
     */
    suspend fun generateCardsFromDocument(
        context: Context,
        uri: Uri
    ): List<GeneratedCard> {
        return try {
            Log.d("DocumentUploadViewModel", "Starting generateCardsFromDocument for URI: $uri")
            
            // Read file and convert to base64
            val inputStream = context.contentResolver.openInputStream(uri) ?: run {
                Log.e("DocumentUploadViewModel", "Failed to open input stream for URI: $uri")
                return emptyList()
            }
            val bytes = inputStream.readBytes()
            inputStream.close()
            val base64File = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val fileName = uri.lastPathSegment
            
            Log.d("DocumentUploadViewModel", "File processed: fileName=$fileName, size=${bytes.size} bytes")
            
            if (fileName.isNullOrEmpty()) {
                Log.e("DocumentUploadViewModel", "File name is null or empty")
                return emptyList()
            }
            
            // Call API to generate flashcards
            Log.d("DocumentUploadViewModel", "Calling API to generate flashcards")
            val result = apiClient.generateFlashcards(fileName, base64File)
            
            when (result) {
                is ApiResult.Success -> {
                    Log.d("DocumentUploadViewModel", "API call successful, received data: ${result.data}")
                    result.data.data.cards
                }
                is ApiResult.Error -> {
                    Log.e("DocumentUploadViewModel", "API call failed: ${result.message}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("DocumentUploadViewModel", "Exception in generateCardsFromDocument", e)
            emptyList()
        }
    }

    /**
     * Add approved cards to the deck
     */
    suspend fun addCardsToDeck(cards: List<GeneratedCard>, deckId: String): ApiResult<String> {
        return try {
            for (cardData in cards) {
                val card = Card(
                    deckId = deckId,
                    front = cardData.front,
                    back = cardData.back,
                )
                cardRepository.insertCard(card)
            }
            ApiResult.Success("${cards.size} cards added to deck successfully!")
        } catch (e: Exception) {
            ApiResult.Error("Error adding cards to deck: ${e.message}")
        }
    }
}
