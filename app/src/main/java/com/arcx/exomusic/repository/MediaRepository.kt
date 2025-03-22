package com.arcx.exomusic.repository

import com.arcx.exomusic.model.MediaModel
import com.arcx.exomusic.utils.ContentResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaRepository @Inject constructor(
    private val contentResolver: ContentResolver
) {
    suspend fun getMediaList(): Flow<List<MediaModel>> = withContext(Dispatchers.IO) { contentResolver.getMediaList().flowOn(Dispatchers.IO) }
}