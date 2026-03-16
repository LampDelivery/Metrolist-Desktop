/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.shared.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<MusicDatabase> {
    val appContext = context as? Context ?: throw IllegalArgumentException("Context is required for Android")
    val dbFile = appContext.getDatabasePath("song.db")
    return Room.databaseBuilder<MusicDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
