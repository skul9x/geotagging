package com.skul9x.geotagging.ui

import com.skul9x.geotagging.data.model.FileItem
import com.skul9x.geotagging.ui.range.components.filterFiles
import com.skul9x.geotagging.ui.range.components.isImageFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileRangeSearchTest {

    private val sampleFiles = listOf(
        FileItem(uri = "content://1", name = "IMG_001.jpg", size = 1024),
        FileItem(uri = "content://2", name = "IMG_002.jpg", size = 2048),
        FileItem(uri = "content://3", name = "DSC_003.png", size = 4096),
        FileItem(uri = "content://4", name = "document.pdf", size = 512),
        FileItem(uri = "content://5", name = "photo.WEBP", size = 1500),
        FileItem(uri = "content://6", name = "image.heic", size = 2500)
    )

    @Test
    fun filterFiles_emptyQuery_returnsFullList() {
        val result = filterFiles(sampleFiles, "")
        assertEquals(sampleFiles, result)

        val resultBlank = filterFiles(sampleFiles, "   ")
        assertEquals(sampleFiles, resultBlank)
    }

    @Test
    fun filterFiles_matchingQuery_returnsFilteredSubset() {
        val result = filterFiles(sampleFiles, "002")
        assertEquals(1, result.size)
        assertEquals("IMG_002.jpg", result.first().name)
    }

    @Test
    fun filterFiles_caseInsensitiveQuery_returnsMatches() {
        val result = filterFiles(sampleFiles, "img")
        assertEquals(2, result.size)
        assertEquals(listOf("IMG_001.jpg", "IMG_002.jpg"), result.map { it.name })
    }

    @Test
    fun filterFiles_noMatch_returnsEmptyList() {
        val result = filterFiles(sampleFiles, "nonexistent")
        assertTrue(result.isEmpty())
    }

    @Test
    fun isImageFile_detectsValidImageExtensions() {
        assertTrue(isImageFile("IMG_001.jpg"))
        assertTrue(isImageFile("photo.JPEG"))
        assertTrue(isImageFile("graphic.png"))
        assertTrue(isImageFile("picture.webp"))
        assertTrue(isImageFile("camera.heic"))
        assertTrue(isImageFile("draw.bmp"))
        assertTrue(isImageFile("anim.gif"))

        assertFalse(isImageFile("document.pdf"))
        assertFalse(isImageFile("video.mp4"))
        assertFalse(isImageFile("archive.zip"))
        assertFalse(isImageFile("noextension"))
    }
}
