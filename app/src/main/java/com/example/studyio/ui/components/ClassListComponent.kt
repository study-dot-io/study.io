package com.example.studyio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studyio.viewmodel.getClasses
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.compose.material3.Button

@Composable
fun renderClassList(navController: NavController) {
    val classList = getClasses()
    // Making a table to display the list of classes
    // Should probabyl make this a component later but im lazy
    LazyColumn(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        // Header
        item {
            Row(Modifier.background(Color.Blue)) {
                Text(text = "Classes")
            }
        }
        // Classes
        items(classList.size) { i ->
            Column {
                Row(Modifier.fillMaxWidth()) {
                    Button(onClick = {navController.navigate("selectedClassScreen/${classList[i]}")}) {
                        Text(
                            text = classList[i],
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        )

                    }

                }
                HorizontalDivider( // Line below each row
                    color = Color.Gray,
                    thickness = 1.dp
                )
            }
        }
    }
}