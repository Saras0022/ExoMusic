package com.arcx.exomusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import coil3.compose.AsyncImage
import com.arcx.exomusic.R
import com.arcx.exomusic.model.MediaModel
import com.arcx.exomusic.ui.screens.HomeScreenViewModel
import com.arcx.exomusic.utils.PlayerState
import kotlinx.coroutines.launch

enum class PainterState {
    ERROR,
    LOADING,
    SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaCards(modifier: Modifier = Modifier, mediaList: List<MediaModel>, mediaItemList: List<MediaItem>, viewModel: HomeScreenViewModel, scaffoldState: BottomSheetScaffoldState) {
    LazyColumn {
        itemsIndexed(mediaList) { index, media ->
            MediaCardItem(
                media = media,
                viewModel = viewModel,
                selectedIndex = index,
                mediaItemList = mediaItemList,
                scaffoldState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaCardItem(
    media: MediaModel,
    viewModel: HomeScreenViewModel,
    selectedIndex: Int,
    mediaItemList: List<MediaItem>,
    scaffoldState: BottomSheetScaffoldState
) {

    val scope = rememberCoroutineScope()

    val painterState = remember(media) { mutableStateOf(PainterState.LOADING) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clickable {
                viewModel.setMediaItem(
                    MediaItem.fromUri(media.contentUri),
                    mediaItemList = mediaItemList,
                    selectedIndex = selectedIndex
                )
                scope.launch {
                    if (viewModel.playerState.value == PlayerState.IDLE) scaffoldState.bottomSheetState.expand()
                }
            },
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                when (painterState.value) {
                    PainterState.LOADING, PainterState.SUCCESS -> {
                        AsyncImage(
                            model = media.albumArt,
                            contentDescription = "",
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .fillMaxWidth(0.16f)
                                .fillMaxHeight(),
                            contentScale = ContentScale.Fit,
                            onError = { painterState.value = PainterState.ERROR },
                            onLoading = { painterState.value = PainterState.LOADING }
                        )
                    }

                    PainterState.ERROR -> {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.music_notes),
                            contentDescription = "Album Art",
                            tint = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray)
                                .fillMaxWidth(0.16f)
                                .fillMaxHeight()
                        )
                    }
                }

            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    media.title,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    media.artist,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }
            Text(
                media.duration.toLong().convertToText(),
                fontSize = 12.sp
            )
        }
    }
}

fun Long.convertToText(): String {
    val sec = this / 1000
    val minutes = sec / 60
    val seconds = sec % 60

    val minutesString = if (minutes < 10) {
        "0$minutes"
    } else {
        minutes.toString()
    }
    val secondsString = if (seconds < 10) {
        "0$seconds"
    } else {
        seconds.toString()
    }
    return "$minutesString:$secondsString"
}