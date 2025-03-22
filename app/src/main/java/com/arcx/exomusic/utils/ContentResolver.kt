package com.arcx.exomusic.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import com.arcx.exomusic.model.MediaModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import androidx.core.net.toUri

class ContentResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mediaList = MutableStateFlow(mutableListOf<MediaModel>())
    private var cursor: Cursor? = null

    private val projection = arrayOf(
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.AudioColumns.ALBUM_ID,
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DURATION
    )

    private val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getMediaList(): Flow<List<MediaModel>> = getCursorData()

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getCursorData(): Flow<List<MediaModel>> {

        cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val displayNameIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val titleIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val idIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdIndex = it.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID)
            val durationIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            it.apply {
                while (it.moveToNext()) {
                    val displayName = getString(displayNameIndex)
                    val id = getLong(idIndex)
                    val albumId = getLong(albumIdIndex)
                    val title = getString(titleIndex)
                    val duration = getInt(durationIndex)
                    val artist = getString(artistIndex)
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                    val albumArt = ContentUris.withAppendedId("content://media/external/audio/albumart".toUri(), albumId)

                    mediaList.value.add(
                        MediaModel(
                            title = title,
                            artist = artist,
                            contentUri = uri,
                            id = id,
                            duration = duration,
                            albumArt = albumArt,
                            displayName = displayName,
                        )
                    )

                }
            }

        }
        return mediaList
    }
}