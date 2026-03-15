package com.fourthshelfmedia.sleepanchor.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val genre: String,
    val subNiche: String,
    val mood: String, // JSON array stored as string
    val coverArt: String,
    val audioFile: String,
    val releaseDate: String,
    val addedToManifest: String,
    val binaural: Boolean,
    val tuningHz: Int,
    val isDownloaded: Boolean = false,
    val localAudioPath: String? = null,
    val localCoverPath: String? = null,
    val lastPlayedAt: Long? = null,
    val playCount: Int = 0,
)
