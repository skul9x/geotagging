package com.skul9x.geotagging.utils

import com.skul9x.geotagging.data.model.FileItem

object FileRangeFilter {
    private val comparator = NaturalOrderComparator()

    /**
     * Extracts a sublist of sorted [FileItem] objects given [startFileName] and [endFileName] (inclusive).
     */
    fun filterRange(
        files: List<FileItem>,
        startFileName: String,
        endFileName: String
    ): List<FileItem> {
        if (files.isEmpty()) return emptyList()

        val trimmedStart = startFileName.trim()
        val trimmedEnd = endFileName.trim()

        val sortedFiles = files.sortedWith { f1, f2 -> comparator.compare(f1.name, f2.name) }

        if (trimmedStart.isEmpty() && trimmedEnd.isEmpty()) {
            return sortedFiles
        }

        val startIndex = if (trimmedStart.isEmpty()) 0 else findStartIndex(sortedFiles, trimmedStart)
        val endIndex = if (trimmedEnd.isEmpty()) sortedFiles.lastIndex else findEndIndex(sortedFiles, trimmedEnd)

        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
            return emptyList()
        }

        return sortedFiles.subList(startIndex, endIndex + 1)
    }

    /**
     * Overload for filtering a list of string file names directly.
     */
    fun filterRangeNames(
        fileNames: List<String>,
        startFileName: String,
        endFileName: String
    ): List<String> {
        val fileItems = fileNames.map { FileItem(uri = it, name = it) }
        return filterRange(fileItems, startFileName, endFileName).map { it.name }
    }

    private fun findStartIndex(sortedFiles: List<FileItem>, target: String): Int {
        val exactIndex = sortedFiles.indexOfFirst { it.name.equals(target, ignoreCase = true) }
        if (exactIndex != -1) return exactIndex

        val baseNameIndex = sortedFiles.indexOfFirst {
            it.name.substringBeforeLast('.').equals(target, ignoreCase = true)
        }
        if (baseNameIndex != -1) return baseNameIndex

        val prefixIndex = sortedFiles.indexOfFirst {
            it.name.startsWith(target, ignoreCase = true)
        }
        if (prefixIndex != -1) return prefixIndex

        val nearestIndex = sortedFiles.indexOfFirst {
            comparator.compare(it.name, target) >= 0
        }
        if (nearestIndex != -1) return nearestIndex

        return -1
    }

    private fun findEndIndex(sortedFiles: List<FileItem>, target: String): Int {
        val exactIndex = sortedFiles.indexOfLast { it.name.equals(target, ignoreCase = true) }
        if (exactIndex != -1) return exactIndex

        val baseNameIndex = sortedFiles.indexOfLast {
            it.name.substringBeforeLast('.').equals(target, ignoreCase = true)
        }
        if (baseNameIndex != -1) return baseNameIndex

        val prefixIndex = sortedFiles.indexOfLast {
            it.name.startsWith(target, ignoreCase = true)
        }
        if (prefixIndex != -1) return prefixIndex

        val nearestIndex = sortedFiles.indexOfLast {
            comparator.compare(it.name, target) <= 0
        }
        if (nearestIndex != -1) return nearestIndex

        return -1
    }
}
