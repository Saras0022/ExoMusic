package com.arcx.exomusic.ui.screens

import android.graphics.drawable.VectorDrawable
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerNotificationManager
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import com.arcx.exomusic.R
import com.arcx.exomusic.model.MediaModel
import com.arcx.exomusic.ui.components.MediaCards
import com.arcx.exomusic.utils.PlayerState
import com.arcx.exomusic.utils.RepeatModes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, viewModel: HomeScreenViewModel = hiltViewModel()) {

    val mediaList by viewModel.mediaList.collectAsState()

    val scaffoldState = rememberBottomSheetScaffoldState()

    val currentPosition by viewModel.currentPosition.collectAsState()

    val currentMediaItemIndex = viewModel.mExoPlayer.currentMediaItemIndex

    val exoPlayer = viewModel.mExoPlayer

    val isLoading by viewModel.isLoading.collectAsState()

    val selectedIndex = remember(viewModel.mExoPlayer) { mutableStateOf(0) }
    val mediaItemList = remember(true) { mutableListOf<MediaItem>() }
    LaunchedEffect(mediaList) {
        mediaList.forEach {
            mediaItemList.add(MediaItem.fromUri(it.contentUri))
        }
    }

    if (!isLoading) {
        val selectedTrack = remember(exoPlayer) { mutableStateOf(mediaList.first()) }

        LaunchedEffect(true) {
            if (mediaList.isNotEmpty()) {
                viewModel.setMediaItemList(mediaItemList)
            }
        }
        LaunchedEffect(currentPosition) {
            val dynamicIndex =
                if (currentMediaItemIndex != 0) currentMediaItemIndex else selectedIndex.value
            selectedTrack.value = mediaList[dynamicIndex]
        }

        BottomSheetScaffold(
            sheetContent = {
                Column(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (scaffoldState.bottomSheetState.currentValue) {
                        SheetValue.PartiallyExpanded -> PlayerBottomBar(selectedTrack.value, currentPosition, selectedTrack.value.duration.toLong(), viewModel, scaffoldState)
                        SheetValue.Hidden -> TODO()
                        SheetValue.Expanded -> PlayerTopContent(selectedTrack.value, viewModel, currentPosition, selectedTrack.value.duration.toLong(), exoPlayer)
                    }
                }
            },
            sheetDragHandle = { },
            sheetShape = RectangleShape,
            sheetPeekHeight = 75.dp,
            scaffoldState = scaffoldState,
            modifier = modifier
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MediaCards(
                    modifier = Modifier.padding(innerPadding),
                    mediaList = mediaList,
                    mediaItemList = mediaItemList,
                    viewModel = viewModel,
                    scaffoldState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerBottomBar(media: MediaModel, duration: Long, totalDuration: Long, viewModel: HomeScreenViewModel, sheetScaffoldState: BottomSheetScaffoldState) {

    val currentPosition = remember(duration) { mutableLongStateOf(duration) }
    val scope = rememberCoroutineScope()

    val playerState by viewModel.playerState.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .drawBehind {
                val progress =
                    duration.toFloat() / totalDuration.absoluteValue

                drawLine(
                    color = Color.White,
                    start = Offset(x = 0f, y = 1.dp.toPx()),
                    end = Offset(x = size.width * progress, y = 1.dp.toPx()),
                    strokeWidth = Stroke.DefaultMiter
                )
            }.clickable { scope.launch { sheetScaffoldState.bottomSheetState.expand() } }
    ) {

        Spacer(Modifier.width(12.dp))

        Box(
            Modifier.size(50.dp)
        ) {
            AsyncImage(
                media.albumArt,
                "Album Art",
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            )
        }

        Column(
            modifier = Modifier
                .height(50.dp)
                .weight(1f)
        ) {
            Text(media.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(media.artist, fontWeight = FontWeight.Normal, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Row {
            IconButton(
                onClick = { viewModel.playOrPause() }
            ) {
                when (playerState) {
                    PlayerState.PLAYING -> Icon(imageVector = ImageVector.vectorResource(R.drawable.pause), "")
                    else -> Icon(imageVector = ImageVector.vectorResource(R.drawable.play), "")
                }
            }
            IconButton(
                onClick = { viewModel.seekToNext() }
            ) { Icon(imageVector = ImageVector.vectorResource(R.drawable.skip_forward), "") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTopContent(media: MediaModel, viewModel: HomeScreenViewModel, duration: Long, totalDuration: Long, exoPlayer: ExoPlayer) {

    val playerState by viewModel.playerState.collectAsState()

    val repeatState by viewModel.repeatState.collectAsState()

    val currentPosition = remember(duration) { mutableLongStateOf(duration) }

    val interactionSource = remember { MutableInteractionSource() }

    val infiniteTransition = rememberInfiniteTransition()

    var shuffleMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(0.85f),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.width(12.dp))

        AsyncImage(
            model = media.albumArt,
            "Album Art",
            modifier = Modifier
                .fillMaxHeight(0.5f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
        ) {

            Text(media.title, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(media.artist, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Slider(
            interactionSource = interactionSource,
            value = currentPosition.longValue.toFloat(),
            onValueChange = {
                currentPosition.longValue = it.toLong()
            },
            onValueChangeFinished = {
                viewModel.seekTo(currentPosition.longValue)
            },
            valueRange = 0f..totalDuration.toFloat(),
            thumb = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .size(12.dp)
                        .background(Color.White)
                        .focusable(interactionSource = interactionSource)
                )
            }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IconButton(
                onClick = {
                    shuffleMode = !shuffleMode
                    viewModel.shuffleToggle(shuffleMode) }
            ) { when (shuffleMode) {
                true -> Icon(ImageVector.vectorResource(R.drawable.shuffle), "")
                false -> Icon(ImageVector.vectorResource(R.drawable.shuffle), "", tint = Color.Gray)
            } }

            IconButton(
                onClick = { viewModel.seekToPrevious() }
            ) { Icon(imageVector = ImageVector.vectorResource(R.drawable.skip_back), "") }
            IconButton(
                onClick = { viewModel.playOrPause() }
            ) {
                when (playerState) {
                    PlayerState.PLAYING -> Icon(imageVector = ImageVector.vectorResource(R.drawable.pause), "")
                    else -> Icon(imageVector = ImageVector.vectorResource(R.drawable.play), "")
                }
            }
            IconButton(
                onClick = { viewModel.seekToNext() }
            ) { Icon(imageVector = ImageVector.vectorResource(R.drawable.skip_forward), "") }

            IconButton(
                onClick = { viewModel.repeatModeChanged() }
            ) {
                when (repeatState) {
                    RepeatModes.OFF -> Icon(ImageVector.vectorResource(R.drawable.repeat), "", tint = Color.Gray)
                    RepeatModes.ONE -> Icon(ImageVector.vectorResource(R.drawable.repeat_once), "")
                    RepeatModes.ALL -> Icon(ImageVector.vectorResource(R.drawable.repeat), "")
                }
            }

        }

    }

}