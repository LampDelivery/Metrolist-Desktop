package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState
import com.metrolist.shared.api.lastfm.LastFM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.metrolist.desktop.ui.screens.settings.*

@Composable
fun IntegrationsScreen(colorScheme: ColorScheme) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppState.showIntegrations = false }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Integrations",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp))
        }

        DiscordSection(colorScheme)

        Spacer(Modifier.height(24.dp))

        LastFmSection(colorScheme)
    }
}

@Composable
private fun DiscordSection(colorScheme: ColorScheme) {
    SettingsGroup(title = "Discord Rich Presence", colorScheme = colorScheme) {
        SettingsToggle(
            title = "Enable Rich Presence",
            subtitle = "Share your current music on Discord",
            checked = AppState.discordRpcEnabled,
            onCheckedChange = { AppState.toggleDiscordRpc(it) },
            colorScheme = colorScheme
        )

        if (AppState.discordRpcEnabled) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = colorScheme.outlineVariant.copy(alpha = 0.5f))

            // App ID
            var appIdInput by remember { mutableStateOf(AppState.discordRpcAppId) }
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("App ID", style = MaterialTheme.typography.labelLarge, color = colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = appIdInput,
                        onValueChange = {
                            appIdInput = it
                            AppState.updateDiscordRpcAppId(it)
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Leave empty for default (Metrolist)") },
                        singleLine = true
                    )
                    Button(onClick = { AppState.applyDiscordRpcAppId() }) {
                        Text("Apply")
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Determines the activity name shown in Discord. Create your own app at discord.com/developers/applications.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Activity Type
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("Activity Type", style = MaterialTheme.typography.labelLarge, color = colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                val activityTypes = listOf(
                    "LISTENING" to "Listening",
                    "PLAYING" to "Playing",
                    "WATCHING" to "Watching",
                    "COMPETING" to "Competing"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    activityTypes.forEach { (key, label) ->
                        FilterChip(
                            selected = AppState.discordRpcActivityType == key,
                            onClick = { AppState.updateDiscordRpcActivityType(key) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = colorScheme.outlineVariant.copy(alpha = 0.5f))

            SettingsToggle(
                title = "Show when paused",
                subtitle = "Keep your status visible even when music is not playing",
                checked = AppState.discordRpcShowIdle,
                onCheckedChange = { AppState.toggleDiscordRpcShowIdle(it) },
                colorScheme = colorScheme
            )

            SettingsToggle(
                title = "Show Details",
                subtitle = "Show song title as primary status",
                checked = AppState.discordRpcUseDetails,
                onCheckedChange = { AppState.toggleDiscordRpcUseDetails(it) },
                colorScheme = colorScheme
            )

            SettingsToggle(
                title = "Show Buttons",
                subtitle = "Show clickable buttons in your Discord profile",
                checked = AppState.discordRpcShowButtons,
                onCheckedChange = { AppState.toggleDiscordRpcShowButtons(it) },
                colorScheme = colorScheme
            )

            if (AppState.discordRpcShowButtons) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Button 1
                DiscordButtonConfig(
                    number = 1,
                    visible = AppState.discordRpcButton1Visible,
                    onVisibleChange = { AppState.toggleDiscordRpcButton1Visible(it) },
                    label = AppState.discordRpcButton1Text,
                    onLabelChange = { AppState.updateDiscordRpcButton1Text(it) },
                    url = AppState.discordRpcButton1Url,
                    onUrlChange = { AppState.updateDiscordRpcButton1Url(it) },
                    colorScheme = colorScheme
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Button 2
                DiscordButtonConfig(
                    number = 2,
                    visible = AppState.discordRpcButton2Visible,
                    onVisibleChange = { AppState.toggleDiscordRpcButton2Visible(it) },
                    label = AppState.discordRpcButton2Text,
                    onLabelChange = { AppState.updateDiscordRpcButton2Text(it) },
                    url = AppState.discordRpcButton2Url,
                    onUrlChange = { AppState.updateDiscordRpcButton2Url(it) },
                    colorScheme = colorScheme
                )

                // Variables hint
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Available variables:",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "{song_name}  •  {artist_name}  •  {album_name}  •  {video_id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscordButtonConfig(
    number: Int,
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    label: String,
    onLabelChange: (String) -> Unit,
    url: String,
    onUrlChange: (String) -> Unit,
    colorScheme: ColorScheme
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Button $number",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.primary
            )
            Switch(
                checked = visible,
                onCheckedChange = onVisibleChange,
                thumbContent = {
                    Icon(
                        imageVector = if (visible) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                }
            )
        }

        if (visible) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = label,
                onValueChange = onLabelChange,
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                label = { Text("URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
fun LastFmSection(colorScheme: ColorScheme) {
    val scope = rememberCoroutineScope()
    var showLoginDialog by remember { mutableStateOf(false) }
    var lastfmUsername by remember { mutableStateOf(AppState.prefs.get("LASTFM_USERNAME", "")) }
    var lastfmSession by remember { mutableStateOf(AppState.prefs.get("LASTFM_SESSION", "")) }
    val isLoggedIn = lastfmSession.isNotEmpty()

    SettingsGroup(title = "Last.fm", colorScheme = colorScheme) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (isLoggedIn) "Logged in as $lastfmUsername" else "Not logged in",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Scrobble your music to Last.fm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }

            if (isLoggedIn) {
                OutlinedButton(onClick = {
                    lastfmSession = ""
                    lastfmUsername = ""
                    AppState.prefs.remove("LASTFM_SESSION")
                    AppState.prefs.remove("LASTFM_USERNAME")
                    AppState.prefs.flush()
                    LastFM.sessionKey = null
                }) {
                    Text("Logout")
                }
            } else {
                Button(onClick = { showLoginDialog = true }) {
                    Text("Login")
                }
            }
        }

        if (isLoggedIn) {
            var scrobblingEnabled by remember { mutableStateOf(AppState.prefs.getBoolean("LASTFM_ENABLED", true)) }
            var useNowPlaying by remember { mutableStateOf(AppState.prefs.getBoolean("LASTFM_USE_NOW_PLAYING", true)) }
            var scrobbleDelayPercent by remember { mutableStateOf(AppState.prefs.getFloat("LASTFM_DELAY_PERCENT", 0.5f)) }
            var scrobbleMinDuration by remember { mutableStateOf(AppState.prefs.getInt("LASTFM_MIN_DURATION", 30)) }
            var scrobbleMaxDelay by remember { mutableStateOf(AppState.prefs.getInt("LASTFM_MAX_DELAY", 180)) }

            SettingsToggle(
                title = "Enable Scrobbling",
                subtitle = "Automatically scrobble tracks",
                checked = scrobblingEnabled,
                onCheckedChange = {
                    scrobblingEnabled = it
                    AppState.prefs.putBoolean("LASTFM_ENABLED", it)
                    AppState.prefs.flush()
                },
                colorScheme = colorScheme
            )

            SettingsToggle(
                title = "Send Now Playing",
                subtitle = "Update your Last.fm status while listening",
                checked = useNowPlaying,
                onCheckedChange = {
                    useNowPlaying = it
                    AppState.prefs.putBoolean("LASTFM_USE_NOW_PLAYING", it)
                    AppState.prefs.flush()
                },
                colorScheme = colorScheme
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = colorScheme.outlineVariant.copy(alpha = 0.5f))

            Column(Modifier.padding(16.dp)) {
                Text("Scrobble Sensitivity", style = MaterialTheme.typography.labelLarge, color = colorScheme.primary)
                Spacer(Modifier.height(8.dp))

                Text("Percentage: ${(scrobbleDelayPercent * 100).roundToInt()}%", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = scrobbleDelayPercent,
                    onValueChange = {
                        scrobbleDelayPercent = it
                        AppState.prefs.putFloat("LASTFM_DELAY_PERCENT", it)
                    },
                    valueRange = 0.3f..0.95f,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Minimum Track Duration: ${scrobbleMinDuration}s", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = scrobbleMinDuration.toFloat(),
                    onValueChange = {
                        scrobbleMinDuration = it.toInt()
                        AppState.prefs.putInt("LASTFM_MIN_DURATION", it.toInt())
                    },
                    valueRange = 10f..60f,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Maximum Delay: ${scrobbleMaxDelay}s", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = scrobbleMaxDelay.toFloat(),
                    onValueChange = {
                        scrobbleMaxDelay = it.toInt()
                        AppState.prefs.putInt("LASTFM_MAX_DELAY", it.toInt())
                    },
                    valueRange = 30f..360f,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { AppState.prefs.flush() },
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text("Save Config", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    if (showLoginDialog) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoggingIn by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var passwordVisible by rememberSaveable { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isLoggingIn) showLoginDialog = false },
            title = { Text("Login to Last.fm") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        }
                    )
                    errorMsg?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (isLoggingIn) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isLoggingIn && username.isNotBlank() && password.isNotEmpty(),
                    onClick = {
                        isLoggingIn = true
                        errorMsg = null
                        scope.launch(Dispatchers.IO) {
                            LastFM.getMobileSession(AppState.client, username, password)
                                .onSuccess { resp ->
                                    val session = resp.session
                                    if (session != null) {
                                        lastfmSession = session.key
                                        lastfmUsername = session.name
                                        AppState.prefs.put("LASTFM_SESSION", lastfmSession)
                                        AppState.prefs.put("LASTFM_USERNAME", lastfmUsername)
                                        AppState.prefs.flush()
                                        LastFM.sessionKey = lastfmSession
                                        scope.launch(Dispatchers.Main) {
                                            showLoginDialog = false
                                            isLoggingIn = false
                                        }
                                    } else {
                                        scope.launch(Dispatchers.Main) {
                                            errorMsg = "Invalid session response"
                                            isLoggingIn = false
                                        }
                                    }
                                }
                                .onFailure { e ->
                                    scope.launch(Dispatchers.Main) {
                                        errorMsg = e.message ?: "Login failed"
                                        isLoggingIn = false
                                    }
                                }
                        }
                    }
                ) {
                    Text("Login")
                }
            },
            dismissButton = {
                TextButton(enabled = !isLoggingIn, onClick = { showLoginDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
