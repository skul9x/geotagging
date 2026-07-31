package com.skul9x.geotagging.ui.range

import com.skul9x.geotagging.data.model.FileItem
import com.skul9x.geotagging.data.model.FileOperationMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FileRangeViewModelTest {

    private lateinit var viewModel: FileRangeViewModel

    @Before
    fun setUp() {
        viewModel = FileRangeViewModel()
    }

    @Test
    fun testSelectSourceDirectoryLoadsAndSortsFiles() {
        val unsortedFiles = listOf(
            FileItem(uri = "uri/abc10", name = "abc10.txt", size = 100L),
            FileItem(uri = "uri/abc2", name = "abc2.txt", size = 200L),
            FileItem(uri = "uri/abc1", name = "abc1.txt", size = 300L)
        )

        viewModel.setSourceFiles(unsortedFiles, "content://source/tree")

        val state = viewModel.uiState.value
        assertEquals("content://source/tree", state.sourceDirectoryUri)
        assertEquals(3, state.sourceFiles.size)
        assertEquals("abc1.txt", state.sourceFiles[0].name)
        assertEquals("abc2.txt", state.sourceFiles[1].name)
        assertEquals("abc10.txt", state.sourceFiles[2].name)
    }

    @Test
    fun testRangeSelectionUpdatesSelectedFileList() {
        val files = listOf(
            FileItem(uri = "uri/abc1", name = "abc1", size = 100L),
            FileItem(uri = "uri/abc2", name = "abc2", size = 200L),
            FileItem(uri = "uri/abc3", name = "abc3", size = 300L),
            FileItem(uri = "uri/abc4", name = "abc4", size = 400L),
            FileItem(uri = "uri/abc5", name = "abc5", size = 500L)
        )

        viewModel.setSourceFiles(files)
        viewModel.setStartFileName("abc1")
        viewModel.setEndFileName("abc4")

        val state = viewModel.uiState.value
        assertEquals(4, state.selectedCount)
        assertEquals(4, state.selectedFiles.size)
        assertEquals(100L + 200L + 300L + 400L, state.totalSelectedSize)
        assertEquals(listOf("abc1", "abc2", "abc3", "abc4"), state.selectedFiles.map { it.name })
    }

    @Test
    fun testOperationModeToggle() {
        assertEquals(FileOperationMode.COPY, viewModel.uiState.value.operationMode)

        viewModel.setOperationMode(FileOperationMode.MOVE)
        assertEquals(FileOperationMode.MOVE, viewModel.uiState.value.operationMode)

        viewModel.setOperationMode(FileOperationMode.COPY)
        assertEquals(FileOperationMode.COPY, viewModel.uiState.value.operationMode)
    }

    @Test
    fun testNewSubfolderOptionState() {
        viewModel.setTargetDirectoryUri("content://target/tree")
        viewModel.setCreateSubfolder(true)
        viewModel.setSubfolderName("NewBatch")
        viewModel.setEnterNewSubfolder(true)

        val state = viewModel.uiState.value
        assertTrue(state.createSubfolder)
        assertEquals("NewBatch", state.subfolderName)
        assertTrue(state.enterNewSubfolder)
        assertEquals("content://target/tree", state.targetDirectoryUri)
    }
}
