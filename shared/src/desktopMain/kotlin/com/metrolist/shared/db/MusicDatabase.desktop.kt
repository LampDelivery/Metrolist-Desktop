package com.metrolist.shared.db

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<MusicDatabase> {
    val dbFile = File(System.getProperty("user.home"), ".metrolist/metrolist.db")
    dbFile.parentFile.mkdirs() // Ensure the directory exists
    return Room.databaseBuilder<MusicDatabase>(
        name = dbFile.absolutePath,
    )
}