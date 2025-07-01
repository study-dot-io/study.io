package com.example.studyio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.studyio.ui.AppState
import com.example.studyio.ui.Screen
import com.example.studyio.ui.screens.CreateDeckScreen
import com.example.studyio.ui.screens.HomeScreen
import com.example.studyio.ui.theme.StudyIOTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyIOTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudyIOApp()
                }
            }
        }
    }
}

@Composable
fun StudyIOApp() {
    var appState by remember { mutableStateOf(AppState()) }
    
    when (appState.currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                decks = appState.decks,
                dueCards = appState.dueCards,
                todayReviews = appState.todayReviews,
                totalCards = appState.totalCards,
                totalDecks = appState.totalDecks,
                onDeckClick = { deck ->
                    // TODO: Navigate to deck detail/study screen
                },
                onCreateDeck = {
                    appState = appState.navigateTo(Screen.CreateDeck)
                },
                onStudyNow = {
                    // TODO: Navigate to study screen
                }
            )
        }
        is Screen.CreateDeck -> {
            CreateDeckScreen(
                onBackPressed = {
                    appState = appState.navigateTo(Screen.Home)
                },
                onDeckCreated = { newDeck ->
                    appState = appState.addDeck(newDeck).navigateTo(Screen.Home)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudyIOAppPreview() {
    StudyIOTheme {
        StudyIOApp()
    }
} 