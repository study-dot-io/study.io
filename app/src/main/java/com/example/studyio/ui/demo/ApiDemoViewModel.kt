package com.example.studyio.ui.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.api.StudyioApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Demo ViewModel showing how to make authenticated API calls
 * This is an example on how to integrate API calls in a ViewModel
 */
@HiltViewModel
class ApiDemoViewModel @Inject constructor(
    private val apiClient: StudyioApiClient
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(ApiDemoUiState())
    val uiState: StateFlow<ApiDemoUiState> = _uiState.asStateFlow()
    
    /**
     * Example 1: Simple health check (no authentication required)
     */
    fun checkServerHealth() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, lastResult = "")
            
            apiClient.healthCheck()
                .onSuccess { healthResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastResult = "✅ Server healthy: ${healthResponse.service}"
                    )
                }
                .onError { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastResult = "❌ Health check failed: $error"
                    )
                }
        }
    }
    
    /**
     * Example 2: Verify Firebase token (POST request)
     */
    fun verifyFirebaseToken() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, lastResult = "")
            
            apiClient.verifyToken()
                .onSuccess { authResponse ->
                    if (authResponse.authenticated) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            lastResult = "✅ Token verified for user: ${authResponse.email}"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            lastResult = "❌ Token verification failed: ${authResponse.error}"
                        )
                    }
                }
                .onError { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastResult = "❌ Token verification error: $error"
                    )
                }
        }
    }
    
    /**
     * This demonstrates the typical flow for authenticated API calls
     */
    fun accessProtectedRoute() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, lastResult = "")
            
            // This is the key method call that demonstrates authenticated API access
            apiClient.getProtectedData()
                .onSuccess { authResponse ->
                    if (authResponse.authenticated) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            lastResult = "✅ Protected route accessed successfully!\nMessage: ${authResponse.message}\nUser: ${authResponse.email}"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            lastResult = "❌ Access denied: ${authResponse.message}"
                        )
                    }
                }
                .onError { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastResult = "❌ Protected route error: $error"
                    )
                }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(lastResult = "")
    }
}

/**
 * UI State for the demo screen
 */
data class ApiDemoUiState(
    val isLoading: Boolean = false,
    val lastResult: String = ""
)
