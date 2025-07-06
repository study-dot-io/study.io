package com.example.studyio

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.studyio.ui.StudyIONavHost
import com.example.studyio.ui.theme.StudyIOTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "StudyIO-MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Log build version at startup -- sanity check to make sure code change was applied (gradle clean -> gradle build)
        Log.i(TAG, "StudyIO Starting - 4PM LAST CHANGED")

        setContent {
            StudyIOTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudyIONavHost()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudyIOAppPreview() {
    StudyIOTheme {
        StudyIONavHost()
    }
}