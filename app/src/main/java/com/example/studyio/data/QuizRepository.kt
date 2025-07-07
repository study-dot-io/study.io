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
    suspend fun getDueCards(deckId: Long, limit: Int = 200): List<Card> = cardDao.getCardsForDeck(deckId, limit)
    suspend fun getNotesByIds(noteIds: List<Long>): Map<Long, Note> = noteDao.getNotesByIds(noteIds).associateBy { it.id }
    suspend fun updateCard(card: Card) = cardDao.updateCard(card)
}
