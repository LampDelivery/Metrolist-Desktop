package com.metrolist.desktop.utils

import com.sun.jna.platform.win32.Crypt32Util
import java.io.File
import java.sql.DriverManager

object BrowserCookieReader {
    private fun getChromeCookiePath(): File? {
        val localAppData = System.getenv("LOCALAPPDATA") ?: return null
        val chromePath = File(localAppData, "Google/Chrome/User Data/Default/Cookies")
        return if (chromePath.exists()) chromePath else null
    }

    fun getYouTubeMusicCookies(): Map<String, String> {
        val cookieDb = getChromeCookiePath() ?: return emptyMap()
        val conn = DriverManager.getConnection("jdbc:sqlite:${cookieDb.absolutePath}")
        val stmt = conn.prepareStatement(
            "SELECT name, encrypted_value FROM cookies WHERE host_key LIKE '%music.youtube.com%'"
        )
        val rs = stmt.executeQuery()
        val cookies = mutableMapOf<String, String>()
        while (rs.next()) {
            val name = rs.getString("name")
            val encValue = rs.getBytes("encrypted_value")
            val decValue = decryptChromeCookie(encValue)
            cookies[name] = decValue
        }
        rs.close()
        stmt.close()
        conn.close()
        return cookies
    }

    private fun decryptChromeCookie(encrypted: ByteArray): String {
        return try {
            if (encrypted.size > 0 && (encrypted[0] == 'v'.code.toByte())) {
                val enc = encrypted.copyOfRange(3, encrypted.size)
                String(Crypt32Util.cryptUnprotectData(enc))
            } else {
                String(Crypt32Util.cryptUnprotectData(encrypted))
            }
        } catch (e: Exception) {
            ""
        }
    }
}
