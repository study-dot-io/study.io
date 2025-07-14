package com.example.studyio.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    // Create a scope for the auth state flow that survives across composables
    private val authScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // StateFlow that emits the current user state
    val currentUser: StateFlow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        
        // Send initial state
        trySend(auth.currentUser)
        
        // Register listener
        auth.addAuthStateListener(authStateListener)
        
        // Clean up when flow is cancelled
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }.stateIn(
        scope = authScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = auth.currentUser
    )
    
    fun signOut() {
        auth.signOut()
    }
    
    override fun onCleared() {
        super.onCleared()
        authScope.cancel()
    }
}