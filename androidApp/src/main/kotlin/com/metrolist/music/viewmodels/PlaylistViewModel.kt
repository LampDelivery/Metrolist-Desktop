package com.metrolist.music.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metrolist.shared.model.PlaylistItem
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.model.YTItem
import com.metrolist.shared.state.GlobalYouTubeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"])

    private val _playlistInfo = mutableStateOf<PlaylistItem?>(null)
    val playlistInfo: State<PlaylistItem?> = _playlistInfo

    private val _songs = mutableStateOf<List<SongItem>>(emptyList())
    val songs: State<List<SongItem>> = _songs

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        loadPlaylist()
    }

    fun loadPlaylist() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val data = GlobalYouTubeRepository.instance.getPlaylist(playlistId)
                
                // Extract playlist info from header
                _playlistInfo.value = data["header"]
                    ?.filterIsInstance<PlaylistItem>()
                    ?.firstOrNull()
                
                // Extract songs from Tracks section or any section containing songs
                val allItems = data.values.flatten()
                _songs.value = allItems.filterIsInstance<SongItem>()
            } catch (t: Throwable) {
                _errorMessage.value = t.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
