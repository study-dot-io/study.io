package com.example.studyio.data

import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.CardDao
import com.example.studyio.data.entities.Note
import com.example.studyio.data.entities.NoteDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val cardDao: CardDao,
    private val noteDao: NoteDao
) {
    suspend fun getDueCards(deckId: Long): List<Card> = cardDao.getCardsForDeck(deckId) // You may want to filter for due cards only
    suspend fun getNotesByIds(noteIds: List<Long>): Map<Long, Note> = noteDao.getNotesByIds(noteIds).associateBy { it.id }
    suspend fun updateCard(card: Card) = cardDao.updateCard(card)
}

