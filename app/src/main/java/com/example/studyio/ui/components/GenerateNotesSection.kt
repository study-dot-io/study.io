package com.example.studyio.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studyio.viewmodel.getClasses
import androidx.compose.ui.Alignment

@Composable
fun GenerateNotesSection() {
    val classList = getClasses().take(2) // Limit to 2 like in screenshot

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp)
                .clickable { /* TODO: Navigate to full notes screen */ },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Generate notes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            classList.forEachIndexed { index, course ->
                val scheduledDays = if (index == 0) "Monday, Wednesday" else "Tuesday, Thursday" // Placeholder
                NotesItem(course, scheduledDays)
            }
        }
    }
}

@Composable
fun NotesItem(course: String, days: String) {
    var checked by remember { mutableStateOf(false) }

//    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
            ) {
                Text(text = course, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = "Scheduled for: $days", style = MaterialTheme.typography.bodySmall)
            }
            Checkbox(checked = checked, onCheckedChange = { checked = it })
        }

//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 4.dp),
//            horizontalArrangement = Arrangement.Start,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text("Mark as completed for today")
//            Spacer(modifier = Modifier.width(8.dp))
//            Checkbox(checked = checked, onCheckedChange = { checked = it })
//        }
//    }
}
