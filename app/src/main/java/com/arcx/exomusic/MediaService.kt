package com.arcx.exomusic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaService: MediaSessionService() {

    @Inject lateinit var mediaSession: MediaSession

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.createNotificationChannel(NotificationChannel(
            "100",
            "YooHoo",
            NotificationManager.IMPORTANCE_LOW
        ))

        val notification = NotificationCompat.Builder(this, "100")
            .setContentTitle("Yoo")
            .setContentText("Haa")
            .build()

        ServiceCompat.startForeground(
            this,
            startId,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.run {
            player.stop()
            release()
        }
    }
}