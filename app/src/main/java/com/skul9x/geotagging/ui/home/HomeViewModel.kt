package com.skul9x.geotagging.ui.home

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.skul9x.geotagging.data.model.GeoImage
import com.skul9x.geotagging.utils.ExifUtils
import com.skul9x.geotagging.utils.ExifWriteResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed class HomeUiEvent {
    data class ShowSnackbar(val message: String) : HomeUiEvent()
    data class RequestWritePermission(val intentSender: IntentSender) : HomeUiEvent()
}

class HomeViewModel @JvmOverloads constructor(
    application: Application,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<HomeUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var pendingLocationUpdate: Pair<Double, Double>? = null

    // Xử lý ảnh chọn lẻ từ Photo Picker
    fun addImages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch(ioDispatcher) {
            val newImages = uris.mapNotNull { uri ->
                processUri(uri)
            }
            updateImageList(newImages)
        }
    }

    // Xử lý chọn cả thư mục (Folder)
    fun loadImagesFromFolder(treeUri: Uri) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch(ioDispatcher) {
            // 1. Giữ quyền truy cập lâu dài vào thư mục này
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Dùng DocumentFile để duyệt thư mục
            val documentFile = DocumentFile.fromTreeUri(context, treeUri)
            if (documentFile == null || !documentFile.isDirectory) {
                sendEvent(HomeUiEvent.ShowSnackbar("Không đọc được thư mục"))
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            // 3. Lọc file ảnh và convert sang GeoImage
            val newImages = documentFile.listFiles()
                .filter { file ->
                    file.isFile && (file.type?.startsWith("image/") == true)
                }
                .mapNotNull { file ->
                    processUri(file.uri, file.name ?: "Unknown", file.length())
                }

            if (newImages.isEmpty()) {
                sendEvent(HomeUiEvent.ShowSnackbar("Thư mục trống hoặc không có ảnh"))
                _uiState.update { it.copy(isLoading = false) }
            } else {
                updateImageList(newImages)
                sendEvent(HomeUiEvent.ShowSnackbar("Đã tải ${newImages.size} ảnh từ thư mục"))
            }
        }
    }

    private fun updateImageList(newImages: List<GeoImage>) {
        _uiState.update { currentState ->
            val currentUris = currentState.images.map { it.uri }.toSet()
            val distinctNewImages = newImages.filter { it.uri !in currentUris }
            
            currentState.copy(
                images = currentState.images + distinctNewImages,
                imageCount = currentState.images.size + distinctNewImages.size,
                isLoading = false
            )
        }
    }

    private fun processUri(uri: Uri, knownName: String? = null, knownSize: Long = 0): GeoImage? {
        // Nếu dùng Photo Picker, cần xin quyền persistable
        if (knownName == null) {
             try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (t: Throwable) { /* Ignore */ }
        }

        var name = knownName ?: "Unknown"
        var size = knownSize
        
        // Nếu chưa biết tên/size (từ PhotoPicker), query lại
        if (knownName == null) {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) name = cursor.getString(nameIndex) ?: "Unknown"
                        if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                    }
                }
            } catch (t: Throwable) { /* Ignore query errors */ }
        }

        val location = ExifUtils.readLocation(context, uri)

        return GeoImage(
            id = UUID.randomUUID().toString(),
            uri = uri,
            name = name,
            dateAdded = System.currentTimeMillis(),
            size = size,
            latitude = location?.first,
            longitude = location?.second,
            isPickerUri = ExifUtils.isPickerUri(uri)
        )
    }

    fun removeImage(image: GeoImage) {
        _uiState.update { state ->
            val updatedList = state.images.filter { it.id != image.id }
            state.copy(
                images = updatedList,
                imageCount = updatedList.size
            )
        }
    }

    fun clearAllImages() {
        _uiState.update { 
            it.copy(images = emptyList(), imageCount = 0) 
        }
        sendEvent(HomeUiEvent.ShowSnackbar("Đã xoá danh sách"))
    }

    fun updateLocationForImages(latitude: Double, longitude: Double) {
        val currentImages = _uiState.value.images
        if (currentImages.isEmpty()) return

        pendingLocationUpdate = Pair(latitude, longitude)

        _uiState.update { 
            it.copy(
                isProcessing = true, 
                processProgress = 0f,
                showEditDialog = false 
            ) 
        }

        viewModelScope.launch(ioDispatcher) {
            val total = currentImages.size
            var successCount = 0
            var permissionRequested = false
            
            val updatedImages = currentImages.mapIndexed { index, image ->
                // Route: Picker URIs → copy-modify-save, others → direct write
                val result = if (image.isPickerUri) {
                    ExifUtils.writeLocationViaCopy(
                        context, image.uri, image.name, latitude, longitude
                    )
                } else {
                    ExifUtils.writeLocation(context, image.uri, latitude, longitude)
                }

                val updatedImage = when (result) {
                    is ExifWriteResult.Success -> {
                        successCount++
                        image.copy(latitude = latitude, longitude = longitude)
                    }
                    is ExifWriteResult.CopyWriteSuccess -> {
                        successCount++
                        // Switch to the new writable MediaStore URI for future edits
                        image.copy(
                            uri = result.newUri,
                            latitude = latitude,
                            longitude = longitude,
                            isPickerUri = false  // Now it's a MediaStore URI we own
                        )
                    }
                    is ExifWriteResult.PermissionDenied -> {
                        if (!permissionRequested) {
                            val intentSender = getIntentSenderFromException(context, result.exception, image.uri)
                            if (intentSender != null) {
                                permissionRequested = true
                                sendEvent(HomeUiEvent.RequestWritePermission(intentSender))
                            } else {
                                sendEvent(HomeUiEvent.ShowSnackbar("Không có quyền ghi EXIF cho ảnh: ${image.name}"))
                            }
                        }
                        image
                    }
                    is ExifWriteResult.FileNotFound -> {
                        sendEvent(HomeUiEvent.ShowSnackbar("Không tìm thấy file ảnh: ${image.name}"))
                        image
                    }
                    is ExifWriteResult.WriteFailed -> {
                        sendEvent(HomeUiEvent.ShowSnackbar("Lỗi khi ghi toạ độ GPS cho ảnh: ${image.name}"))
                        image
                    }
                }
                
                _uiState.update { 
                    it.copy(processProgress = (index + 1).toFloat() / total) 
                }

                updatedImage
            }

            _uiState.update {
                it.copy(
                    images = updatedImages,
                    isProcessing = false,
                    processProgress = 0f
                )
            }
            
            if (!permissionRequested && successCount > 0) {
                sendEvent(HomeUiEvent.ShowSnackbar("Hoàn tất: Đã cập nhật GPS cho $successCount/$total ảnh"))
            }
        }
    }

    fun onWritePermissionGranted() {
        pendingLocationUpdate?.let { (lat, lng) ->
            updateLocationForImages(lat, lng)
        }
    }

    private fun getIntentSenderFromException(context: Context, exception: SecurityException, uri: Uri): IntentSender? {
        return try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    try {
                        MediaStore.createWriteRequest(context.contentResolver, listOf(uri)).intentSender
                    } catch (e: Exception) {
                        (exception as? RecoverableSecurityException)?.userAction?.actionIntent?.intentSender
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    (exception as? RecoverableSecurityException)?.userAction?.actionIntent?.intentSender
                }
                else -> null
            }
        } catch (t: Throwable) {
            (exception as? RecoverableSecurityException)?.userAction?.actionIntent?.intentSender
        }
    }

    fun showEditDialog(show: Boolean) {
        _uiState.update { it.copy(showEditDialog = show) }
    }

    private fun sendEvent(event: HomeUiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}

data class HomeUiState(
    val images: List<GeoImage> = emptyList(),
    val imageCount: Int = 0,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val processProgress: Float = 0f,
    val showEditDialog: Boolean = false
)