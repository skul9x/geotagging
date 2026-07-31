package com.skul9x.geotagging.ui.range

import com.skul9x.geotagging.utils.FileOperationsHelper
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

class FileOperationsHelperTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testLocalFileCopyAndMoveMock() {
        val sourceContent = "Hello, Geotagging File Range Manager!".toByteArray()
        val inputStream = ByteArrayInputStream(sourceContent)
        val outputStream = ByteArrayOutputStream()

        val bytesCopied = FileOperationsHelper.copyStream(inputStream, outputStream)

        assertEquals(sourceContent.size.toLong(), bytesCopied)
        assertArrayEquals(sourceContent, outputStream.toByteArray())
    }

    @Test
    fun testLocalFileStreamCopyOnDisk() {
        val sourceFile = tempFolder.newFile("source_test.txt")
        val targetFile = File(tempFolder.root, "target_test.txt")

        val fileData = "Testing stream copy on actual file system".toByteArray()
        sourceFile.writeBytes(fileData)

        sourceFile.inputStream().use { input ->
            targetFile.outputStream().use { output ->
                FileOperationsHelper.copyStream(input, output)
            }
        }

        assertTrue(targetFile.exists())
        assertEquals(fileData.size.toLong(), targetFile.length())
        assertArrayEquals(fileData, targetFile.readBytes())
    }

    @Test
    fun testProgressCallbackDispatch() {
        val totalDataSize = 32 * 1024 // 32 KB
        val sourceData = ByteArray(totalDataSize) { (it % 256).toByte() }
        val inputStream = ByteArrayInputStream(sourceData)
        val outputStream = ByteArrayOutputStream()

        val capturedProgresses = mutableListOf<Long>()
        FileOperationsHelper.copyStream(inputStream, outputStream, bufferSize = 8192) { bytesCopied ->
            capturedProgresses.add(bytesCopied)
        }

        assertTrue(capturedProgresses.isNotEmpty())
        assertEquals(totalDataSize.toLong(), capturedProgresses.last())

        val totalFiles = 10
        val progressReports = mutableListOf<Float>()
        for (i in 0..totalFiles) {
            val fraction = if (totalFiles > 0) i.toFloat() / totalFiles else 0f
            progressReports.add(fraction)
        }

        assertEquals(11, progressReports.size)
        assertEquals(0.0f, progressReports.first(), 0.001f)
        assertEquals(1.0f, progressReports.last(), 0.001f)
    }
}
