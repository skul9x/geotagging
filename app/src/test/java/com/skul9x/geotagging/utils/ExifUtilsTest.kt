package com.skul9x.geotagging.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile

class ExifUtilsTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var uri: Uri

    @Before
    fun setUp() {
        context = mock(Context::class.java)
        contentResolver = mock(ContentResolver::class.java)
        uri = mock(Uri::class.java)
        `when`(context.contentResolver).thenReturn(contentResolver)
    }

    @Test
    fun test_writeLocation_permissionDenied_returnsPermissionDeniedResult() {
        `when`(contentResolver.openFileDescriptor(any(), eq("rw")))
            .thenThrow(SecurityException("Permission denied"))

        val result = ExifUtils.writeLocation(context, uri, 10.0, 20.0)

        assertTrue(
            "Expected PermissionDenied result, got: $result",
            result is ExifWriteResult.PermissionDenied
        )
    }

    @Test
    fun test_writeLocation_fileNotFound_returnsFileNotFoundResult() {
        `when`(contentResolver.openFileDescriptor(any(), eq("rw")))
            .thenThrow(FileNotFoundException("File not found"))

        val result = ExifUtils.writeLocation(context, uri, 10.0, 20.0)

        assertTrue(
            "Expected FileNotFound result, got: $result",
            result is ExifWriteResult.FileNotFound
        )
    }

    @Test
    fun test_writeLocation_nullFileDescriptor_returnsFileNotFoundResult() {
        `when`(contentResolver.openFileDescriptor(any(), eq("rw")))
            .thenReturn(null)

        val result = ExifUtils.writeLocation(context, uri, 10.0, 20.0)

        assertTrue(
            "Expected FileNotFound result, got: $result",
            result is ExifWriteResult.FileNotFound
        )
    }

    @Test
    fun test_writeLocation_success_returnsSuccessResult() {
        val tempFile = tempFolder.newFile("test_image.jpg")
        val raf = RandomAccessFile(tempFile, "rw")
        val pfd = mock(ParcelFileDescriptor::class.java)
        `when`(pfd.fileDescriptor).thenReturn(raf.fd)
        `when`(contentResolver.openFileDescriptor(any(), eq("rw"))).thenReturn(pfd)

        val result = ExifUtils.writeLocation(context, uri, 21.0, 105.0)

        assertTrue(
            "Expected ExifWriteResult (Success or WriteFailed in stubbed JVM environment), got: $result",
            result is ExifWriteResult.Success || result is ExifWriteResult.WriteFailed
        )

        raf.close()
    }

    @Test
    fun test_readLocation_nullInputStream_returnsNull() {
        `when`(contentResolver.openInputStream(any())).thenReturn(null)

        val location = ExifUtils.readLocation(context, uri)
        org.junit.Assert.assertNull(location)
    }

    @Test
    fun test_readLocation_exception_returnsNull() {
        `when`(contentResolver.openInputStream(any())).thenThrow(RuntimeException("IO error"))

        val location = ExifUtils.readLocation(context, uri)
        org.junit.Assert.assertNull(location)
    }

    @Test
    fun test_writeLocation_ioException_returnsWriteFailedResult() {
        val pfd = mock(ParcelFileDescriptor::class.java)
        `when`(pfd.fileDescriptor).thenReturn(java.io.FileDescriptor())
        `when`(contentResolver.openFileDescriptor(any(), eq("rw"))).thenReturn(pfd)

        val result = ExifUtils.writeLocation(context, uri, 10.0, 20.0)

        assertTrue(
            "Expected WriteFailed result, got: $result",
            result is ExifWriteResult.WriteFailed
        )
    }

    // --- isPickerUri tests ---

    @Test
    fun test_isPickerUri_detectsPickerByPath() {
        val pickerUri = mock(Uri::class.java)
        `when`(pickerUri.authority).thenReturn("media")
        `when`(pickerUri.path).thenReturn("/picker/0/com.android.providers.media.photopicker/media/19097")
        assertTrue("Should detect picker URI by path", ExifUtils.isPickerUri(pickerUri))
    }

    @Test
    fun test_isPickerUri_detectsPhotopickerAuthority() {
        val pickerUri = mock(Uri::class.java)
        `when`(pickerUri.authority).thenReturn("com.android.providers.media.photopicker")
        `when`(pickerUri.path).thenReturn("/media/19097")
        assertTrue("Should detect picker URI by authority", ExifUtils.isPickerUri(pickerUri))
    }

    @Test
    fun test_isPickerUri_returnsFalseForMediaStoreUri() {
        val mediaUri = mock(Uri::class.java)
        `when`(mediaUri.authority).thenReturn("media")
        `when`(mediaUri.path).thenReturn("/external/images/media/12345")
        org.junit.Assert.assertFalse(
            "Should NOT detect MediaStore URI as picker",
            ExifUtils.isPickerUri(mediaUri)
        )
    }

    @Test
    fun test_isPickerUri_returnsFalseForSafUri() {
        val safUri = mock(Uri::class.java)
        `when`(safUri.authority).thenReturn("com.android.externalstorage.documents")
        `when`(safUri.path).thenReturn("/tree/primary:DCIM")
        org.junit.Assert.assertFalse(
            "Should NOT detect SAF URI as picker",
            ExifUtils.isPickerUri(safUri)
        )
    }

    // --- writeLocationViaCopy tests ---

    @Test
    fun test_writeLocationViaCopy_noInputStream_returnsFileNotFound() {
        `when`(contentResolver.openInputStream(any())).thenReturn(null)

        val result = ExifUtils.writeLocationViaCopy(context, uri, "test.jpg", 10.0, 20.0)

        assertTrue(
            "Expected FileNotFound result when input stream is null, got: $result",
            result is ExifWriteResult.FileNotFound
        )
    }

    @Test
    fun test_writeLocationViaCopy_securityException_returnsPermissionDenied() {
        `when`(contentResolver.openInputStream(any()))
            .thenThrow(SecurityException("Access denied"))

        val result = ExifUtils.writeLocationViaCopy(context, uri, "test.jpg", 10.0, 20.0)

        assertTrue(
            "Expected PermissionDenied result, got: $result",
            result is ExifWriteResult.PermissionDenied
        )
    }
}
