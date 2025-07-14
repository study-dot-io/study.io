package com.example.studyio.ui.screens
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val isSignedIn = remember { mutableStateOf(auth.currentUser != null) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { res ->
        onSignInResult(res, isSignedIn)
    }

    // Listen for auth state changes
    DisposableEffect(Unit) {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            val wasSignedIn = isSignedIn.value
            isSignedIn.value = currentUser != null
            
            if (!wasSignedIn && currentUser != null) {
                onAuthSuccess()
            }
        }
        
        auth.addAuthStateListener(authStateListener)
        
        onDispose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    // Check if user is already signed in when screen loads
    LaunchedEffect(Unit) {
        if (isSignedIn.value) {
            onAuthSuccess()
        }
    }

    // This text indicates that we are on the auth screen... helplful for debugging screen sta
    Text(
        text = "Auth Screen - Signed In: ${isSignedIn.value}",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier
            .padding(top = 64.dp, start = 24.dp, end = 24.dp)
    )

    LaunchedEffect(isSignedIn.value) {
        if (!isSignedIn.value) {
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

private fun onSignInResult(
    result: FirebaseAuthUIAuthenticationResult,
    isSignedIn: MutableState<Boolean>,
) {
    val success = result.resultCode == Activity.RESULT_OK
    if (success) {
        // The auth state listener will handle calling onAuthSuccess
        // when the auth state actually changes
        isSignedIn.value = true
    }
}
