package com.metrolist.music.db

import com.metrolist.music.db.entities.Song
import com.metrolist.music.db.entities.Album
import com.metrolist.music.db.entities.Artist
import com.metrolist.music.db.entities.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Stub MusicDatabase class
 * TODO: Implement actual database in KMP base
 */
abstract class MusicDatabase {
    
    // Stub implementations that return empty flows
    open fun songs(): Flow<List<Song>> = flowOf(emptyList())
    open fun song(id: String): Flow<Song?> = flowOf(null)
    
    open fun albums(): Flow<List<Album>> = flowOf(emptyList())
    open fun album(id: String): Flow<Album?> = flowOf(null)
    
    open fun artists(): Flow<List<Artist>> = flowOf(emptyList())
    open fun artist(id: String): Flow<Artist?> = flowOf(null)
    
    open fun playlists(): Flow<List<Playlist>> = flowOf(emptyList())
    open fun playlist(id: String): Flow<Playlist?> = flowOf(null)
    open fun playlistByBrowseId(browseId: String): Flow<Playlist?> = flowOf(null)
    
    open fun searchSongs(query: String): Flow<List<Song>> = flowOf(emptyList())
    open fun searchAlbums(query: String): Flow<List<Album>> = flowOf(emptyList())
    open fun searchArtists(query: String): Flow<List<Artist>> = flowOf(emptyList())
    open fun searchPlaylists(query: String): Flow<List<Playlist>> = flowOf(emptyList())
    
    open suspend fun insert(song: com.metrolist.music.db.entities.SongEntity) {}
    open suspend fun insert(album: com.metrolist.music.db.entities.AlbumEntity) {}
    open suspend fun insert(artist: com.metrolist.music.db.entities.ArtistEntity) {}
    open suspend fun insert(playlist: com.metrolist.music.db.entities.PlaylistEntity) {}
    
    open suspend fun update(song: com.metrolist.music.db.entities.SongEntity) {}
    open suspend fun update(album: com.metrolist.music.db.entities.AlbumEntity) {}
    open suspend fun update(artist: com.metrolist.music.db.entities.ArtistEntity) {}
    open suspend fun update(playlist: com.metrolist.music.db.entities.PlaylistEntity) {}
    
    open suspend fun delete(song: com.metrolist.music.db.entities.SongEntity) {}
    open suspend fun delete(album: com.metrolist.music.db.entities.AlbumEntity) {}
    open suspend fun delete(artist: com.metrolist.music.db.entities.ArtistEntity) {}
    open suspend fun delete(playlist: com.metrolist.music.db.entities.PlaylistEntity) {}
    
    open suspend fun <R> transaction(block: suspend MusicDatabase.() -> R): R = block()
    
    open fun getLifetimePlayCount(id: String): Flow<Int> = flowOf(0)
}
