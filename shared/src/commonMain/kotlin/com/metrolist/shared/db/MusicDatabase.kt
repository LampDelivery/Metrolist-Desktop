/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.shared.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.metrolist.shared.db.daos.DatabaseDao
import com.metrolist.shared.db.daos.SpeedDialDao
import com.metrolist.shared.db.entities.AlbumArtistMap
import com.metrolist.shared.db.entities.AlbumEntity
import com.metrolist.shared.db.entities.ArtistEntity
import com.metrolist.shared.db.entities.Event
import com.metrolist.shared.db.entities.FormatEntity
import com.metrolist.shared.db.entities.LyricsEntity
import com.metrolist.shared.db.entities.PlayCountEntity
import com.metrolist.shared.db.entities.PlaylistEntity
import com.metrolist.shared.db.entities.PlaylistSongMap
import com.metrolist.shared.db.entities.PlaylistSongMapPreview
import com.metrolist.shared.db.entities.PodcastEntity
import com.metrolist.shared.db.entities.RecognitionHistory
import com.metrolist.shared.db.entities.RelatedSongMap
import com.metrolist.shared.db.entities.SearchHistory
import com.metrolist.shared.db.entities.SetVideoIdEntity
import com.metrolist.shared.db.entities.SongAlbumMap
import com.metrolist.shared.db.entities.SongArtistMap
import com.metrolist.shared.db.entities.SongEntity
import com.metrolist.shared.db.entities.SortedSongAlbumMap
import com.metrolist.shared.db.entities.SortedSongArtistMap
import com.metrolist.shared.db.entities.SpeedDialItem

const val DATABASE_VERSION = 36

@Database(
    entities = [
        SongEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        PlaylistEntity::class,
        SongArtistMap::class,
        SongAlbumMap::class,
        AlbumArtistMap::class,
        PlaylistSongMap::class,
        SearchHistory::class,
        FormatEntity::class,
        LyricsEntity::class,
        Event::class,
        RelatedSongMap::class,
        SetVideoIdEntity::class,
        PlayCountEntity::class,
        RecognitionHistory::class,
        SpeedDialItem::class,
        PodcastEntity::class
    ],
    views = [
        SortedSongArtistMap::class,
        SortedSongAlbumMap::class,
        PlaylistSongMapPreview::class,
    ],
    version = DATABASE_VERSION,
    exportSchema = true
)
@TypeConverters(Converters::class)
@ConstructedBy(MusicDatabaseConstructor::class)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun dao(): DatabaseDao
    abstract fun speedDialDao(): SpeedDialDao
}

// Expect factory for the database
expect fun getDatabaseBuilder(context: Any? = null): RoomDatabase.Builder<MusicDatabase>

// Database constructor for Room multiplatform
expect object MusicDatabaseConstructor : RoomDatabaseConstructor<MusicDatabase>
