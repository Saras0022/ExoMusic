package com.arcx.exomusic.model

import android.net.Uri

data class MediaModel(
    val title: String,
    val artist: String,
    val contentUri: Uri,
    val id: Long,
    val duration: Int,
    val albumArt: Uri,
    val displayName: String
)