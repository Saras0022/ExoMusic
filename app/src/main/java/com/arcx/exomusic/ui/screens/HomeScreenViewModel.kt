package com.arcx.exomusic.ui.screens

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Player.RepeatMode
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import com.arcx.exomusic.model.MediaModel
import com.arcx.exomusic.repository.MediaRepository
import com.arcx.exomusic.utils.Player
import com.arcx.exomusic.utils.RepeatModes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val player: Player,
    private val exoPlayer: ExoPlayer
): ViewModel() {

    private val _mediaList = MutableStateFlow<List<MediaModel>>(listOf())
    val mediaList = _mediaList.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _exoplayer = exoPlayer
    val mExoPlayer = _exoplayer

    private val _playerState = player.playerState
    val playerState = _playerState

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val repeatState = player.repeatState

    init {
        getMediaList()
        currentTime()
        exoPlayer.addListener(player)
    }

    private fun getMediaList() {
        viewModelScope.launch {
            mediaRepository.getMediaList()
                .distinctUntilChanged()
                .collectLatest { mMediaList ->
                    if (mMediaList.isNotEmpty()) {
                        _mediaList.update { mMediaList }
                        _isLoading.value = false
                    }
                }
        }
    }

    fun setMediaItem(
        mediaItem: MediaItem,
        mediaItemList: List<MediaItem>,
        selectedIndex: Int,
        playWhenReady: Boolean = true
    ) {
        viewModelScope.launch {
            player.setMediaItem(
                mediaItem,
                mediaItemList,
                selectedIndex,
                playWhenReady
            )
        }
    }

    fun setMediaItemList(mediaItemList: List<MediaItem>) {
        viewModelScope.launch {
            player.setMediaItemList(mediaItemList)
        }
    }

    fun shuffleToggle(mode: Boolean) = player.onShuffleModeEnabledChanged(mode)
    fun repeatModeChanged() = player.repeatModeChanged()
    fun playOrPause() = player.playOrPause()
    fun seekToNext() = player.next()
    fun seekToPrevious() = player.previous()
    fun seekTo(newPostion: Long) = player.seekTo(newPostion)
    private fun currentTime() {
        viewModelScope.launch {
            player.currentPosition { position ->
                _currentPosition.update { position }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.removeListener(player)
        exoPlayer.release()
    }
}

