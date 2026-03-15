package com.fourthshelfmedia.sleepanchor.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fourthshelfmedia.sleepanchor.data.local.TrackEntity
import com.fourthshelfmedia.sleepanchor.ui.components.formatDuration
import com.fourthshelfmedia.sleepanchor.ui.theme.AnchorAccent
import com.fourthshelfmedia.sleepanchor.ui.theme.AnchorBlack
import com.fourthshelfmedia.sleepanchor.ui.theme.AnchorCard
import com.fourthshelfmedia.sleepanchor.ui.theme.AnchorTextDim

@Composable
fun NowPlayingScreen(
    track: TrackEntity?,
    isPlaying: Boolean,
    progressFraction: Float,
    elapsedSeconds: Int,
    timerText: String?,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipPrev: () -> Unit,
    onSkipNext: () -> Unit,
    onSleepTimer: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (track == null) return

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AnchorBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (timerText != null) {
                Text(
                    text = "Sleep in $timerText",
                    style = MaterialTheme.typography.labelSmall,
                    color = AnchorAccent,
                )
            }

            // Download button
            IconButton(onClick = onDownload) {
                Icon(
                    imageVector = if (track.isDownloaded) Icons.Default.CloudDone
                    else Icons.Default.CloudDownload,
                    contentDescription = if (track.isDownloaded) "Downloaded" else "Download",
                    tint = if (track.isDownloaded) AnchorAccent
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Cover art
        AsyncImage(
            model = track.coverArt,
            contentDescription = track.title,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Track info
        Text(
            text = track.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = track.artist,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
        )
        if (track.binaural) {
            Text(
                text = "${track.tuningHz} Hz  /  Binaural",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = AnchorAccent,
            trackColor = AnchorCard,
        )

        // Time labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatDuration(elapsedSeconds),
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = formatDuration(track.durationSeconds),
                style = MaterialTheme.typography.labelSmall,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Playback controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onSkipPrev) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Big play/pause button
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(72.dp)
                    .background(AnchorAccent, CircleShape),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(40.dp),
                    tint = AnchorBlack,
                )
            }

            IconButton(onClick = onSkipNext) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Sleep timer button
        IconButton(
            onClick = onSleepTimer,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                Icons.Default.Bedtime,
                contentDescription = "Sleep Timer",
                tint = if (timerText != null) AnchorAccent else AnchorTextDim,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
