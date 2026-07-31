package com.skul9x.geotagging.data.model

data class FileItem(
    val uri: String,
    val name: String,
    val size: Long = 0L,
    val mimeType: String = "",
    val lastModified: Long = 0L,
    val parentUri: String = ""
)
