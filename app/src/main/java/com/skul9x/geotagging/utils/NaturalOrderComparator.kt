package com.skul9x.geotagging.utils

class NaturalOrderComparator : Comparator<String> {
    override fun compare(s1: String?, s2: String?): Int {
        if (s1 == s2) return 0
        if (s1 == null) return -1
        if (s2 == null) return 1

        val len1 = s1.length
        val len2 = s2.length
        var i1 = 0
        var i2 = 0

        while (i1 < len1 && i2 < len2) {
            val c1 = s1[i1]
            val c2 = s2[i2]

            val isDigit1 = c1.isDigit()
            val isDigit2 = c2.isDigit()

            if (isDigit1 && isDigit2) {
                val start1 = i1
                while (i1 < len1 && s1[i1].isDigit()) {
                    i1++
                }
                val start2 = i2
                while (i2 < len2 && s2[i2].isDigit()) {
                    i2++
                }

                val numStr1 = s1.substring(start1, i1)
                val numStr2 = s2.substring(start2, i2)

                val trimmed1 = numStr1.trimStart('0')
                val trimmed2 = numStr2.trimStart('0')

                val cmp: Int = if (trimmed1.length != trimmed2.length) {
                    trimmed1.length.compareTo(trimmed2.length)
                } else {
                    trimmed1.compareTo(trimmed2)
                }

                if (cmp != 0) return cmp

                if (numStr1.length != numStr2.length) {
                    return numStr1.length.compareTo(numStr2.length)
                }
            } else {
                val charCmp = c1.lowercaseChar().compareTo(c2.lowercaseChar())
                if (charCmp != 0) return charCmp
                i1++
                i2++
            }
        }

        return len1.compareTo(len2)
    }
}
