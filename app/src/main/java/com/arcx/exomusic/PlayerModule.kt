package com.arcx.exomusic

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.arcx.exomusic.utils.ContentResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PlayerModule {

    @Singleton
    @Provides
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    @Singleton
    @Provides
    fun provideContentResolver(@ApplicationContext context: Context) = ContentResolver(context)

    @Singleton
    @Provides
    fun provideMediaSession(@ApplicationContext context: Context, exoPlayer: ExoPlayer) = MediaSession.Builder(context, exoPlayer).build()

}