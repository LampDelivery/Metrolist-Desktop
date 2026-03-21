package com.metrolist.android

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metrolist.shared.model.Chip
import com.metrolist.shared.model.HomePageData
import com.metrolist.shared.model.HomeSection
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.model.YTItem
import com.metrolist.shared.state.GlobalYouTubeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _homePageData = mutableStateOf(HomePageData())
    val homePageData: State<HomePageData> = _homePageData

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _selectedChip = mutableStateOf<Chip?>(null)
    val selectedChip: State<Chip?> = _selectedChip

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _homePageData.value = GlobalYouTubeRepository.instance.getHomePageData()
            } catch (t: Throwable) {
                _errorMessage.value = t.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleChip(chip: Chip?) {
        if (_selectedChip.value?.title == chip?.title) {
            _selectedChip.value = null
            loadHome()
        } else {
            _selectedChip.value = chip
            chip?.params?.let { params ->
                loadHomeWithParams(params)
            }
        }
    }

    private fun loadHomeWithParams(params: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Fetch home with chip params
                val result = GlobalYouTubeRepository.instance.getHomePageData(params)
                _homePageData.value = result
            } catch (t: Throwable) {
                _errorMessage.value = t.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun shouldShowSection(section: HomeSection): Boolean {
        // Add logic to filter sections based on user preferences
        return section.items.isNotEmpty()
    }

    fun getSectionSongs(section: HomeSection): List<SongItem> {
        return section.items.filterIsInstance<SongItem>()
    }

    fun isSongsOnlySection(section: HomeSection): Boolean {
        return section.items.isNotEmpty() && section.items.all { it is SongItem }
    }
}
