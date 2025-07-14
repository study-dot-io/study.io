package com.example.studyio.ui.screens
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyio.ui.auth.AuthViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val user by authViewModel.currentUser.collectAsState()

    val signInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { res ->
    }

    // Check if user is already signed in when screen loads or when auth state changes
    LaunchedEffect(user) {
        if (user != null) {
            onAuthSuccess()
        }
    }

    // This text indicates that we are on the auth screen... helpful for debugging screen state
    Text(
        text = "Auth Screen - Signed In: ${user != null}",
        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
        modifier = androidx.compose.ui.Modifier
            .padding(top = 64.dp, start = 24.dp, end = 24.dp)
    )

    LaunchedEffect(user) {
        if (user == null) {
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)
        }
    }
}