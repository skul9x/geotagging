package com.skul9x.geotagging.integration

import com.skul9x.geotagging.data.model.FileOperationMode
import com.skul9x.geotagging.ui.range.FileRangeViewModel
import com.skul9x.geotagging.utils.FileOperationsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class FileRangeE2EIntegrationTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: FileRangeViewModel
    private lateinit var sourceDir: File
    private lateinit var targetDir: File

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FileRangeViewModel()

        sourceDir = tempFolder.newFolder("source_files")
        targetDir = tempFolder.newFolder("target_files")

        // Create 10 temporary files abc1.txt .. abc10.txt
        for (i in 1..10) {
            val file = File(sourceDir, "abc$i.txt")
            file.writeText("Content of abc$i")
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun runOperationAndWait(viewModel: FileRangeViewModel) {
        viewModel.startOperation(null)
        testDispatcher.scheduler.advanceUntilIdle()

        var attempts = 0
        while (viewModel.uiState.value.operationResult == null && attempts < 100) {
            Thread.sleep(50)
            testDispatcher.scheduler.advanceUntilIdle()
            attempts++
        }
    }

    @Test
    fun testEndToEndRangeCopy() = runTest(testDispatcher) {
        val sourcePath = sourceDir.absolutePath
        val targetPath = targetDir.absolutePath

        viewModel.selectSourceDirectoryPath(sourcePath)
        viewModel.selectTargetDirectoryPath(targetPath)

        // Select range abc1.txt to abc4.txt
        viewModel.setStartFileName("abc1.txt")
        viewModel.setEndFileName("abc4.txt")
        viewModel.setOperationMode(FileOperationMode.COPY)

        assertEquals(4, viewModel.uiState.value.selectedCount)

        runOperationAndWait(viewModel)

        val destFiles = targetDir.listFiles() ?: emptyArray()
        val destFileNames = destFiles.map { it.name }.sorted()
        assertEquals("Expected 4 files in dest but got ${destFiles.size}: $destFileNames", 4, destFiles.size)
        assertEquals(listOf("abc1.txt", "abc2.txt", "abc3.txt", "abc4.txt"), destFileNames)

        val srcFiles = sourceDir.listFiles() ?: emptyArray()
        assertEquals(10, srcFiles.size)
    }

    @Test
    fun testEndToEndRangeMove() = runTest(testDispatcher) {
        val sourcePath = sourceDir.absolutePath
        val targetPath = targetDir.absolutePath

        viewModel.selectSourceDirectoryPath(sourcePath)
        viewModel.selectTargetDirectoryPath(targetPath)

        // Select range abc1.txt to abc4.txt
        viewModel.setStartFileName("abc1.txt")
        viewModel.setEndFileName("abc4.txt")
        viewModel.setOperationMode(FileOperationMode.MOVE)

        assertEquals(4, viewModel.uiState.value.selectedCount)

        runOperationAndWait(viewModel)

        val destFiles = targetDir.listFiles() ?: emptyArray()
        val destFileNames = destFiles.map { it.name }.sorted()
        assertEquals("Expected 4 files in dest but got ${destFiles.size}: $destFileNames", 4, destFiles.size)
        assertEquals(listOf("abc1.txt", "abc2.txt", "abc3.txt", "abc4.txt"), destFileNames)

        val srcFiles = sourceDir.listFiles() ?: emptyArray()
        assertEquals(6, srcFiles.size)
        val remainingSrcNames = srcFiles.map { it.name }.sorted()
        assertFalse(remainingSrcNames.contains("abc1.txt"))
        assertFalse(remainingSrcNames.contains("abc4.txt"))
        assertTrue(remainingSrcNames.contains("abc5.txt"))
        assertTrue(remainingSrcNames.contains("abc10.txt"))
    }

    @Test
    fun testEndToEndMoveIntoNewSubfolder() = runTest(testDispatcher) {
        val sourcePath = sourceDir.absolutePath
        val targetPath = targetDir.absolutePath

        viewModel.selectSourceDirectoryPath(sourcePath)
        viewModel.selectTargetDirectoryPath(targetPath)

        viewModel.setStartFileName("abc1.txt")
        viewModel.setEndFileName("abc4.txt")
        viewModel.setOperationMode(FileOperationMode.MOVE)
        viewModel.setCreateSubfolder(true)
        viewModel.setSubfolderName("Batch_01")
        viewModel.setEnterNewSubfolder(true)

        assertEquals(4, viewModel.uiState.value.selectedCount)

        runOperationAndWait(viewModel)

        val subFolder = File(targetDir, "Batch_01")
        assertTrue("Subfolder Batch_01 should exist", subFolder.exists() && subFolder.isDirectory)

        val subfolderFiles = subFolder.listFiles() ?: emptyArray()
        val subfolderFileNames = subfolderFiles.map { it.name }.sorted()
        assertEquals("Expected 4 files in subfolder but got ${subfolderFiles.size}: $subfolderFileNames", 4, subfolderFiles.size)
        assertEquals(listOf("abc1.txt", "abc2.txt", "abc3.txt", "abc4.txt"), subfolderFileNames)
    }

    @Test
    fun testBuildVerification() {
        val result = FileOperationsHelper.queryFilesInPath(sourceDir.absolutePath)
        assertEquals(10, result.size)
    }
}
