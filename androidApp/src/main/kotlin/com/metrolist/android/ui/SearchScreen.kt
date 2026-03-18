package com.metrolist.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.metrolist.shared.model.YTItem
import com.metrolist.shared.state.GlobalYouTubeRepository
import kotlinx.coroutines.launch

@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<YTItem>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search music") },
            singleLine = true,
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = androidx.compose.ui.text.input.KeyboardActions(onSearch = {
                if (query.isBlank()) return@KeyboardActions
                scope.launch {
                    isLoading = true
                    error = null
                    try {
                        results = GlobalYouTubeRepository.instance.search(query).items
                    } catch (t: Throwable) {
                        error = t.message ?: "Unknown error"
                    } finally {
                        isLoading = false
                    }
                }
            })
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    results.forEach { item ->
                        Text(item.title, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}
