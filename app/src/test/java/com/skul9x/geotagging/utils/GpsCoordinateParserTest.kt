package com.skul9x.geotagging.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GpsCoordinateParserTest {

    @Test
    fun parse_standardDotCoordinates_returnsCorrectDoublePair() {
        val result = GpsCoordinateParser.parseCoordinates("21.1573890", "106.1998193")
        assertNotNull(result)
        assertEquals(21.1573890, result!!.first, 0.0000001)
        assertEquals(106.1998193, result.second, 0.0000001)
    }

    @Test
    fun parse_commaDecimalCoordinates_returnsCorrectDoublePair() {
        val result = GpsCoordinateParser.parseCoordinates("21,1573890", "106,1998193")
        assertNotNull(result)
        assertEquals(21.1573890, result!!.first, 0.0000001)
        assertEquals(106.1998193, result.second, 0.0000001)
    }

    @Test
    fun parse_singleLineGoogleMapsFormatWithCommas_returnsCorrectDoublePair() {
        val result = GpsCoordinateParser.parseSingleLineCoordinates("21,1573890, 106,1998193")
        assertNotNull(result)
        assertEquals(21.1573890, result!!.first, 0.0000001)
        assertEquals(106.1998193, result.second, 0.0000001)
    }

    @Test
    fun parse_singleLineGoogleMapsFormatWithDots_returnsCorrectDoublePair() {
        val result = GpsCoordinateParser.parseSingleLineCoordinates("21.1573890, 106.1998193")
        assertNotNull(result)
        assertEquals(21.1573890, result!!.first, 0.0000001)
        assertEquals(106.1998193, result.second, 0.0000001)
    }

    @Test
    fun parse_singleLineSpaceSeparated_returnsCorrectDoublePair() {
        val result = GpsCoordinateParser.parseSingleLineCoordinates("21.1573890 106.1998193")
        assertNotNull(result)
        assertEquals(21.1573890, result!!.first, 0.0000001)
        assertEquals(106.1998193, result.second, 0.0000001)
    }

    @Test
    fun parse_outOfRangeCoordinates_returnsNull() {
        val result1 = GpsCoordinateParser.parseCoordinates("95.0", "200.0")
        assertNull(result1)

        val result2 = GpsCoordinateParser.parseSingleLineCoordinates("95.0, 200.0")
        assertNull(result2)

        val result3 = GpsCoordinateParser.parseCoordinates("-91.0", "100.0")
        assertNull(result3)

        val result4 = GpsCoordinateParser.parseCoordinates("21.0", "-181.0")
        assertNull(result4)
    }

    @Test
    fun parse_invalidText_returnsNull() {
        val result1 = GpsCoordinateParser.parseSingleLineCoordinates("invalid string")
        assertNull(result1)

        val result2 = GpsCoordinateParser.parseCoordinates("abc", "def")
        assertNull(result2)

        val result3 = GpsCoordinateParser.parseSingleLineCoordinates("")
        assertNull(result3)
    }
}
