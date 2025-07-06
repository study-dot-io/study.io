package com.example.studyio.util

import android.content.Context
import android.net.Uri
import com.example.studyio.data.entities.StudyioDatabase
import com.example.studyio.data.importAnkiApkgFromStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

fun importAnkiApkg(
    context: Context,
    db: StudyioDatabase,
    uri: Uri,
    onProgress: (String) -> Unit = {},
    onComplete: (Boolean, String) -> Unit = { _, _ -> }
) {
    val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    appScope.launch {
        try {
            onProgress("Opening file...")
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                onProgress("Reading APKG file...")
                importAnkiApkgFromStream(
                    inputStream = inputStream,
                    db = db,
                    tempDir = context.cacheDir,
                    onProgress = onProgress
                )
                onComplete(true, "Import completed successfully!")
            } ?: run {
                onComplete(false, "Failed to open selected file")
            }
        } catch (e: Exception) {
            onComplete(false, "Import failed: ${e.message}")
        }
    }
}

