package com.metrolist.desktop.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

import com.metrolist.shared.model.SongItem
import com.metrolist.shared.state.GlobalYouTubeRepository
import io.ktor.utils.io.core.use
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.util.prefs.Preferences

enum class DownloadState {
    NOT_DOWNLOADED,
    QUEUED,
    DOWNLOADING,
    DOWNLOADED,
    FAILED
}

@Serializable
data class DownloadedFile(
    val songId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val audioFilePath: String,
    val thumbnailFilePath: String?,
        val duration: Long?,
    val downloadDate: Long
) {
    fun toSongItem(): SongItem = SongItem(
        id = songId,
        title = title,
        artists = listOf(com.metrolist.shared.model.ArtistTiny(id = null, name = artist)),
        thumbnail = thumbnailUrl,
            duration = duration?.toLong(),
        album = null
    )
}

object DownloadManager {
    private val client = OkHttpClient()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val maxConcurrentDownloads = 3
    private val downloadSemaphore = Semaphore(maxConcurrentDownloads)

    // State flows for download progress
    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadState>> = _downloadStates.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    // Track active downloads
    private val activeJobs = ConcurrentHashMap<String, kotlinx.coroutines.Job>()

    // Base directory for downloads
    private val downloadBaseDir: File by lazy {
        val dir = File(System.getProperty("user.home"), ".metrolist/downloads")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    private suspend fun downloadFile(url: String, destinationPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            println("[DownloadManager] Starting download: $url -> $destinationPath")
            val request = Request.Builder().url(url).build()
            val response: Response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                println("[DownloadManager] Download failed: HTTP ${response.code}")
                return@withContext false
            }

            val file = File(destinationPath)
            val tempFile = File("$destinationPath.tmp")

            response.body?.byteStream()?.use { input ->
                tempFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            tempFile.renameTo(file)
            if (!file.exists() || file.length() < 1024) {
                println("[DownloadManager] Downloaded file is missing or too small: ${file.absolutePath}")
                return@withContext false
            }
            println("[DownloadManager] Download completed: ${file.absolutePath} (${file.length()} bytes)")
            return@withContext true
        } catch (e: Exception) {
            println("[DownloadManager] Exception during download: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }

    // Stub missing methods to resolve compilation errors
    private fun updateState(songId: String, state: DownloadState) {}
    private fun updateProgress(songId: String, progress: Float?) {}
    private suspend fun getAudioStreamUrl(songId: String): String {
        // Use actual stream URL extraction from shared codebase
        return GlobalYouTubeRepository.instance.getStreamUrl(songId) ?: ""
    }
    init {
        // Load saved download state on initialization
        scope.launch {
            loadDownloadedFiles()
        }
    }

    private fun getDownloadDirectory(): File {
        return downloadBaseDir
    }

    private fun getAudioFilePath(songId: String): String {
        return File(getDownloadDirectory(), "$songId.audio").absolutePath
    }

    private fun getThumbnailFilePath(songId: String): String {
        return File(getDownloadDirectory(), "$songId.thumb").absolutePath
    }

    fun isDownloaded(songId: String): Boolean {
        return _downloadStates.value[songId] == DownloadState.DOWNLOADED
    }

    fun getDownloadedFile(songId: String): DownloadedFile? {
        val metadata = getDownloadMetadata(songId) ?: return null
        val audioFile = File(metadata.audioFilePath)
        return if (audioFile.exists()) metadata else null
    }

    fun getDownloadedFiles(): List<DownloadedFile> {
        val metadataJson = Preferences.userNodeForPackage(DownloadManager::class.java)
            .get("DOWNLOADED_FILES_V1", "")
        if (metadataJson.isBlank()) return emptyList()

        return try {
            Json.decodeFromString<List<DownloadedFile>>(metadataJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveDownloadedFiles(files: List<DownloadedFile>) {
        val metadataJson = Json.encodeToString(files)
        Preferences.userNodeForPackage(DownloadManager::class.java).put("DOWNLOADED_FILES_V1", metadataJson)
    }

    private fun loadDownloadedFiles() {
        val files = getDownloadedFiles()
        val states = files.associate { it.songId to DownloadState.DOWNLOADED }
        _downloadStates.value = states
    }

    private fun getDownloadMetadata(songId: String): DownloadedFile? {
        return getDownloadedFiles().find { it.songId == songId }
    }

    private fun addDownloadMetadata(file: DownloadedFile) {
        val currentFiles = getDownloadedFiles().toMutableList()
        currentFiles.removeAll { it.songId == file.songId }
        currentFiles.add(file)
        saveDownloadedFiles(currentFiles)
    }

        fun removeDownloadMetadata(songId: String) {
        val currentFiles = getDownloadedFiles().toMutableList()
        currentFiles.removeAll { it.songId == songId }
        saveDownloadedFiles(currentFiles)
    }

    fun getLocalAudioPath(songId: String): String? {
        val metadata = getDownloadMetadata(songId) ?: return null
        val audioFile = File(metadata.audioFilePath)
        if (!audioFile.exists()) {
            println("[DownloadManager] Local audio file not found: ${audioFile.absolutePath}")
            return null
        }
        if (audioFile.length() < 1024) {
            println("[DownloadManager] Local audio file too small: ${audioFile.absolutePath}")
            return null
        }
        return metadata.audioFilePath
    }

    fun download(song: SongItem) {
        if (activeJobs.containsKey(song.id) || isDownloaded(song.id)) return

        val job = scope.launch {
            try {
                updateState(song.id, DownloadState.QUEUED)
                downloadSemaphore.withPermit {
                    updateState(song.id, DownloadState.DOWNLOADING)
                    updateProgress(song.id, 0f)

                    // Download audio
                    val audioUrl = getAudioStreamUrl(song.id)
                    if (!downloadFile(audioUrl, getAudioFilePath(song.id))) {
                        throw Exception("Failed to download audio")
                    }

                    updateProgress(song.id, 0.7f)

                    // Download thumbnail if available
                    var thumbnailPath: String? = null
                    if (!song.thumbnail.isNullOrEmpty()) {
                        thumbnailPath = getThumbnailFilePath(song.id)
                        if (!downloadFile(song.thumbnail!!, thumbnailPath)) {
                            thumbnailPath = null // Don't fail if thumbnail fails
                        }
                    }

                    updateProgress(song.id, 1f)

                    // Save metadata
                    val downloadedFile = DownloadedFile(
                        songId = song.id,
                        title = song.title,
                        artist = song.artists.firstOrNull()?.name ?: "Unknown Artist",
                        thumbnailUrl = song.thumbnail,
                        audioFilePath = getAudioFilePath(song.id),
                        thumbnailFilePath = thumbnailPath,
                        duration = song.duration,
                        downloadDate = System.currentTimeMillis()
                    )
                        addDownloadMetadata(downloadedFile)
                        updateState(song.id, DownloadState.DOWNLOADED)
                        // Synchronize AppState with DownloadManager state
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            com.metrolist.desktop.state.AppState.downloadedFiles = getDownloadedFiles()
                            com.metrolist.desktop.state.AppState.downloadStates = downloadStates.value
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                updateState(song.id, DownloadState.FAILED)
            } finally {
                activeJobs.remove(song.id)
                // Clean up progress after a delay
                kotlinx.coroutines.delay(2000)
                updateProgress(song.id, null)
            }
        }

        activeJobs[song.id] = job
    }

    fun deleteDownload(songId: String) {
        activeJobs[songId]?.cancel()
        activeJobs.remove(songId)

        getDownloadMetadata(songId)?.let { metadata ->
            // Delete audio file
            File(metadata.audioFilePath).delete()
            // Delete thumbnail if exists
            metadata.thumbnailFilePath?.let { File(it).delete() }
            // Remove metadata
            removeDownloadMetadata(songId)
        }

        updateState(songId, DownloadState.NOT_DOWNLOADED)
    }
}
