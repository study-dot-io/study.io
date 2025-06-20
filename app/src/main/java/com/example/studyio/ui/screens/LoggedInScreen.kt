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

@Composable
fun LoggedInScreen() {
    Column(modifier= Modifier.padding(16.dp)) {
        Text(text="Row 1", modifier = Modifier.fillMaxWidth())

        Spacer(modifier= Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text="Col 1")
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text="Col 2")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text="Row 3", modifier = Modifier.fillMaxWidth())
    }
}