package com.arcx.exomusic

import android.app.Application
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication: Application() {

    @Inject lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        player.prepare()
    }

    override fun onTerminate() {
        super.onTerminate()
        player.release()
    }

}