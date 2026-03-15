package com.metrolist.shared.playback

import com.metrolist.shared.model.SongItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicQueue {
    private val _items = MutableStateFlow<List<SongItem>>(emptyList())
    val items = _items.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex = _currentIndex.asStateFlow()

    private var originalItems = emptyList<SongItem>()
    var isShuffled = false
        private set

    fun setQueue(songs: List<SongItem>, startIndex: Int = 0) {
        originalItems = songs
        _items.value = songs
        _currentIndex.value = startIndex
    }

    fun getNext(): SongItem? {
        val nextIdx = _currentIndex.value + 1
        return if (nextIdx < _items.value.size) {
            _currentIndex.value = nextIdx
            _items.value[nextIdx]
        } else null
    }

    fun getPrevious(): SongItem? {
        val prevIdx = _currentIndex.value - 1
        return if (prevIdx >= 0) {
            _currentIndex.value = prevIdx
            _items.value[prevIdx]
        } else null
    }

    fun currentItem(): SongItem? {
        val idx = _currentIndex.value
        return if (idx in _items.value.indices) _items.value[idx] else null
    }

    fun addToQueue(songs: List<SongItem>) {
        _items.value = _items.value + songs
        if (originalItems.isNotEmpty()) originalItems = originalItems + songs
    }

    fun addToQueue(song: SongItem) = addToQueue(listOf(song))

    fun playNext(songs: List<SongItem>) {
        val insertAt = (_currentIndex.value + 1).coerceAtLeast(0)
        val current = _items.value.toMutableList()
        current.addAll(insertAt, songs)
        _items.value = current
    }

    fun playNext(song: SongItem) = playNext(listOf(song))

    fun toggleShuffle() {
        isShuffled = !isShuffled
        if (isShuffled) {
            val current = currentItem()
            val shuffled = originalItems.shuffled().toMutableList()
            if (current != null) {
                shuffled.remove(current)
                shuffled.add(0, current)
            }
            _items.value = shuffled
            _currentIndex.value = 0
        } else {
            val current = currentItem()
            _items.value = originalItems
            _currentIndex.value = if (current != null) originalItems.indexOf(current) else -1
        }
    }
}
