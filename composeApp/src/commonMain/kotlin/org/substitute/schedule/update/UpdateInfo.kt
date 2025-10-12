package org.substitute.schedule.update

data class UpdateInfo(
    val version: String,
    val releaseNotes: String,
    val downloadUrl: String?,
    val storeUrl: String?,
    val releaseDate: String,
    val isMandatory: Boolean = false
)