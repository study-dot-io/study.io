package com.example.studyio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
    HomeScreen(
        onDeckClick = { deck ->
            // TODO: Navigate to deck detail/study screen
        },
        onCreateDeck = {
            // TODO: Navigate to create deck screen
        },
        onStudyNow = {
            // TODO: Navigate to study screen
        }
    )
}

@Preview(showBackground = true)
@Composable
fun StudyIOAppPreview() {
    StudyIOTheme {
        StudyIOApp()
    }
} 