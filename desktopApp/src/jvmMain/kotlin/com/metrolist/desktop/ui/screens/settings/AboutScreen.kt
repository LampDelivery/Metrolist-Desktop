package com.metrolist.desktop.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import com.metrolist.desktop.ui.components.AsyncImage
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import java.net.URL
import javax.imageio.ImageIO

@Composable
fun AboutScreen(onBack: () -> Unit = {}) {
    val uriHandler = LocalUriHandler.current
    val donationUrl = "https://github.com/sponsors/LampDelivery"
    val githubUrl = "https://github.com/MetrolistGroup/Metrolist"
    val discordUrl = "https://discord.gg/rJwDxXsf8c"
    val telegramUrl = "https://t.me/metrolistapp"
    val licenseUrl = "https://github.com/MetrolistGroup/Metrolist/blob/main/LICENSE"

    val scrollState = androidx.compose.foundation.rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(scrollState),
    ) {
        Text(
            "About",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                AsyncImage(
                    url = "https://github.com/LampDelivery.png",
                    modifier = Modifier.size(80.dp).clip(CircleShape)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Metrolist Desktop",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Developer: LampDelivery",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "github.com/LampDelivery",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { uriHandler.openUri("https://github.com/LampDelivery") }
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { uriHandler.openUri(donationUrl) },
                    modifier = Modifier.width(220.dp).height(48.dp)
                ) {
                    Text("Donate / Sponsor", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Social Links",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "GitHub",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(githubUrl) }
                    .padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Discord",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(discordUrl) }
                    .padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Telegram",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(telegramUrl) }
                    .padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = "License",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(licenseUrl) }
                    .padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}
