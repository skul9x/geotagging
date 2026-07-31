package com.skul9x.geotagging.ui.range

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skul9x.geotagging.data.model.FileItem
import com.skul9x.geotagging.data.model.FileOperationMode
import com.skul9x.geotagging.utils.FileOperationResult
import com.skul9x.geotagging.utils.FileOperationsHelper
import com.skul9x.geotagging.utils.FileRangeFilter
import com.skul9x.geotagging.utils.NaturalOrderComparator
import com.skul9x.geotagging.utils.OperationProgress
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FileRangeUiState(
    val sourceDirectoryUri: String? = null,
    val targetDirectoryUri: String? = null,
    val sourceFiles: List<FileItem> = emptyList(),
    val startFileName: String = "",
    val endFileName: String = "",
    val selectedFiles: List<FileItem> = emptyList(),
    val selectedCount: Int = 0,
    val totalSelectedSize: Long = 0L,
    val operationMode: FileOperationMode = FileOperationMode.COPY,
    val createSubfolder: Boolean = false,
    val subfolderName: String = "",
    val enterNewSubfolder: Boolean = false,
    val isOperating: Boolean = false,
    val progress: OperationProgress = OperationProgress(),
    val operationResult: FileOperationResult? = null,
    val showResultModal: Boolean = false
)

sealed interface FileRangeUiEvent {
    data class ShowSnackbar(val message: String) : FileRangeUiEvent
    data class OperationFinished(val successCount: Int, val failureCount: Int) : FileRangeUiEvent
}

class FileRangeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FileRangeUiState())
    val uiState: StateFlow<FileRangeUiState> = _uiState.asStateFlow()

    private val _events = Channel<FileRangeUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val comparator = NaturalOrderComparator()

    fun selectSourceDirectory(context: Context?, uri: Uri) {
        val files = FileOperationsHelper.queryFilesInDirectory(context, uri)
        val uriStr = try { uri.toString() } catch (_: Exception) { "" }
        setSourceFiles(files, uriStr)
    }

    fun selectSourceDirectoryPath(path: String) {
        val files = FileOperationsHelper.queryFilesInPath(path)
        setSourceFiles(files, path)
    }

    fun setSourceFiles(files: List<FileItem>, uriString: String? = null) {
        val sortedFiles = files.sortedWith { f1, f2 -> comparator.compare(f1.name, f2.name) }
        _uiState.update { currentState ->
            currentState.copy(
                sourceDirectoryUri = uriString ?: currentState.sourceDirectoryUri,
                sourceFiles = sortedFiles
            )
        }
        updateRangeSelection()
    }

    fun selectTargetDirectory(uri: Uri) {
        val uriStr = try { uri.toString() } catch (_: Exception) { "" }
        setTargetDirectoryUri(uriStr)
    }

    fun selectTargetDirectoryPath(path: String) {
        setTargetDirectoryUri(path)
    }

    fun setTargetDirectoryUri(uriString: String?) {
        _uiState.update { it.copy(targetDirectoryUri = uriString) }
    }

    fun setStartFileName(name: String) {
        _uiState.update { it.copy(startFileName = name) }
        updateRangeSelection()
    }

    fun setEndFileName(name: String) {
        _uiState.update { it.copy(endFileName = name) }
        updateRangeSelection()
    }

    fun setOperationMode(mode: FileOperationMode) {
        _uiState.update { it.copy(operationMode = mode) }
    }

    fun setCreateSubfolder(enabled: Boolean) {
        _uiState.update { it.copy(createSubfolder = enabled) }
    }

    fun setSubfolderName(name: String) {
        _uiState.update { it.copy(subfolderName = name) }
    }

    fun setEnterNewSubfolder(enabled: Boolean) {
        _uiState.update { it.copy(enterNewSubfolder = enabled) }
    }

    fun dismissResultModal() {
        _uiState.update { it.copy(showResultModal = false, operationResult = null) }
    }

    private fun updateRangeSelection() {
        val state = _uiState.value
        val filtered = FileRangeFilter.filterRange(
            files = state.sourceFiles,
            startFileName = state.startFileName,
            endFileName = state.endFileName
        )
        _uiState.update {
            it.copy(
                selectedFiles = filtered,
                selectedCount = filtered.size,
                totalSelectedSize = filtered.sumOf { file -> file.size }
            )
        }
    }

    fun startOperation(context: Context?) {
        val state = _uiState.value
        val targetUriStr = state.targetDirectoryUri
        if (targetUriStr.isNullOrEmpty()) {
            viewModelScope.launch {
                _events.send(FileRangeUiEvent.ShowSnackbar("Target directory is not selected"))
            }
            return
        }

        if (state.selectedFiles.isEmpty()) {
            viewModelScope.launch {
                _events.send(FileRangeUiEvent.ShowSnackbar("No files selected in range"))
            }
            return
        }

        // Prevent accidental move operation when target is same as source directory
        if (state.operationMode == FileOperationMode.MOVE && targetUriStr == state.sourceDirectoryUri && !state.createSubfolder) {
            viewModelScope.launch {
                _events.send(FileRangeUiEvent.ShowSnackbar("Cannot move files: Target directory is identical to source directory."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isOperating = true,
                    operationResult = null,
                    showResultModal = false
                )
            }

            var effectiveTargetUriStr = targetUriStr
            if (state.createSubfolder && state.subfolderName.isNotBlank()) {
                val createdFolderStr = FileOperationsHelper.createSubfolderPath(
                    targetPath = effectiveTargetUriStr,
                    folderName = state.subfolderName.trim()
                )
                if (createdFolderStr != null) {
                    if (state.enterNewSubfolder) {
                        setTargetDirectoryUri(createdFolderStr)
                    }
                    effectiveTargetUriStr = createdFolderStr
                }
            }

            val result = FileOperationsHelper.executeOperationPath(
                context = context,
                files = state.selectedFiles,
                targetPathStr = effectiveTargetUriStr,
                mode = state.operationMode,
                onProgress = { progress ->
                    _uiState.update { it.copy(progress = progress) }
                }
            )

            if (state.operationMode == FileOperationMode.MOVE && state.sourceDirectoryUri != null) {
                val updatedFiles = FileOperationsHelper.queryFilesInPath(state.sourceDirectoryUri)
                setSourceFiles(updatedFiles)
            }

            _uiState.update {
                it.copy(
                    isOperating = false,
                    operationResult = result,
                    showResultModal = true
                )
            }

            _events.send(FileRangeUiEvent.OperationFinished(result.successCount, result.failureCount))
        }
    }
}
