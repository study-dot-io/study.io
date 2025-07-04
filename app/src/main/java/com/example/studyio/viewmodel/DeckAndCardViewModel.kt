package com.example.studyio.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.model.Deck
import com.example.studyio.data.model.ReviewCards
import com.example.studyio.data.repository.DeckRepository
import com.example.studyio.data.repository.ReviewCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckViewModel @Inject constructor(
    private val deckRepository: DeckRepository
) : ViewModel() {
    val decks = mutableStateOf<List<Deck>>(emptyList())
    init {
        viewModelScope.launch {
            deckRepository.insertDummyData()
            decks.value = deckRepository.getAlldecks()
        }
    }
}

@HiltViewModel
class CardViewModel @Inject constructor(
    private val reviewCardRepository: ReviewCardRepository
) : ViewModel() {
    val cards = mutableStateOf<List<ReviewCards>>(emptyList())
    init {
        viewModelScope.launch {
            reviewCardRepository.insertDummyCards()
            cards.value = reviewCardRepository.getAllReviewCards(1)
        }
    }
}

