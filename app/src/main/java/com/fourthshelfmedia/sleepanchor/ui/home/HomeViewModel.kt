package com.fourthshelfmedia.sleepanchor.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fourthshelfmedia.sleepanchor.data.local.TrackEntity
import com.fourthshelfmedia.sleepanchor.data.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TrackRepository(application)

    val tracks: Flow<List<TrackEntity>> = repository.allTracks

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.syncManifest() // Silently handle errors for now
            _isRefreshing.value = false
        }
    }
}
