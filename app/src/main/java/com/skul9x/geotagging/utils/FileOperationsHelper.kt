package com.skul9x.geotagging.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import com.skul9x.geotagging.data.model.FileItem
import com.skul9x.geotagging.data.model.FileOperationMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream

data class OperationProgress(
    val processedFiles: Int = 0,
    val totalFiles: Int = 0,
    val processedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val progressFraction: Float = 0f,
    val currentFileName: String = "",
    val isCompleted: Boolean = false,
    val errorMsg: String? = null
)

data class FileOperationResult(
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val errors: List<String> = emptyList()
)

object FileOperationsHelper {

    private val comparator = NaturalOrderComparator()

    private fun getCleanPath(rawPath: String): String {
        var p = rawPath
        if (p.startsWith("file://")) {
            p = p.substring(7)
        } else if (p.startsWith("file:")) {
            p = p.substring(5)
        }
        return p
    }

    /**
     * Reads all files in a local folder path.
     */
    fun queryFilesInPath(path: String): List<FileItem> {
        val files = mutableListOf<FileItem>()
        val cleanPath = getCleanPath(path)
        val dir = File(cleanPath)
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.filter { it.isFile }?.forEach { f ->
                files.add(
                    FileItem(
                        uri = "file://${f.absolutePath}",
                        name = f.name,
                        size = f.length(),
                        mimeType = "text/plain",
                        lastModified = f.lastModified(),
                        parentUri = "file://${dir.absolutePath}"
                    )
                )
            }
        }
        return files.sortedWith { f1, f2 -> comparator.compare(f1.name, f2.name) }
    }

    /**
     * Reads all files in a tree directory URI using SAF or local filesystem fallback.
     */
    fun queryFilesInDirectory(context: Context?, treeUri: Uri?): List<FileItem> {
        if (treeUri == null) {
            return emptyList()
        }

        val uriStr = try { treeUri.toString() } catch (_: Exception) { "" }
        if (context == null || uriStr.startsWith("file:") || treeUri.scheme == "file") {
            val path = try { treeUri.path ?: uriStr } catch (_: Exception) { uriStr }
            return queryFilesInPath(path)
        }

        val files = mutableListOf<FileItem>()
        try {
            val contentResolver = context.contentResolver
            val docId = DocumentsContract.getTreeDocumentId(treeUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)

            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            )

            contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
                val mimeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val dateIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                while (cursor.moveToNext()) {
                    val childDocId = cursor.getString(idIndex)
                    val mimeType = if (mimeIndex != -1 && !cursor.isNull(mimeIndex)) cursor.getString(mimeIndex) else ""

                    if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                        continue
                    }

                    val name = if (nameIndex != -1 && !cursor.isNull(nameIndex)) cursor.getString(nameIndex) else childDocId
                    val size = if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else 0L
                    val lastModified = if (dateIndex != -1 && !cursor.isNull(dateIndex)) cursor.getLong(dateIndex) else 0L

                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childDocId)

                    files.add(
                        FileItem(
                            uri = fileUri.toString(),
                            name = name,
                            size = size,
                            mimeType = mimeType,
                            lastModified = lastModified,
                            parentUri = treeUri.toString()
                        )
                    )
                }
            }
        } catch (_: Exception) {
            val path = try { treeUri.path ?: "" } catch (_: Exception) { "" }
            return queryFilesInPath(path)
        }

        return files.sortedWith { f1, f2 -> comparator.compare(f1.name, f2.name) }
    }

    /**
     * Creates a new subfolder in the target directory path or SAF tree URI.
     */
    fun createSubfolderPath(targetPath: String, folderName: String): String? {
        val cleanPath = getCleanPath(targetPath)
        val targetDir = File(cleanPath)
        val subFolder = File(targetDir, folderName)
        if (subFolder.exists() || subFolder.mkdirs()) {
            return "file://${subFolder.absolutePath}"
        }
        return null
    }

    fun createSubfolder(context: Context?, targetDirUri: Uri?, folderName: String): Uri? {
        if (targetDirUri == null) return null

        val uriStr = try { targetDirUri.toString() } catch (_: Exception) { "" }
        if (context == null || uriStr.startsWith("file:") || targetDirUri.scheme == "file") {
            val path = try { targetDirUri.path ?: uriStr } catch (_: Exception) { uriStr }
            val created = createSubfolderPath(path, folderName)
            return if (created != null) try { Uri.parse(created) } catch (_: Exception) { null } else null
        }

        return try {
            val contentResolver = context.contentResolver
            val docId = DocumentsContract.getTreeDocumentId(targetDirUri)
            val parentDocUri = DocumentsContract.buildDocumentUriUsingTree(targetDirUri, docId)
            DocumentsContract.createDocument(
                contentResolver,
                parentDocUri,
                DocumentsContract.Document.MIME_TYPE_DIR,
                folderName
            )
        } catch (_: Exception) {
            val path = try { targetDirUri.path ?: "" } catch (_: Exception) { "" }
            val created = createSubfolderPath(path, folderName)
            if (created != null) try { Uri.parse(created) } catch (_: Exception) { null } else null
        }
    }

    /**
     * Copies stream contents from [input] to [output], invoking [onBytesCopied] periodically.
     */
    fun copyStream(
        input: InputStream,
        output: OutputStream,
        bufferSize: Int = 8192,
        onBytesCopied: ((Long) -> Unit)? = null
    ): Long {
        var totalBytesRead = 0L
        val buffer = ByteArray(bufferSize)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
            totalBytesRead += bytesRead
            onBytesCopied?.invoke(totalBytesRead)
        }
        output.flush()
        return totalBytesRead
    }

    /**
     * Performs asynchronous file copy/move operations across SAF URIs or local filesystem.
     */
    suspend fun executeOperation(
        context: Context?,
        files: List<FileItem>,
        targetDirUri: Uri?,
        mode: FileOperationMode,
        onProgress: ((OperationProgress) -> Unit)? = null
    ): FileOperationResult = withContext(Dispatchers.IO) {
        val targetPathStr = if (targetDirUri != null) {
            try { targetDirUri.toString() } catch (_: Exception) { "" }
        } else ""

        executeOperationPath(context, files, targetPathStr, mode, onProgress)
    }

    suspend fun executeOperationPath(
        context: Context?,
        files: List<FileItem>,
        targetPathStr: String,
        mode: FileOperationMode,
        onProgress: ((OperationProgress) -> Unit)? = null
    ): FileOperationResult = withContext(Dispatchers.IO) {
        val totalFiles = files.size
        val totalBytes = files.sumOf { it.size }
        var processedFiles = 0
        var processedBytes = 0L
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()

        val isLocalMode = context == null || targetPathStr.startsWith("file:") || (files.isNotEmpty() && files.first().uri.startsWith("file:"))

        if (isLocalMode) {
            val cleanTargetPath = getCleanPath(targetPathStr)
            val targetDirFile = File(cleanTargetPath)
            if (!targetDirFile.exists()) {
                targetDirFile.mkdirs()
            }

            for (file in files) {
                val currentFileName = file.name
                onProgress?.invoke(
                    OperationProgress(
                        processedFiles = processedFiles,
                        totalFiles = totalFiles,
                        processedBytes = processedBytes,
                        totalBytes = totalBytes,
                        progressFraction = if (totalFiles > 0) processedFiles.toFloat() / totalFiles else 0f,
                        currentFileName = currentFileName
                    )
                )

                try {
                    val srcFilePath = getCleanPath(file.uri)
                    val srcFile = File(srcFilePath)
                    val destFile = File(targetDirFile, file.name)

                    srcFile.inputStream().use { input ->
                        destFile.outputStream().use { output ->
                            copyStream(input, output) { deltaBytes ->
                                val currentTotalProcessed = processedBytes + deltaBytes
                                val byteFraction = if (totalBytes > 0) currentTotalProcessed.toFloat() / totalBytes else 0f
                                onProgress?.invoke(
                                    OperationProgress(
                                        processedFiles = processedFiles,
                                        totalFiles = totalFiles,
                                        processedBytes = currentTotalProcessed,
                                        totalBytes = totalBytes,
                                        progressFraction = byteFraction,
                                        currentFileName = currentFileName
                                    )
                                )
                            }
                        }
                    }

                    if (mode == FileOperationMode.MOVE) {
                        srcFile.delete()
                    }

                    processedFiles++
                    processedBytes += file.size
                    successCount++

                    onProgress?.invoke(
                        OperationProgress(
                            processedFiles = processedFiles,
                            totalFiles = totalFiles,
                            processedBytes = processedBytes,
                            totalBytes = totalBytes,
                            progressFraction = if (totalFiles > 0) processedFiles.toFloat() / totalFiles else 1.0f,
                            currentFileName = currentFileName
                        )
                    )
                } catch (e: Exception) {
                    failureCount++
                    val err = "Error processing ${file.name}: ${e.message}"
                    errors.add(err)
                    processedFiles++
                    onProgress?.invoke(
                        OperationProgress(
                            processedFiles = processedFiles,
                            totalFiles = totalFiles,
                            processedBytes = processedBytes,
                            totalBytes = totalBytes,
                            progressFraction = if (totalFiles > 0) processedFiles.toFloat() / totalFiles else 1.0f,
                            currentFileName = currentFileName,
                            errorMsg = err
                        )
                    )
                }
            }

            onProgress?.invoke(
                OperationProgress(
                    processedFiles = totalFiles,
                    totalFiles = totalFiles,
                    processedBytes = totalBytes,
                    totalBytes = totalBytes,
                    progressFraction = 1.0f,
                    currentFileName = "",
                    isCompleted = true
                )
            )

            return@withContext FileOperationResult(
                successCount = successCount,
                failureCount = failureCount,
                errors = errors
            )
        }

        // SAF mode
        val targetDirUri = Uri.parse(targetPathStr)
        val contentResolver = context!!.contentResolver
        val targetDocId = DocumentsContract.getTreeDocumentId(targetDirUri)
        val targetParentUri = DocumentsContract.buildDocumentUriUsingTree(targetDirUri, targetDocId)

        for (file in files) {
            val fileUri = Uri.parse(file.uri)
            val currentFileName = file.name

            onProgress?.invoke(
                OperationProgress(
                    processedFiles = processedFiles,
                    totalFiles = totalFiles,
                    processedBytes = processedBytes,
                    totalBytes = totalBytes,
                    progressFraction = if (totalFiles > 0) processedFiles.toFloat() / totalFiles else 0f,
                    currentFileName = currentFileName
                )
            )

            try {
                var nativeHandled = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (mode == FileOperationMode.COPY) {
                        try {
                            val resultUri = DocumentsContract.copyDocument(contentResolver, fileUri, targetParentUri)
                            if (resultUri != null) {
                                nativeHandled = true
                            }
                        } catch (_: Exception) {}
                    } else if (mode == FileOperationMode.MOVE && file.parentUri.isNotEmpty()) {
                        try {
                            val sourceParentUri = DocumentsContract.buildDocumentUriUsingTree(
                                Uri.parse(file.parentUri),
                                DocumentsContract.getTreeDocumentId(Uri.parse(file.parentUri))
                            )
                            val resultUri = DocumentsContract.moveDocument(
                                contentResolver,
                                fileUri,
                                sourceParentUri,
                                targetParentUri
                            )
                            if (resultUri != null) {
                                nativeHandled = true
                            }
                        } catch (_: Exception) {}
                    }
                }

                if (!nativeHandled) {
                    val mimeType = file.mimeType.ifEmpty { "application/octet-stream" }
                    val newTargetDocUri = DocumentsContract.createDocument(
                        contentResolver,
                        targetParentUri,
                        mimeType,
                        file.name
                    ) ?: throw IllegalStateException("Could not create target document for ${file.name}")

                    contentResolver.openInputStream(fileUri).use { inputStream ->
                        contentResolver.openOutputStream(newTargetDocUri).use { outputStream ->
                            if (inputStream != null && outputStream != null) {
                                copyStream(inputStream, outputStream) { deltaBytes ->
                                    val currentTotalProcessed = processedBytes + deltaBytes
                                    val byteFraction = if (totalBytes > 0) currentTotalProcessed.toFloat() / totalBytes else 0f
                                    onProgress?.invoke(
                                        OperationProgress(
                                            processedFiles = processedFiles,
                                            totalFiles = totalFiles,
                                            processedBytes = currentTotalProcessed,
                                            totalBytes = totalBytes,
                                            progressFraction = byteFraction,
                                            currentFileName = currentFileName
                                        )
                                    )
                                }
                            } else {
                                throw IllegalStateException("Failed to open streams for ${file.name}")
                            }
                        }
                    }

                    if (mode == FileOperationMode.MOVE) {
                        DocumentsContract.deleteDocument(contentResolver, fileUri)
                    }
                }

                processedFiles++
                processedBytes += file.size
                successCount++

                onProgress?.invoke(
                    OperationProgress(
                        processedFiles = processedFiles,
                        totalFiles = totalFiles,
                        processedBytes = processedBytes,
                        totalBytes = totalBytes,
                        progressFraction = if (totalFiles > 0) processedFiles.toFloat() / totalFiles else 1.0f,
                        currentFileName = currentFileName
                    )
                )

            } catch (e: Exception) {
                failureCount++
                val err = "Error processing ${file.name}: ${e.message}"
                errors.add(err)
                processedFiles++
                onProgress?.invoke(
                    OperationProgress(
                        processedFiles = processedFiles,
                        totalFiles = totalFiles,
                        processedBytes = processedBytes,
                        totalBytes = totalBytes,
                        progressFraction = if (totalFiles > 0) processedFiles.toFloat() / totalFiles else 1.0f,
                        currentFileName = currentFileName,
                        errorMsg = err
                    )
                )
            }
        }

        onProgress?.invoke(
            OperationProgress(
                processedFiles = totalFiles,
                totalFiles = totalFiles,
                processedBytes = totalBytes,
                totalBytes = totalBytes,
                progressFraction = 1.0f,
                currentFileName = "",
                isCompleted = true
            )
        )

        FileOperationResult(
            successCount = successCount,
            failureCount = failureCount,
            errors = errors
        )
    }
}
