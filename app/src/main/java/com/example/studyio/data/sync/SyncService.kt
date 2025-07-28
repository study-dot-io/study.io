package com.example.studyio.data.sync

import android.util.Log
import com.example.studyio.data.api.StudyioApiClient
import com.example.studyio.data.api.SyncRequest
import com.example.studyio.data.entities.CardDao
import com.example.studyio.data.entities.DeckDao
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
        val now = System.currentTimeMillis()

        if (isSyncing) {
            log("Sync skipped: already in progress")
            return
        }

        runSync()
    }

    private suspend fun runSync() {
        log("Running sync")
        isSyncing = true
        try {
            val request = getSyncRequest()

             apiClient.syncData(request).onSuccess {
                 markAllSynced()
             }.onError {
                 log("Sync failed: $it")
             }

            lastSyncTime = System.currentTimeMillis()
        } finally {
            isSyncing = false
        }
    }

    private suspend fun getSyncRequest(): SyncRequest {
        val unsyncedDecks = deckDao.getUnsynced()
        val unsyncedCards = cardDao.getUnsynced()

        return SyncRequest(unsyncedDecks, unsyncedCards)
    }

    private suspend fun markAllSynced() {
        deckDao.markAllSynced()
        cardDao.markAllSynced()
    }

    private fun log(msg: String) {
        Log.d("SyncService", msg)
    }
}
