package com.metrolist.desktop.utils

import java.io.File
import java.sql.DriverManager
import java.util.Base64
import com.sun.jna.platform.win32.Crypt32Util

object BrowserCookieReader {
    // Returns the default Chrome profile cookie DB path for Windows
    private fun getChromeCookiePath(): File? {
        val localAppData = System.getenv("LOCALAPPDATA") ?: return null
        val chromePath = File(localAppData, "Google/Chrome/User Data/Default/Cookies")
        return if (chromePath.exists()) chromePath else null
    }

    // Reads and decrypts cookies for music.youtube.com
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

    // Decrypts Chrome cookie value using Windows DPAPI
    private fun decryptChromeCookie(encrypted: ByteArray): String {
        // Chrome prefix is 'v10' or 'v11' for newer cookies
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
