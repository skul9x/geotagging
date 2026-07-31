package com.skul9x.geotagging.utils

import com.skul9x.geotagging.data.model.FileItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FileRangeFilterTest {

    @Test
    fun testExtractValidRange() {
        val input = listOf("abc1", "abc2", "abc3", "abc4", "abc5").map { FileItem(uri = "path/$it", name = it) }
        val result = FileRangeFilter.filterRange(input, "abc1", "abc4")
        val resultNames = result.map { it.name }

        assertEquals(4, result.size)
        assertEquals(listOf("abc1", "abc2", "abc3", "abc4"), resultNames)
    }

    @Test
    fun testStartFileGreaterThanEndFile() {
        val input = listOf("abc1", "abc2", "abc3", "abc4", "abc5").map { FileItem(uri = "path/$it", name = it) }
        val result = FileRangeFilter.filterRange(input, "abc4", "abc1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun testPartialMatchingRange() {
        val input = listOf("img_001.jpg", "img_002.jpg", "img_003.jpg", "img_004.jpg")
            .map { FileItem(uri = "path/$it", name = it) }

        // Start given without extension, end given without extension
        val result = FileRangeFilter.filterRange(input, "img_001", "img_003")
        val resultNames = result.map { it.name }

        assertEquals(3, result.size)
        assertEquals(listOf("img_001.jpg", "img_002.jpg", "img_003.jpg"), resultNames)
    }

    @Test
    fun testSingleFileRange() {
        val input = listOf("abc1", "abc2", "abc3", "abc4", "abc5").map { FileItem(uri = "path/$it", name = it) }
        val result = FileRangeFilter.filterRange(input, "abc2", "abc2")
        val resultNames = result.map { it.name }

        assertEquals(1, result.size)
        assertEquals(listOf("abc2"), resultNames)
    }
}
