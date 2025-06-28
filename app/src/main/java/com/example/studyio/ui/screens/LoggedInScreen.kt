package com.example.studyio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studyio.ui.components.renderClassList

@Composable
fun LoggedInScreen(navController: NavController) {
    Column(modifier= Modifier.padding(16.dp)) {
        Text(text="Hello, Aditya", modifier = Modifier.fillMaxWidth())

        Spacer(modifier= Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // My classes component
                renderClassList(navController)
            }

            Column(modifier = Modifier.weight(1f)) {
                // Add classes component
                Text("Add class")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Upcoming deadlines component
        Text("Upcoming deadlines")

    }
}