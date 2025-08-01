package com.example.studyio.ui.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyio.ui.auth.AuthViewModel

/**
 * Demo screen showing API integration examples
 * 1. Make API calls from Compose UI
 * 2. Handle authentication state
 * 3. Display results and handle errors
 * 4. Structure authenticated API calls properly
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiDemoScreen(
    modifier: Modifier = Modifier,
    apiDemoViewModel: ApiDemoViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by apiDemoViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Home"
                )
            }
        }
        
        Text(
            text = "API Integration Demo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Authentication Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (currentUser != null) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Authentication Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (currentUser != null) {
                        "✅ Signed in as: ${currentUser?.email}"
                    } else {
                        "❌ Not signed in - some API calls will fail"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Instructions for Groupmates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = """
                    This demo shows how to make authenticated API calls:
                    
                    1. Start your Flask server: python server/app.py
                    2. Create a port forward to your Android emulator so it can access the server running on your laptop:
                       adb reverse tcp:5000 tcp:5000
                    3. Try the buttons below to see different API patterns
                    4. Check StudyioApiClient.kt for implementation details
                    5. Use getProtectedData() as the main pattern for authenticated calls
                    
                    Key Pattern:
                    - Get Firebase ID token from current user
                    - Add "Bearer <token>" to Authorization header
                    - Handle success/error responses appropriately
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // API Demo Buttons
        Text(
            text = "API Test Buttons",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Health Check (no auth required)
        Button(
            onClick = { apiDemoViewModel.checkServerHealth() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("1. Health Check (No Auth Required)")
        }
        
        // Token Verification (POST request)
        Button(
            onClick = { apiDemoViewModel.verifyFirebaseToken() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("2. Verify Firebase Token (POST)")
        }
        
        // Protected Route Access (main pattern)
        Button(
            onClick = { apiDemoViewModel.accessProtectedRoute() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("3. Access Protected Route (MAIN PATTERN)")
        }
        
        // Clear Results
        OutlinedButton(
            onClick = { apiDemoViewModel.clearResult() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.lastResult.isNotEmpty()
        ) {
            Text("Clear Results")
        }
        
        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Results Display
        if (uiState.lastResult.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "API Response",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.lastResult,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Code Examples
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Key Code Pattern",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = """
                    // In your ViewModel:
                    fun callProtectedApi() {
                        viewModelScope.launch {
                            apiClient.getProtectedData()
                                .onSuccess { response ->
                                    // Handle success
                                    if (response.authenticated) {
                                        // Use the data
                                    }
                                }
                                .onError { error ->
                                    // Handle error
                                }
                        }
                    }
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}
