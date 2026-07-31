package com.skul9x.geotagging.ui.home

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.skul9x.geotagging.data.model.GeoImage
import com.skul9x.geotagging.utils.GpsCoordinateParser
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    // Launcher: Ghi file Scoped Storage (IntentSender result)
    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onWritePermissionGranted()
        }
    }

    // Listen for UI events (Toast/Snackbar/WritePermission)
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HomeUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is HomeUiEvent.RequestWritePermission -> {
                    val intentSenderRequest = IntentSenderRequest.Builder(event.intentSender).build()
                    writePermissionLauncher.launch(intentSenderRequest)
                }
            }
        }
    }

    // 1. Launcher: Chọn nhiều ảnh lẻ (Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        viewModel.addImages(uris)
    }

    // 2. Launcher: Chọn thư mục (OpenDocumentTree)
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            // Cần cờ persist để truy cập lâu dài
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(uri, flag)
            } catch (e: Exception) { /* Ignore */ }
            
            viewModel.loadImagesFromFolder(uri)
        }
    }

    if (uiState.showEditDialog) {
        EditLocationDialog(
            onDismiss = { viewModel.showEditDialog(false) },
            onConfirm = { lat, long -> viewModel.updateLocationForImages(lat, long) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Batch Geotagging")
                        Text(
                            text = if (uiState.imageCount > 0) "${uiState.imageCount} ảnh đã chọn" else "Chọn ảnh để bắt đầu",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    if (uiState.images.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAllImages() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Xoá tất cả")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    // Nút chọn ảnh lẻ
                    TextButton(onClick = { 
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Ảnh")
                    }
                    
                    Spacer(Modifier.width(8.dp))

                    // Nút chọn thư mục
                    TextButton(onClick = { 
                        folderPickerLauncher.launch(null)
                    }) {
                        Icon(Icons.Rounded.FolderOpen, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Thư mục")
                    }
                },
                floatingActionButton = {
                    // Chỉ hiện nút Sửa khi có ảnh
                    if (uiState.images.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = { viewModel.showEditDialog(true) },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            elevation = if (uiState.isProcessing) FloatingActionButtonDefaults.elevation(0.dp) else FloatingActionButtonDefaults.elevation()
                        ) {
                            if (uiState.isProcessing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Edit, contentDescription = "Sửa Location")
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isProcessing) {
                LinearProgressIndicator(
                    progress = { uiState.processProgress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.images.isEmpty() && !uiState.isLoading) {
                    EmptyState()
                } else {
                    ImageGrid(
                        images = uiState.images,
                        onRemove = { viewModel.removeImage(it) }
                    )
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun EditLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, Double) -> Unit
) {
    var quickInputText by remember { mutableStateOf("") }
    var latStr by remember { mutableStateOf("") }
    var longStr by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val currentParsed = remember(latStr, longStr, quickInputText) {
        GpsCoordinateParser.parseCoordinates(latStr, longStr)
            ?: GpsCoordinateParser.parseSingleLineCoordinates(quickInputText)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đặt Location mới") },
        text = {
            Column(modifier = Modifier.animateContentSize()) {
                Text(
                    text = "Nhập toạ độ GPS để áp dụng cho tất cả ảnh đang chọn.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Single line input for Google Maps format
                OutlinedTextField(
                    value = quickInputText,
                    onValueChange = { input ->
                        quickInputText = input
                        val parsed = GpsCoordinateParser.parseSingleLineCoordinates(input)
                        if (parsed != null) {
                            latStr = parsed.first.toString()
                            longStr = parsed.second.toString()
                            error = null
                        } else if (input.isNotBlank()) {
                            error = "Không thể phân tích chuỗi toạ độ nhanh"
                        } else {
                            error = null
                        }
                    },
                    label = { Text("Dán/Nhập chuỗi toạ độ nhanh (Google Maps)") },
                    placeholder = { Text("VD: 21,1573890, 106,1998193") },
                    trailingIcon = {
                        if (quickInputText.isNotEmpty()) {
                            IconButton(onClick = {
                                quickInputText = ""
                                error = null
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Xoá chuỗi nhanh")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(4.dp))

                // Paste from Clipboard Button
                TextButton(
                    onClick = {
                        val clipText = clipboardManager.getText()?.text
                        if (!clipText.isNullOrBlank()) {
                            quickInputText = clipText
                            val parsed = GpsCoordinateParser.parseSingleLineCoordinates(clipText)
                                ?: GpsCoordinateParser.parseCoordinates(clipText, "")
                            if (parsed != null) {
                                latStr = parsed.first.toString()
                                longStr = parsed.second.toString()
                                error = null
                            } else {
                                error = "Bộ nhớ tạm không chứa toạ độ hợp lệ"
                            }
                        } else {
                            error = "Bộ nhớ tạm rỗng"
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Dán từ bộ nhớ tạm")
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latStr,
                        onValueChange = { newLat ->
                            latStr = newLat
                            val parsed = GpsCoordinateParser.parseCoordinates(newLat, longStr)
                            if (parsed != null) {
                                error = null
                            } else if (newLat.isNotBlank() || longStr.isNotBlank()) {
                                error = "Toạ độ không hợp lệ (-90..90, -180..180)"
                            } else {
                                error = null
                            }
                        },
                        label = { Text("Vĩ độ (Lat)") },
                        placeholder = { Text("21.0285 / 21,0285") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = longStr,
                        onValueChange = { newLong ->
                            longStr = newLong
                            val parsed = GpsCoordinateParser.parseCoordinates(latStr, newLong)
                            if (parsed != null) {
                                error = null
                            } else if (latStr.isNotBlank() || newLong.isNotBlank()) {
                                error = "Toạ độ không hợp lệ (-90..90, -180..180)"
                            } else {
                                error = null
                            }
                        },
                        label = { Text("Kinh độ (Long)") },
                        placeholder = { Text("105.8542 / 105,8542") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (currentParsed != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Toạ độ hợp lệ: ${currentParsed.first}, ${currentParsed.second}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    keyboardController?.hide()
                    val parsed = GpsCoordinateParser.parseCoordinates(latStr, longStr)
                        ?: GpsCoordinateParser.parseSingleLineCoordinates(quickInputText)

                    if (parsed != null) {
                        onConfirm(parsed.first, parsed.second)
                    } else {
                        error = "Toạ độ không hợp lệ (-90..90, -180..180)"
                    }
                },
                enabled = currentParsed != null
            ) {
                Text("Áp dụng")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Huỷ")
            }
        }
    )
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chưa có ảnh nào",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Chọn 'Ảnh' hoặc 'Thư mục' bên dưới",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ImageGrid(
    images: List<GeoImage>,
    onRemove: (GeoImage) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(bottom = 88.dp, top = 8.dp, start = 8.dp, end = 8.dp), 
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(images, key = { it.id }) { image ->
            ImageItem(image = image, onRemove = onRemove)
        }
    }
}

@Composable
fun ImageItem(
    image: GeoImage,
    onRemove: (GeoImage) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            AsyncImage(
                model = image.uri,
                contentDescription = image.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onRemove(image) }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Xoá",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = image.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(12.dp),
                        tint = if (image.latitude != null) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (image.latitude != null && image.longitude != null) 
                             String.format(Locale.US, "%.4f, %.4f", image.latitude, image.longitude)
                        else "No GPS",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (image.latitude != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}