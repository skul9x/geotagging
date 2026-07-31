package com.skul9x.geotagging.data.model

import android.net.Uri

data class GeoImage(
    val id: String,
    val uri: Uri,
    val name: String,
    val dateAdded: Long,
    val size: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isPickerUri: Boolean = false
)