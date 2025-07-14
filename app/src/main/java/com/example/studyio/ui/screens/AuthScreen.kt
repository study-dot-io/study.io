package com.example.studyio.ui.screens

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val isSignedIn = remember { mutableStateOf(auth.currentUser != null) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { res ->
        onSignInResult(res, isSignedIn, onAuthSuccess)
    }

    LaunchedEffect(Unit) {
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
        } else {
            onAuthSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!isSignedIn.value) {
            Text("Signing in...")
        } else {
            Button(onClick = {
                AuthUI.getInstance().signOut(context).addOnCompleteListener {
                    isSignedIn.value = false
                }
            }) {
                Text("Sign Out")
            }
        }
    }
}

private fun onSignInResult(
    result: FirebaseAuthUIAuthenticationResult,
    isSignedIn: androidx.compose.runtime.MutableState<Boolean>,
    onAuthSuccess: () -> Unit
) {
    val response = result.idpResponse
    if (result.resultCode == Activity.RESULT_OK) {
        isSignedIn.value = true
        onAuthSuccess()
    } else {
        // Sign in failed or cancelled
        isSignedIn.value = false
    }
}
