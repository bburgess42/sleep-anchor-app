package com.fourthshelfmedia.sleepanchor.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Fetches the track manifest from the static JSON endpoint.
 * No server needed -- just a JSON file on GitHub Pages or Cloudflare R2.
 */
class ManifestApi(
    private val manifestUrl: String = DEFAULT_MANIFEST_URL,
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    data class ManifestTrack(
        val id: String,
        val title: String,
        val artist: String,
        val durationSeconds: Int,
        val genre: String,
        val subNiche: String,
        val mood: List<String>,
        val coverArt: String,
        val audioFile: String,
        val releaseDate: String,
        val addedToManifest: String,
        val binaural: Boolean,
        val tuningHz: Int,
    )

    data class Manifest(
        val version: Int,
        val audioBaseUrl: String,
        val trackCount: Int,
        val tracks: List<ManifestTrack>,
    )

    suspend fun fetchManifest(): Result<Manifest> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(manifestUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Manifest fetch failed: ${response.code}")
                )
            }

            val body = response.body?.string()
                ?: return@withContext Result.failure(Exception("Empty response"))

            val json = JSONObject(body)
            val tracksArray = json.getJSONArray("tracks")
            val tracks = (0 until tracksArray.length()).map { i ->
                parseTrack(tracksArray.getJSONObject(i))
            }

            Result.success(
                Manifest(
                    version = json.optInt("version", 1),
                    audioBaseUrl = json.optString("audio_base_url", ""),
                    trackCount = json.optInt("track_count", tracks.size),
                    tracks = tracks,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseTrack(json: JSONObject): ManifestTrack {
        val moodArray = json.optJSONArray("mood") ?: JSONArray()
        val moods = (0 until moodArray.length()).map { moodArray.getString(it) }

        return ManifestTrack(
            id = json.getString("id"),
            title = json.getString("title"),
            artist = json.optString("artist", "Sleep Anchor"),
            durationSeconds = json.optInt("duration_seconds", 0),
            genre = json.optString("genre", ""),
            subNiche = json.optString("sub_niche", ""),
            mood = moods,
            coverArt = json.optString("cover_art", ""),
            audioFile = json.optString("audio_file", ""),
            releaseDate = json.optString("release_date", ""),
            addedToManifest = json.optString("added_to_manifest", ""),
            binaural = json.optBoolean("binaural", false),
            tuningHz = json.optInt("tuning_hz", 440),
        )
    }

    companion object {
        // Update this when you set up hosting
        const val DEFAULT_MANIFEST_URL =
            "https://bburgess42.github.io/sleep-anchor-app/manifest.json"
    }
}
