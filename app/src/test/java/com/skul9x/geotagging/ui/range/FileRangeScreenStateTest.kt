package com.skul9x.geotagging.ui.range

import com.skul9x.geotagging.data.model.FileItem
import com.skul9x.geotagging.data.model.FileOperationMode
import com.skul9x.geotagging.ui.home.HomeViewModel
import com.skul9x.geotagging.ui.navigation.MainTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FileRangeScreenStateTest {

    private lateinit var viewModel: FileRangeViewModel

    @Before
    fun setUp() {
        viewModel = FileRangeViewModel()
    }

    @Test
    fun testUiStateRenderingDefaults() {
        val state = viewModel.uiState.value

        // Verify default state renders empty directory placeholders
        assertNull(state.sourceDirectoryUri)
        assertNull(state.targetDirectoryUri)
        assertTrue(state.sourceFiles.isEmpty())
        assertTrue(state.startFileName.isEmpty())
        assertTrue(state.endFileName.isEmpty())
        assertTrue(state.selectedFiles.isEmpty())
        assertEquals(0, state.selectedCount)
        assertEquals(0L, state.totalSelectedSize)
        assertEquals(FileOperationMode.COPY, state.operationMode)
        assertFalse(state.createSubfolder)
        assertTrue(state.subfolderName.isEmpty())
        assertFalse(state.enterNewSubfolder)
        assertFalse(state.isOperating)

        // Verify conditions required to enable execute button
        val canExecuteDefault = !state.targetDirectoryUri.isNullOrEmpty() &&
                state.selectedFiles.isNotEmpty() &&
                !state.isOperating
        assertFalse("Execute button must be disabled by default", canExecuteDefault)
    }

    @Test
    fun testRangeSelectionHighlighting() {
        val files = listOf(
            FileItem(uri = "content://1", name = "IMG_0001.jpg", size = 1000L),
            FileItem(uri = "content://2", name = "IMG_0002.jpg", size = 2000L),
            FileItem(uri = "content://3", name = "IMG_0003.jpg", size = 1500L),
            FileItem(uri = "content://4", name = "IMG_0004.jpg", size = 3000L),
            FileItem(uri = "content://5", name = "IMG_0005.jpg", size = 2500L)
        )

        viewModel.setSourceFiles(files, "content://source/folder")
        viewModel.setStartFileName("IMG_0002.jpg")
        viewModel.setEndFileName("IMG_0004.jpg")

        val state = viewModel.uiState.value
        val selectedNames = state.selectedFiles.map { it.name }

        // Verify files falling within [StartFile, EndFile] are correctly filtered and highlighted
        assertEquals(3, state.selectedCount)
        assertEquals(listOf("IMG_0002.jpg", "IMG_0003.jpg", "IMG_0004.jpg"), selectedNames)
        assertEquals(6500L, state.totalSelectedSize)

        // Check each source file selection boundary
        assertTrue("IMG_0002.jpg should be selected", selectedNames.contains("IMG_0002.jpg"))
        assertTrue("IMG_0003.jpg should be selected", selectedNames.contains("IMG_0003.jpg"))
        assertTrue("IMG_0004.jpg should be selected", selectedNames.contains("IMG_0004.jpg"))
        assertFalse("IMG_0001.jpg should NOT be selected", selectedNames.contains("IMG_0001.jpg"))
        assertFalse("IMG_0005.jpg should NOT be selected", selectedNames.contains("IMG_0005.jpg"))
    }

    @Test
    fun testTabNavigationSwitching() {
        val rangeViewModel = FileRangeViewModel()

        // Mutate state in range viewmodel
        val files = listOf(
            FileItem(uri = "content://1", name = "DSC_001.jpg", size = 500L),
            FileItem(uri = "content://2", name = "DSC_002.jpg", size = 800L)
        )
        rangeViewModel.setSourceFiles(files, "content://source")
        rangeViewModel.setStartFileName("DSC_001.jpg")
        rangeViewModel.setEndFileName("DSC_002.jpg")
        rangeViewModel.setOperationMode(FileOperationMode.MOVE)

        // Switch active tab index from 0 to 1 and back to 0
        var activeTab = MainTab.BATCH_GEOTAG
        assertEquals("Batch Geotag", activeTab.title)

        // Switch to File Range tab
        activeTab = MainTab.FILE_RANGE
        assertEquals("File Range", activeTab.title)

        // Verify rangeViewModel state is preserved
        val rangeState = rangeViewModel.uiState.value
        assertEquals("content://source", rangeState.sourceDirectoryUri)
        assertEquals("DSC_001.jpg", rangeState.startFileName)
        assertEquals("DSC_002.jpg", rangeState.endFileName)
        assertEquals(FileOperationMode.MOVE, rangeState.operationMode)
        assertEquals(2, rangeState.selectedCount)

        // Switch back to Batch Geotag tab
        activeTab = MainTab.BATCH_GEOTAG
        assertEquals("Batch Geotag", activeTab.title)

        // Re-check rangeViewModel state after tab navigation roundtrip
        val rangeStateAfter = rangeViewModel.uiState.value
        assertEquals("content://source", rangeStateAfter.sourceDirectoryUri)
        assertEquals(2, rangeStateAfter.selectedCount)
    }
}
