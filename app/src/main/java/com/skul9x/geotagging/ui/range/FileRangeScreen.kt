package com.skul9x.geotagging.ui.range

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skul9x.geotagging.data.model.FileOperationMode
import com.skul9x.geotagging.ui.range.components.DirectoryPickerCard
import com.skul9x.geotagging.ui.range.components.FileOperationProgressDialog
import com.skul9x.geotagging.ui.range.components.FilePreviewList
import com.skul9x.geotagging.ui.range.components.NewFolderOptionCard
import com.skul9x.geotagging.ui.range.components.OperationModeToggle
import com.skul9x.geotagging.ui.range.components.RangeSelectorCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileRangeScreen(
    modifier: Modifier = Modifier,
    viewModel: FileRangeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.events.collect { event ->
            when (event) {
                is FileRangeUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is FileRangeUiEvent.OperationFinished -> {
                    val msg = "Completed! Success: ${event.successCount}, Failures: ${event.failureCount}"
                    snackbarHostState.showSnackbar(msg)
                }
            }
        }
    }

    // SAF Document Tree pickers
    val sourceFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) {}
            viewModel.selectSourceDirectory(context, uri)
        }
    }

    val targetFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) {}
            viewModel.selectTargetDirectory(uri)
        }
    }

    val canExecute = !uiState.targetDirectoryUri.isNullOrEmpty() &&
            uiState.selectedFiles.isNotEmpty() &&
            !uiState.isOperating

    Scaffold(
        modifier = modifier.semantics { contentDescription = "File Range Screen" },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "File Range Manager",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select & Copy / Move Range",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Operation Progress & Summary Modal Dialog
            FileOperationProgressDialog(
                isOperating = uiState.isOperating,
                progress = uiState.progress,
                operationMode = uiState.operationMode,
                targetDirectoryUri = uiState.targetDirectoryUri,
                resultSummary = if (uiState.showResultModal) uiState.operationResult else null,
                onDismiss = { viewModel.dismissResultModal() },
                onOpenDestinationFolder = {
                    val uriStr = uiState.targetDirectoryUri
                    if (!uriStr.isNullOrEmpty()) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(android.net.Uri.parse(uriStr), "*/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 1. Directory Pickers
                DirectoryPickerCard(
                    sourceDirectoryUri = uiState.sourceDirectoryUri,
                    targetDirectoryUri = uiState.targetDirectoryUri,
                    onSelectSourceClick = { sourceFolderLauncher.launch(null) },
                    onSelectTargetClick = { targetFolderLauncher.launch(null) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Range Selector
                RangeSelectorCard(
                    startFileName = uiState.startFileName,
                    endFileName = uiState.endFileName,
                    sourceFiles = uiState.sourceFiles,
                    onStartFileSelected = { viewModel.setStartFileName(it) },
                    onEndFileSelected = { viewModel.setEndFileName(it) },
                    onQuickSelectAll = {
                        if (uiState.sourceFiles.isNotEmpty()) {
                            viewModel.setStartFileName(uiState.sourceFiles.first().name)
                            viewModel.setEndFileName(uiState.sourceFiles.last().name)
                        }
                    },
                    onClearRange = {
                        viewModel.setStartFileName("")
                        viewModel.setEndFileName("")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Operation Mode Toggle
                OperationModeToggle(
                    selectedMode = uiState.operationMode,
                    onModeSelected = { viewModel.setOperationMode(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. New Folder Option Card
                NewFolderOptionCard(
                    createSubfolder = uiState.createSubfolder,
                    subfolderName = uiState.subfolderName,
                    enterNewSubfolder = uiState.enterNewSubfolder,
                    onCreateSubfolderChanged = { viewModel.setCreateSubfolder(it) },
                    onSubfolderNameChanged = { viewModel.setSubfolderName(it) },
                    onEnterNewSubfolderChanged = { viewModel.setEnterNewSubfolder(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 5. File Preview List
                FilePreviewList(
                    sourceFiles = uiState.sourceFiles,
                    selectedFiles = uiState.selectedFiles,
                    totalSelectedSize = uiState.totalSelectedSize,
                    startFileName = uiState.startFileName,
                    endFileName = uiState.endFileName,
                    onFileSelectedAsStart = { viewModel.setStartFileName(it) },
                    onFileSelectedAsEnd = { viewModel.setEndFileName(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action / Execution Button
                Button(
                    onClick = { viewModel.startOperation(context) },
                    enabled = canExecute,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .semantics { contentDescription = "Execute ${uiState.operationMode.name} Button" },
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.operationMode == FileOperationMode.COPY)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    if (uiState.isOperating) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Executing ${uiState.operationMode.name}...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Execute ${uiState.operationMode.name} (${uiState.selectedCount} files)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
