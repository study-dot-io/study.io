package com.example.studyio.data.sync

import android.util.Log
import com.example.studyio.data.api.StudyioApiClient
import com.example.studyio.data.api.SyncResponse
import com.example.studyio.data.entities.CardDao
import com.example.studyio.data.entities.DeckDao
import com.example.studyio.events.Events
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncService @Inject constructor (
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
    private val apiClient: StudyioApiClient,
) {
    private var isSyncing = false
    private var lastSyncTime: Long = 0

    suspend fun sync() {
        if (isSyncing) {
            log("Sync skipped: already in progress")
            return
        }

        runSync()
    }

    private suspend fun saveDataToLocal(data: SyncResponse) {
        log("Saving data to local database")

        // Save decks
        data.decks.forEach { deck ->
            try{
                deckDao.insertDeck(deck)
            } catch (e: Exception) {
                deckDao.updateDeck(deck)
            }
        }

        // Save cards
        data.cards.forEach { card ->
            try{
                cardDao.insertCard(card)
            } catch (e: Exception) {
                cardDao.updateCard(card)
            }
        }

        Events.decksUpdated()

        log("Data saved successfully")
    }

    private suspend fun runSync() {
        log("Running sync")
        isSyncing = true
        try {
            val decks = deckDao.getAllDecksStateless()
            val cards = cardDao.getAllCards()

             apiClient.syncData(decks, cards).onSuccess { data ->
                 markAllSynced()
                 saveDataToLocal(data)
             }.onError {
                 log("Sync failed: $it")
             }

            lastSyncTime = System.currentTimeMillis()
        } finally {
            isSyncing = false
        }
    }

    private suspend fun markAllSynced() {
        deckDao.markAllSynced()
        cardDao.markAllSynced()
    }

    private fun log(msg: String) {
        Log.d("SyncService", msg)
    }
}
