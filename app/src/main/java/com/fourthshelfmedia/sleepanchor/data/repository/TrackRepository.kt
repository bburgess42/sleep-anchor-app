package com.fourthshelfmedia.sleepanchor.data.repository

import android.content.Context
import com.fourthshelfmedia.sleepanchor.data.local.AppDatabase
import com.fourthshelfmedia.sleepanchor.data.local.TrackEntity
import com.fourthshelfmedia.sleepanchor.data.remote.ManifestApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class TrackRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.trackDao()
    private val api = ManifestApi()
    private val httpClient = OkHttpClient()
    private val appContext = context.applicationContext

    /** Observable list of all tracks, newest first. */
    val allTracks: Flow<List<TrackEntity>> = dao.getAllTracks()

    /** Observable list of downloaded tracks. */
    val downloadedTracks: Flow<List<TrackEntity>> = dao.getDownloadedTracks()

    /**
     * Sync with remote manifest. Returns count of new tracks added.
     * Safe to call frequently -- uses INSERT IGNORE so existing tracks aren't overwritten.
     */
    suspend fun syncManifest(): Result<Int> {
        val result = api.fetchManifest()
        val manifest = result.getOrElse { return Result.failure(it) }

        val entities = manifest.tracks.map { track ->
            TrackEntity(
                id = track.id,
                title = track.title,
                artist = track.artist,
                durationSeconds = track.durationSeconds,
                genre = track.genre,
                subNiche = track.subNiche,
                mood = track.mood.joinToString(","),
                coverArt = "${manifest.audioBaseUrl}/art/${track.coverArt}",
                audioFile = "${manifest.audioBaseUrl}/tracks/${track.audioFile}",
                releaseDate = track.releaseDate,
                addedToManifest = track.addedToManifest,
                binaural = track.binaural,
                tuningHz = track.tuningHz,
            )
        }

        val countBefore = dao.getTrackCount()
        dao.insertTracks(entities)
        val countAfter = dao.getTrackCount()

        return Result.success(countAfter - countBefore)
    }

    /** Download a track's audio file for offline playback. */
    suspend fun downloadTrack(trackId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val track = dao.getTrack(trackId)
                ?: return@withContext Result.failure(Exception("Track not found"))

            val dir = File(appContext.filesDir, "tracks")
            dir.mkdirs()
            val file = File(dir, "${track.id}.ogg")

            val request = Request.Builder().url(track.audioFile).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Download failed: ${response.code}"))
            }

            response.body?.byteStream()?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            dao.setDownloaded(trackId, true, file.absolutePath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Remove a downloaded track's local file. */
    suspend fun removeDownload(trackId: String) {
        val track = dao.getTrack(trackId) ?: return
        track.localAudioPath?.let { File(it).delete() }
        dao.setDownloaded(trackId, false, null)
    }

    /** Record that a track was played. */
    suspend fun recordPlay(trackId: String) {
        dao.recordPlay(trackId, System.currentTimeMillis())
    }

    suspend fun getTrack(trackId: String): TrackEntity? = dao.getTrack(trackId)
}
