package com.metrolist.desktop.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javax.swing.BoxLayout
import javax.swing.JPanel
import java.net.CookieHandler
import java.net.CookieManager
import java.net.URI

@Composable
fun EmbeddedSignInView(
    onAuthDataExtracted: (cookie: String, visitorData: String, dataSyncId: String) -> Unit, 
    modifier: Modifier = Modifier
) {
    SwingPanel(
        modifier = modifier,
        factory = {
            // Always install a fresh CookieManager before JavaFX starts so that
            // WebEngine requests go through our store (captures HttpOnly cookies too).
            val cookieManager = CookieManager()
            cookieManager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL)
            CookieHandler.setDefault(cookieManager)

            val jfxPanel = JFXPanel()
            val container = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(jfxPanel)
            }

            Platform.runLater {
                val webView = WebView()
                val engine: WebEngine = webView.engine
                
                // annoying crap (thanks google)
                engine.userAgent = "Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/122.0.6261.89 Mobile/15E148 Safari/604.1"
                
                engine.load("https://accounts.google.com/ServiceLogin?continue=https%3A%2F%2Fmusic.youtube.com")

                engine.loadWorker.stateProperty().addListener { _, _, newState ->
                    if (newState == Worker.State.SUCCEEDED) {
                        val url = engine.location
                        if (url.contains("music.youtube.com")) {
                            try {
                                val visitorData = engine.executeScript("window.yt.config_.VISITOR_DATA") as? String
                                val dataSyncId = engine.executeScript("window.yt.config_.DATASYNC_ID") as? String

                                val uri = URI.create("https://music.youtube.com/")
                                val cookiesMap = cookieManager.get(uri, emptyMap())
                                val cookieStr = cookiesMap["Cookie"]?.joinToString("; ")
                                    ?: engine.executeScript("document.cookie") as? String

                                if (!cookieStr.isNullOrBlank() && !visitorData.isNullOrBlank() && !dataSyncId.isNullOrBlank()) {
                                    onAuthDataExtracted(
                                        cookieStr, 
                                        visitorData, 
                                        dataSyncId.substringBefore("||")
                                    )
                                }
                            } catch (e: Exception) {
                                // might fail on some pages, ignore
                            }
                        }
                    }
                }

                jfxPanel.scene = Scene(webView)
            }
            container
        }
    )
}
