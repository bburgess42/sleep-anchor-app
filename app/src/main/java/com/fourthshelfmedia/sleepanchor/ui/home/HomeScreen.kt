package com.fourthshelfmedia.sleepanchor.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fourthshelfmedia.sleepanchor.ui.components.TrackCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    currentTrackId: String?,
    onTrackClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tracks by viewModel.tracks.collectAsState(initial = emptyList())
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (tracks.isEmpty() && !isRefreshing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No tracks yet",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "Syncing with server...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        text = "Sleep Anchor",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    Text(
                        text = "${tracks.size} tracks",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }

                items(tracks, key = { it.id }) { track ->
                    TrackCard(
                        track = track,
                        isPlaying = track.id == currentTrackId,
                        onClick = { onTrackClick(track.id) },
                    )
                }
            }
        }
    }
}
