package com.skul9x.geotagging.utils

object GpsCoordinateParser {
    private val COORDINATE_REGEX = Regex("""[+-]?\d+(?:[.,]\d+)?""")

    /**
     * Parses a single line string containing latitude and longitude coordinates.
     * Supports dot ('.') and comma (',') decimal separators as well as various
     * string delimiters (commas, spaces, tabs, etc.).
     *
     * Example inputs:
     * - "21.1573890, 106.1998193"
     * - "21,1573890, 106,1998193"
     * - "21,1573890 106,1998193"
     */
    fun parseSingleLineCoordinates(input: String): Pair<Double, Double>? {
        if (input.isBlank()) return null
        
        val matches = COORDINATE_REGEX.findAll(input).map { it.value }.toList()
        if (matches.size < 2) return null

        val lat = matches[0].replace(',', '.').toDoubleOrNull() ?: return null
        val long = matches[1].replace(',', '.').toDoubleOrNull() ?: return null

        return if (isValidCoordinate(lat, long)) Pair(lat, long) else null
    }

    /**
     * Parses separate latitude and longitude strings into a Pair of Doubles.
     * Supports dot ('.') and comma (',') decimal separators.
     * If longStr is empty and latStr contains a single line coordinate string, it attempts auto-parsing.
     */
    fun parseCoordinates(latStr: String, longStr: String): Pair<Double, Double>? {
        if (latStr.isBlank() && longStr.isBlank()) return null
        
        if (latStr.isNotBlank() && longStr.isBlank()) {
            return parseSingleLineCoordinates(latStr)
        }

        val lat = latStr.trim().replace(',', '.').toDoubleOrNull() ?: return null
        val long = longStr.trim().replace(',', '.').toDoubleOrNull() ?: return null

        return if (isValidCoordinate(lat, long)) Pair(lat, long) else null
    }

    /**
     * Checks if latitude is in range [-90.0, 90.0] and longitude is in range [-180.0, 180.0].
     */
    fun isValidCoordinate(lat: Double, long: Double): Boolean {
        return lat >= -90.0 && lat <= 90.0 && long >= -180.0 && long <= 180.0
    }
}
