package com.metrolist.android

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metrolist.shared.model.HomePageData
import com.metrolist.shared.state.GlobalYouTubeRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _homePageData = mutableStateOf(HomePageData())
    val homePageData: State<HomePageData> = _homePageData

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

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
}
