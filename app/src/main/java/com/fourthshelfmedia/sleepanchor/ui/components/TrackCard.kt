package com.fourthshelfmedia.sleepanchor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fourthshelfmedia.sleepanchor.data.local.TrackEntity
import com.fourthshelfmedia.sleepanchor.ui.theme.AnchorCard
import com.fourthshelfmedia.sleepanchor.ui.theme.AnchorPlaying

@Composable
fun TrackCard(
    track: TrackEntity,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AnchorCard),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Cover art thumbnail
            AsyncImage(
                model = track.coverArt,
                contentDescription = track.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Track info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPlaying) AnchorPlaying
                    else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatDuration(track.durationSeconds),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (track.subNiche.isNotBlank()) {
                    Text(
                        text = track.subNiche,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            // Status icons
            if (isPlaying) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Now playing",
                    tint = AnchorPlaying,
                    modifier = Modifier.size(20.dp),
                )
            }
            if (track.isDownloaded) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = "Downloaded",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(start = 4.dp),
                )
            }
        }
    }
}

fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m}:${String.format("%02d", s)}"
}
