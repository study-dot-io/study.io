package com.example.studyio.data.entities

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeckRepository @Inject constructor(private val deckDao: DeckDao) {
    suspend fun getAllDecks(): List<Deck> = deckDao.getAllDecks()
    suspend fun insertDeck(deck: Deck) = deckDao.insertDeck(deck)
    suspend fun deleteDeck(deckId: String) = deckDao.deleteDeck(deckId)
    suspend fun getDueCardsCount(deckId: String): Int = deckDao.getDueCardsCount(deckId)
}

@Singleton
class CardRepository @Inject constructor(private val cardDao: CardDao) {
    suspend fun getDueCards(deckId: String, limit: Int = 200): List<Card> = cardDao.getDueCards(deckId, limit)
    suspend fun updateCard(card: Card) = cardDao.updateCard(card)
}
