package com.example.studyio.ui.importer

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.entities.StudyioDatabase
import com.example.studyio.data.importAnkiApkgFromStream
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val db: StudyioDatabase
) : ViewModel() {
    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting
    private val _importMessage = MutableStateFlow("")
    val importMessage: StateFlow<String> = _importMessage

    fun importApkg(context: Context, uri: Uri, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        _isImporting.value = true
        _importMessage.value = "Preparing import..."
        viewModelScope.launch {
            try {
                _importMessage.value = "Opening file..."
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    _importMessage.value = "Reading APKG file..."
                    importAnkiApkgFromStream(
                        inputStream = inputStream,
                        db = db,
                        tempDir = context.cacheDir,
                        onProgress = { msg -> _importMessage.value = msg }
                    )
                    _isImporting.value = false
                    _importMessage.value = ""
                    onComplete(true, "Import completed successfully!")
                } ?: run {
                    _isImporting.value = false
                    _importMessage.value = ""
                    onComplete(false, "Failed to open selected file")
                }
            } catch (e: Exception) {
                _isImporting.value = false
                _importMessage.value = ""
                onComplete(false, "Import failed: ${e.message}")
            }
        }
    }
}

