package com.metrolist.desktop.utils

import com.metrolist.shared.model.SongItem
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

data class HistoryEntry(
    val songId: String,
    val title: String,
    val artists: String,
    val thumbnailUrl: String?,
    val playedAt: Long  // epoch millis
)

data class HistoryStat(
    val songId: String,
    val title: String,
    val artists: String,
    val thumbnailUrl: String?,
    val playCount: Int,
    val lastPlayedAt: Long
)

data class ArtistStat(
    val name: String,
    val playCount: Int
)

object HistoryRepository {
    private val dbFile: File by lazy {
        val dir = File(System.getProperty("user.home"), ".metrolist")
        dir.mkdirs()
        File(dir, "history.db")
    }

    private val connection: Connection by lazy {
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}").also { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS play_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        song_id TEXT NOT NULL,
                        title TEXT NOT NULL,
                        artists TEXT NOT NULL,
                        thumbnail_url TEXT,
                        played_at INTEGER NOT NULL
                    )
                """.trimIndent())
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_played_at ON play_history(played_at)")
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_song_id   ON play_history(song_id)")
            }
        }
    }

    fun recordPlay(song: SongItem) {
        try {
            connection.prepareStatement(
                "INSERT INTO play_history(song_id, title, artists, thumbnail_url, played_at) VALUES (?, ?, ?, ?, ?)"
            ).use { stmt ->
                stmt.setString(1, song.id)
                stmt.setString(2, song.title)
                stmt.setString(3, song.artists.joinToString(", ") { it.name })
                stmt.setString(4, song.thumbnail)
                stmt.setLong(5, System.currentTimeMillis())
                stmt.executeUpdate()
            }
            // Keep only the most recent 2000 entries
            connection.createStatement().use { stmt ->
                stmt.execute(
                    "DELETE FROM play_history WHERE id NOT IN (SELECT id FROM play_history ORDER BY played_at DESC LIMIT 2000)"
                )
            }
        } catch (_: Exception) {}
    }

    fun getRecentHistory(limit: Int = 50): List<HistoryEntry> = try {
        connection.prepareStatement(
            "SELECT song_id, title, artists, thumbnail_url, played_at FROM play_history ORDER BY played_at DESC LIMIT ?"
        ).use { stmt ->
            stmt.setInt(1, limit)
            val rs = stmt.executeQuery()
            buildList {
                while (rs.next()) add(
                    HistoryEntry(
                        songId      = rs.getString("song_id"),
                        title       = rs.getString("title"),
                        artists     = rs.getString("artists"),
                        thumbnailUrl = rs.getString("thumbnail_url"),
                        playedAt    = rs.getLong("played_at")
                    )
                )
            }
        }
    } catch (_: Exception) { emptyList() }

    fun getMostPlayed(limit: Int = 20, sinceMs: Long = 0L): List<HistoryStat> = try {
        connection.prepareStatement("""
            SELECT song_id, title, artists, thumbnail_url,
                   COUNT(*) AS play_count, MAX(played_at) AS last_played
            FROM play_history
            WHERE played_at >= ?
            GROUP BY song_id
            ORDER BY play_count DESC, last_played DESC
            LIMIT ?
        """.trimIndent()).use { stmt ->
            stmt.setLong(1, sinceMs)
            stmt.setInt(2, limit)
            val rs = stmt.executeQuery()
            buildList {
                while (rs.next()) add(
                    HistoryStat(
                        songId      = rs.getString("song_id"),
                        title       = rs.getString("title"),
                        artists     = rs.getString("artists"),
                        thumbnailUrl = rs.getString("thumbnail_url"),
                        playCount   = rs.getInt("play_count"),
                        lastPlayedAt = rs.getLong("last_played")
                    )
                )
            }
        }
    } catch (_: Exception) { emptyList() }

    fun getArtistStats(limit: Int = 20, sinceMs: Long = 0L): List<ArtistStat> = try {
        val rows = connection.prepareStatement(
            "SELECT artists FROM play_history WHERE played_at >= ? ORDER BY played_at DESC LIMIT 2000"
        ).use { stmt ->
            stmt.setLong(1, sinceMs)
            val rs = stmt.executeQuery()
            buildList { while (rs.next()) add(rs.getString("artists")) }
        }
        rows.flatMap { artists -> artists.split(", ").map { it.trim() }.filter { it.isNotBlank() } }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { ArtistStat(it.key, it.value) }
    } catch (_: Exception) { emptyList() }

    fun getRecentHistorySince(sinceMs: Long, limit: Int = 2000): List<HistoryEntry> = try {
        connection.prepareStatement(
            "SELECT song_id, title, artists, thumbnail_url, played_at FROM play_history WHERE played_at >= ? ORDER BY played_at DESC LIMIT ?"
        ).use { stmt ->
            stmt.setLong(1, sinceMs)
            stmt.setInt(2, limit)
            val rs = stmt.executeQuery()
            buildList {
                while (rs.next()) add(
                    HistoryEntry(
                        songId       = rs.getString("song_id"),
                        title        = rs.getString("title"),
                        artists      = rs.getString("artists"),
                        thumbnailUrl = rs.getString("thumbnail_url"),
                        playedAt     = rs.getLong("played_at")
                    )
                )
            }
        }
    } catch (_: Exception) { emptyList() }
}
