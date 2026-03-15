package com.fourthshelfmedia.sleepanchor

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.fourthshelfmedia.sleepanchor.data.local.TrackEntity
import com.fourthshelfmedia.sleepanchor.data.repository.TrackRepository
import com.fourthshelfmedia.sleepanchor.player.PlaybackService
import com.fourthshelfmedia.sleepanchor.player.SleepTimerManager
import com.fourthshelfmedia.sleepanchor.ui.components.MiniPlayerBar
import com.fourthshelfmedia.sleepanchor.ui.home.HomeScreen
import com.fourthshelfmedia.sleepanchor.ui.home.HomeViewModel
import com.fourthshelfmedia.sleepanchor.ui.player.NowPlayingScreen
import com.fourthshelfmedia.sleepanchor.ui.theme.AnchorBlack
import com.fourthshelfmedia.sleepanchor.ui.theme.SleepAnchorTheme
import com.fourthshelfmedia.sleepanchor.ui.timer.SleepTimerSheet
import android.util.Log
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var mediaController: MediaController? = null
    private var sleepTimerManager: SleepTimerManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SleepAnchorTheme {
                SleepAnchorRoot()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            sleepTimerManager = SleepTimerManager(mediaController!!)
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() {
        mediaController?.release()
        mediaController = null
        sleepTimerManager = null
        super.onStop()
    }

    @Composable
    private fun SleepAnchorRoot() {
        val homeViewModel: HomeViewModel = viewModel()
        val repository = remember { TrackRepository(applicationContext) }

        // UI state
        var currentScreen by remember { mutableStateOf("home") }
        var currentTrackId by remember { mutableStateOf<String?>(null) }
        var currentTrack by remember { mutableStateOf<TrackEntity?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        var progressFraction by remember { mutableFloatStateOf(0f) }
        var elapsedSeconds by remember { mutableIntStateOf(0) }
        var showTimerSheet by remember { mutableStateOf(false) }

        // Sleep timer state
        val timerActive = sleepTimerManager?.isActive?.collectAsState()?.value ?: false
        val timerRemainingMs = sleepTimerManager?.remainingMs?.collectAsState()?.value ?: 0L
        val timerText = if (timerActive) sleepTimerManager?.formatRemaining(timerRemainingMs) else null

        // Poll player state for progress updates
        LaunchedEffect(mediaController) {
            while (true) {
                mediaController?.let { controller ->
                    isPlaying = controller.isPlaying
                    if (controller.duration > 0) {
                        progressFraction =
                            controller.currentPosition.toFloat() / controller.duration
                        elapsedSeconds = (controller.currentPosition / 1000).toInt()
                    }
                    val mediaId = controller.currentMediaItem?.mediaId
                    if (mediaId != currentTrackId) {
                        currentTrackId = mediaId
                    }
                }
                delay(500) // Update every 500ms
            }
        }

        // Load current track entity when track changes
        LaunchedEffect(currentTrackId) {
            currentTrackId?.let { id ->
                currentTrack = repository.getTrack(id)
            }
        }

        // Play a track
        fun playTrack(trackId: String) {
            val controller = mediaController ?: return
            val tracks = homeViewModel.tracks

            kotlinx.coroutines.MainScope().launch {
                val trackList = tracks.first()
                val index = trackList.indexOfFirst { it.id == trackId }
                if (index < 0) return@launch

                // Build the full queue
                controller.clearMediaItems()
                trackList.forEach { track ->
                    val audioUrl = track.localAudioPath ?: track.audioFile
                    controller.addMediaItem(
                        PlaybackService.buildMediaItem(
                            trackId = track.id,
                            title = track.title,
                            artist = track.artist,
                            audioUrl = audioUrl,
                            coverArtUrl = track.coverArt,
                        )
                    )
                }

                Log.d("SleepAnchor", "Playing track $trackId at index $index, audioUrl=${trackList[index].audioFile}")
                controller.seekToDefaultPosition(index)
                controller.prepare()
                controller.play()

                currentTrackId = trackId
                currentTrack = trackList[index]
                currentScreen = "player"

                repository.recordPlay(trackId)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AnchorBlack)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Main content area
                Box(modifier = Modifier.weight(1f)) {
                    when (currentScreen) {
                        "home" -> HomeScreen(
                            viewModel = homeViewModel,
                            currentTrackId = currentTrackId,
                            onTrackClick = { trackId -> playTrack(trackId) },
                        )

                        "player" -> NowPlayingScreen(
                            track = currentTrack,
                            isPlaying = isPlaying,
                            progressFraction = progressFraction,
                            elapsedSeconds = elapsedSeconds,
                            timerText = timerText,
                            onBack = { currentScreen = "home" },
                            onPlayPause = {
                                mediaController?.let {
                                    if (it.isPlaying) it.pause() else it.play()
                                }
                            },
                            onSkipPrev = { mediaController?.seekToPreviousMediaItem() },
                            onSkipNext = { mediaController?.seekToNextMediaItem() },
                            onSleepTimer = { showTimerSheet = true },
                            onDownload = {
                                currentTrackId?.let { id ->
                                    kotlinx.coroutines.MainScope().launch {
                                        val track = repository.getTrack(id)
                                        if (track?.isDownloaded == true) {
                                            repository.removeDownload(id)
                                        } else {
                                            repository.downloadTrack(id)
                                        }
                                        currentTrack = repository.getTrack(id)
                                    }
                                }
                            },
                        )
                    }
                }

                // Mini player bar (visible on home screen when something is playing)
                if (currentScreen == "home" && currentTrack != null) {
                    MiniPlayerBar(
                        trackTitle = currentTrack!!.title,
                        isPlaying = isPlaying,
                        timerText = timerText,
                        onPlayPause = {
                            mediaController?.let {
                                if (it.isPlaying) it.pause() else it.play()
                            }
                        },
                        onTap = { currentScreen = "player" },
                    )
                }
            }

            // Sleep timer bottom sheet
            if (showTimerSheet) {
                SleepTimerSheet(
                    isTimerActive = timerActive,
                    onSetTimer = { minutes, fadeOut ->
                        val durationMs = minutes * 60 * 1000L
                        val fadeMs = if (fadeOut) 5 * 60 * 1000L else 0L
                        sleepTimerManager?.start(durationMs, fadeMs)
                        showTimerSheet = false
                    },
                    onCancelTimer = {
                        sleepTimerManager?.cancel()
                        showTimerSheet = false
                    },
                    onDismiss = { showTimerSheet = false },
                )
            }
        }
    }
}
