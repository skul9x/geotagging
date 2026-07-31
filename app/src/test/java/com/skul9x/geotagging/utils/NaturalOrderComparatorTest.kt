package com.skul9x.geotagging.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NaturalOrderComparatorTest {

    private val comparator = NaturalOrderComparator()

    @Test
    fun testNaturalSortingAlphanumeric() {
        val input = listOf("abc10", "abc1", "abc2", "abc9")
        val expected = listOf("abc1", "abc2", "abc9", "abc10")
        val sorted = input.sortedWith(comparator)
        assertEquals(expected, sorted)
    }

    @Test
    fun testCaseInsensitiveSorting() {
        val input = listOf("abc2", "ABC1", "Abc3")
        val expected = listOf("ABC1", "abc2", "Abc3")
        val sorted = input.sortedWith(comparator)
        assertEquals(expected, sorted)
    }

    @Test
    fun testComplexFileNames() {
        val input = listOf("img_10.jpg", "img_01.jpg", "img_2.jpg")
        val expected = listOf("img_01.jpg", "img_2.jpg", "img_10.jpg")
        val sorted = input.sortedWith(comparator)
        assertEquals(expected, sorted)
    }

    @Test
    fun testPerformance10000Files() {
        val input = (1..10000).shuffled().map { "file_$it.png" }
        val startTime = System.currentTimeMillis()
        val sorted = input.sortedWith(comparator)
        val duration = System.currentTimeMillis() - startTime

        assertEquals(10000, sorted.size)
        assertEquals("file_1.png", sorted.first())
        assertEquals("file_10000.png", sorted.last())
        assertTrue("Sorting 10,000 files took $duration ms, expected < 500ms", duration < 500)
    }
}
