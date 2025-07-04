package com.example.studyio.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyio.viewmodel.CardViewModel
@Composable
fun CardListScreen(deckId: Long, viewModel: CardViewModel = hiltViewModel()) {
    val cards by viewModel.cards
//    LazyColumn {
//        items(cards.size) { card ->
//            Text(text = "${card.front} → ${card.back}")
//        }
//    }
}

@Composable
fun SelectedClassScreen(
    classId: String,
    navController: NavController

){
    CardListScreen(1)
}
