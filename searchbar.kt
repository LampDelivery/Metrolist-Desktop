private fun SearchBarInSidebar(
    colorScheme: ColorScheme,
    searchText: String,
    onSearchChange: (String) -> Unit,
    onSearchFocusChange: (Boolean) -> Unit,
    isSearchFocused: Boolean
) {
    val focusManager = LocalFocusManager.current
    var localSearchFocused by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = searchText,
            onValueChange = onSearchChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colorScheme.onSurface),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .background(colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .onFocusChanged { focusState ->
                    localSearchFocused = focusState.isFocused
                    onSearchFocusChange(focusState.isFocused)
                }
                .onKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp) {
                        AppState.isExpanded = false
                        AppState.addSearchQuery(searchText)
                        AppState.search(searchText)
                        focusManager.clearFocus()
                        true
                    } else false
                },
            singleLine = true,
            cursorBrush = SolidColor(colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                AppState.isExpanded = false
                AppState.addSearchQuery(searchText)
                AppState.search(searchText)
                focusManager.clearFocus()
            }),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (searchText.isEmpty()) {
                            Text(
                                "Search...",
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        innerTextField()
                    }
                    if (searchText.isNotEmpty()) {
                        Icon(
                            Icons.Default.Close,
                            null,
                            modifier = Modifier.size(18.dp).clickable { onSearchChange("") },
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )
    }
}
