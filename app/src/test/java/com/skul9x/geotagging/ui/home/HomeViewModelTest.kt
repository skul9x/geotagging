package com.skul9x.geotagging.ui.home

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.skul9x.geotagging.data.model.GeoImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var application: Application
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        application = mock(Application::class.java)
        context = mock(Context::class.java)
        contentResolver = mock(ContentResolver::class.java)

        `when`(application.applicationContext).thenReturn(context)
        `when`(context.contentResolver).thenReturn(contentResolver)

        viewModel = HomeViewModel(application, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_updateLocation_permissionDenied_emitsPermissionEventOrSnackbar() = runTest {
        val sampleUri = mock(Uri::class.java)
        `when`(sampleUri.toString()).thenReturn("content://test/123")

        `when`(contentResolver.openFileDescriptor(eq(sampleUri), eq("rw")))
            .thenThrow(SecurityException("Permission denied test"))

        viewModel.addImages(listOf(sampleUri))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.images.size)

        viewModel.updateLocationForImages(21.0285, 105.8542)
        advanceUntilIdle()

        val event = viewModel.uiEvent.first()
        assertTrue(
            "Expected event to be ShowSnackbar or RequestWritePermission, but was: $event",
            event is HomeUiEvent.ShowSnackbar || event is HomeUiEvent.RequestWritePermission
        )
    }

    @Test
    fun test_loadFolder_requestsWritePermissions() = runTest {
        val mockTreeUri = mock(Uri::class.java)

        viewModel.loadImagesFromFolder(mockTreeUri)
        advanceUntilIdle()

        val expectedFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        verify(contentResolver).takePersistableUriPermission(eq(mockTreeUri), eq(expectedFlags))
    }

    @Test
    fun test_removeImage_updatesStateCorrectly() = runTest {
        val sampleUri = mock(Uri::class.java)
        `when`(sampleUri.toString()).thenReturn("content://test/456")

        viewModel.addImages(listOf(sampleUri))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.images.size)
        val imageToRemove = viewModel.uiState.value.images.first()

        viewModel.removeImage(imageToRemove)
        assertEquals(0, viewModel.uiState.value.images.size)
        assertEquals(0, viewModel.uiState.value.imageCount)
    }

    @Test
    fun test_clearAllImages_clearsListAndEmitsEvent() = runTest {
        val sampleUri = mock(Uri::class.java)
        `when`(sampleUri.toString()).thenReturn("content://test/789")

        viewModel.addImages(listOf(sampleUri))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.images.size)

        viewModel.clearAllImages()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.images.size)
        val event = viewModel.uiEvent.first()
        assertTrue(event is HomeUiEvent.ShowSnackbar)
        assertEquals("Đã xoá danh sách", (event as HomeUiEvent.ShowSnackbar).message)
    }

    @Test
    fun test_showEditDialog_updatesState() {
        viewModel.showEditDialog(true)
        assertTrue(viewModel.uiState.value.showEditDialog)

        viewModel.showEditDialog(false)
        org.junit.Assert.assertFalse(viewModel.uiState.value.showEditDialog)
    }
}
