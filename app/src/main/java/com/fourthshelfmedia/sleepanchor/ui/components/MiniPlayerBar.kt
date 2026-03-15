package com.fourthshelfmedia.sleepanchor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fourthshelfmedia.sleepanchor.ui.theme.AnchorCard
import com.fourthshelfmedia.sleepanchor.ui.theme.AnchorPlaying

@Composable
fun MiniPlayerBar(
    trackTitle: String,
    isPlaying: Boolean,
    timerText: String?,
    onPlayPause: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AnchorCard)
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = trackTitle,
                style = MaterialTheme.typography.titleMedium,
                color = AnchorPlaying,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (timerText != null) {
                Text(
                    text = "Sleep timer: $timerText",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        IconButton(onClick = onPlayPause) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = AnchorPlaying,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
