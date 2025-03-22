package com.arcx.exomusic.utils

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class Player @Inject constructor(
    private val exoPlayer: ExoPlayer
) : Player.Listener {

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState = _playerState.asStateFlow()

    private val _repeatState = MutableStateFlow(RepeatModes.OFF)
    val repeatState = _repeatState.asStateFlow()

    private val job: Job? = null

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        when (isPlaying) {
            true -> _playerState.value = PlayerState.PLAYING
            false -> _playerState.value = PlayerState.PAUSED
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        super.onRepeatModeChanged(repeatMode)
        when (repeatMode) {

            Player.REPEAT_MODE_ALL -> _repeatState.value = RepeatModes.ALL

            Player.REPEAT_MODE_OFF -> _repeatState.value = RepeatModes.OFF

            Player.REPEAT_MODE_ONE -> _repeatState.value = RepeatModes.ONE
        }
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        super.onShuffleModeEnabledChanged(shuffleModeEnabled)

        when (shuffleModeEnabled) {
            true -> exoPlayer.shuffleModeEnabled = true
            false -> exoPlayer.shuffleModeEnabled = false
        }
    }

    suspend fun setMediaItem(
        mediaItem: MediaItem,
        mediaItemList: List<MediaItem>,
        selectedIndex: Int,
        playWhenReady: Boolean
    ) {
        val mediaItemsFirst = mediaItemList.subList(0, selectedIndex)
        val mediaItemsLast = mediaItemList.subList(selectedIndex + 1, mediaItemList.size)
        exoPlayer.clearMediaItems()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.addMediaItems(0, mediaItemsFirst)
        exoPlayer.addMediaItems(mediaItemsLast)
        exoPlayer.prepare()
        if (playWhenReady) exoPlayer.play()
    }

    fun setMediaItemList(mediaItemList: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItemList)
        exoPlayer.prepare()
    }

    fun playOrPause() = if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()

    fun repeatModeChanged() {
        when (exoPlayer.repeatMode) {
            Player.REPEAT_MODE_OFF -> exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    fun next() {
        exoPlayer.seekToNextMediaItem()
        exoPlayer.play()
    }

    fun previous() {
        exoPlayer.seekToPreviousMediaItem()
        exoPlayer.play()
    }

    fun seekTo(newPosition: Long) = exoPlayer.seekTo(newPosition)

    suspend fun currentPosition(position: (Long) -> Unit) {
        job.run {
            while (true) {
                position(exoPlayer.currentPosition)
                delay(1000)
            }
        }
    }
}

enum class PlayerState {
    IDLE,
    READY,
    ENDED,
    PLAYING,
    PAUSED
}

enum class RepeatModes {
    OFF,
    ONE,
    ALL
}