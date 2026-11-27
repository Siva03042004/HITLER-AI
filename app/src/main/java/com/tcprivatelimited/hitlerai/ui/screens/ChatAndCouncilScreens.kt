package com.tcprivatelimited.hitlerai.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ViewTimeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tcprivatelimited.hitlerai.R
import com.tcprivatelimited.hitlerai.data.local.CouncilSessionEntity
import com.tcprivatelimited.hitlerai.data.model.AiSpecialist
import com.tcprivatelimited.hitlerai.data.model.ChatMessage
import com.tcprivatelimited.hitlerai.data.model.WorkspaceCatalog
import com.tcprivatelimited.hitlerai.data.repository.CouncilProgress
import com.tcprivatelimited.hitlerai.ui.DataUiState
import com.tcprivatelimited.hitlerai.ui.theme.BrandInkDark
import com.tcprivatelimited.hitlerai.ui.theme.LocalWorkspacePreset
import com.tcprivatelimited.hitlerai.ui.theme.LogoCoral
import com.tcprivatelimited.hitlerai.ui.theme.LogoViolet
import com.tcprivatelimited.hitlerai.ui.theme.SerifGreetingFontFamily
import com.tcprivatelimited.hitlerai.ui.theme.StatusGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ChatWorkspaceScreen(
    messagesState: DataUiState<List<ChatMessage>>,
    isTemporaryChat: Boolean,
    temporaryMessages: List<ChatMessage>,
    activeSpecialist: AiSpecialist?,
    isSending: Boolean,
    streamingReply: String,
    chatError: String?,
    onGoToCouncil: () -> Unit,
    onSelectQuickPrompt: (String) -> Unit,
    onSelectSpecialist: (AiSpecialist?) -> Unit,
    onClearSpecialist: () -> Unit,
    onSaveMessageToLibrary: (title: String, content: String) -> Unit
) {
    val messages = if (isTemporaryChat) {
        temporaryMessages
    } else {
        (messagesState as? DataUiState.Success)?.data.orEmpty()
    }
    val hasMessages = messages.isNotEmpty() || isSending || streamingReply.isNotBlank()

    if (!hasMessages) {
        if (isTemporaryChat) {
            TemporaryChatEmptyState(
                activeSpecialist = activeSpecialist,
                chatError = chatError,
                onSelectSpecialist = onSelectSpecialist
            )
        } else {
            MinimalGreetingHero(
                activeSpecialist = activeSpecialist,
                chatError = chatError,
                onGoToCouncil = onGoToCouncil,
                onSelectQuickPrompt = onSelectQuickPrompt,
                onClearSpecialist = onClearSpecialist
            )
        }
    } else {
        ConversationMessagesContent(
            messages = messages,
            isTemporaryChat = isTemporaryChat,
            activeSpecialist = activeSpecialist,
            isSending = isSending,
            streamingReply = streamingReply,
            chatError = chatError,
            onClearSpecialist = onClearSpecialist,
            onSaveMessageToLibrary = onSaveMessageToLibrary
        )
    }
}

@Composable
private fun MinimalGreetingHero(
    activeSpecialist: AiSpecialist?,
    chatError: String?,
    onGoToCouncil: () -> Unit,
    onSelectQuickPrompt: (String) -> Unit,
    onClearSpecialist: () -> Unit
) {
    val preset = LocalWorkspacePreset.current
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingText = when (hour) {
        in 5..11 -> stringResource(R.string.greeting_morning)
        in 12..16 -> stringResource(R.string.greeting_afternoon)
        in 17..21 -> stringResource(R.string.greeting_evening)
        else -> stringResource(R.string.greeting_late)
    }

    val quickIcons = listOf(
        Icons.Outlined.Search,
        Icons.Outlined.ViewTimeline,
        Icons.Outlined.Edit,
        Icons.Outlined.Insights
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Centered glowing brand logo (Screenshot 3 composition)
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = stringResource(R.string.cd_app_logo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(LogoViolet.copy(alpha = 0.55f), LogoCoral.copy(alpha = 0.55f))
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
            )

            // Editorial serif greeting in Newsreader
            Text(
                text = greetingText,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = SerifGreetingFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 32.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            // Subtle rounded pill below greeting (matches Screenshot 3 pill)
            Surface(
                shape = CircleShape,
                color = preset.pillContainerColor.copy(alpha = 0.85f),
                modifier = Modifier
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = preset.borderColor.copy(alpha = 0.65f),
                        shape = CircleShape
                    )
                    .clickable(onClick = onGoToCouncil)
                    .testTag("meet_team_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.greeting_pill_prefix),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.greeting_pill_action),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (activeSpecialist != null) {
                ActiveSpecialistBanner(
                    specialist = activeSpecialist,
                    onClear = onClearSpecialist
                )
            }

            if (!chatError.isNullOrBlank()) {
                ErrorNoticeBanner(message = chatError)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Airy horizontal quick-prompt pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(WorkspaceCatalog.quickPrompts.size) { index ->
                    val item = WorkspaceCatalog.quickPrompts[index]
                    val icon = quickIcons[index.coerceAtMost(quickIcons.lastIndex)]
                    Surface(
                        shape = CircleShape,
                        color = preset.surfaceColor.copy(alpha = 0.72f),
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                color = preset.borderColor.copy(alpha = 0.55f),
                                shape = CircleShape
                            )
                            .clickable { onSelectQuickPrompt(item.promptPrefix) }
                            .testTag("quick_prompt_$index")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemporaryChatEmptyState(
    activeSpecialist: AiSpecialist?,
    chatError: String?,
    onSelectSpecialist: (AiSpecialist?) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(34.dp)
            )

            Text(
                text = stringResource(R.string.temporary_chat_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 21.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.temporary_chat_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Specialist selector matching Screenshot 1 ("Personalized ▾")
            Box {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { menuExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("temp_specialist_picker"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = activeSpecialist?.name ?: "Personalized",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Choose specialist",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Personalized (Default)") },
                        onClick = {
                            onSelectSpecialist(null)
                            menuExpanded = false
                        }
                    )
                    WorkspaceCatalog.specialists.forEach { specialist ->
                        DropdownMenuItem(
                            text = { Text("${specialist.name} · ${specialist.role}") },
                            onClick = {
                                onSelectSpecialist(specialist)
                                menuExpanded = false
                            }
                        )
                    }
                }
            }

            if (!chatError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                ErrorNoticeBanner(message = chatError)
            }
        }
    }
}

@Composable
private fun ConversationMessagesContent(
    messages: List<ChatMessage>,
    isTemporaryChat: Boolean,
    activeSpecialist: AiSpecialist?,
    isSending: Boolean,
    streamingReply: String,
    chatError: String?,
    onClearSpecialist: () -> Unit,
    onSaveMessageToLibrary: (title: String, content: String) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, streamingReply.length) {
        val total = messages.size + if (isSending || streamingReply.isNotBlank()) 1 else 0
        if (total > 0) {
            listState.animateScrollToItem(total)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isTemporaryChat) {
            item(key = "temp_notice") {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            CircleShape
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = stringResource(R.string.temporary_chat_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (activeSpecialist != null) {
            item(key = "specialist_banner") {
                ActiveSpecialistBanner(
                    specialist = activeSpecialist,
                    onClear = onClearSpecialist
                )
            }
        }

        items(messages, key = { it.id.ifBlank { "${it.role}_${it.content.hashCode()}" } }) { msg ->
            ChatBubbleRow(
                isUser = msg.role == "user",
                senderLabel = msg.senderLabel,
                content = msg.content,
                onSaveToLibrary = if (msg.role == "assistant") {
                    { onSaveMessageToLibrary(msg.content.take(60), msg.content) }
                } else null
            )
        }

        if (isSending || streamingReply.isNotBlank()) {
            item(key = "streaming_bubble") {
                ChatBubbleRow(
                    isUser = false,
                    senderLabel = activeSpecialist?.name?.uppercase() ?: "HITLER AI",
                    content = streamingReply.ifBlank { "Thinking…" },
                    isStreaming = true,
                    onSaveToLibrary = null
                )
            }
        }

        if (!chatError.isNullOrBlank()) {
            item(key = "chat_error") {
                ErrorNoticeBanner(message = chatError)
            }
        }
    }
}

@Composable
private fun ChatBubbleRow(
    isUser: Boolean,
    senderLabel: String,
    content: String,
    isStreaming: Boolean = false,
    onSaveToLibrary: (() -> Unit)?
) {
    val preset = LocalWorkspacePreset.current

    if (isUser) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 6.dp
                ),
                color = preset.elevatedSurfaceColor.copy(alpha = 0.92f),
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .border(
                        width = 1.dp,
                        color = preset.borderColor.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 6.dp
                        )
                    )
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = stringResource(R.string.cd_app_logo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(0.75.dp, LogoViolet.copy(alpha = 0.45f), CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 580.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = senderLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (isStreaming) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Generating with Gemini…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (onSaveToLibrary != null) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(
                            onClick = onSaveToLibrary,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("save_reply_library_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = stringResource(R.string.cd_save_to_library),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CouncilScreen(
    liveProgress: CouncilProgress?,
    selectedSession: CouncilSessionEntity?,
    isRunning: Boolean,
    councilError: String?,
    searchQuery: String,
    localSessions: List<CouncilSessionEntity>,
    onSearchQueryChange: (String) -> Unit,
    onSelectSession: (CouncilSessionEntity) -> Unit,
    onStartNewSession: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    onSaveSynthesisToLibrary: (title: String, content: String) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()) }
    val activeTurns: Map<String, String>? = when {
        liveProgress != null -> liveProgress.turns
        selectedSession != null -> selectedSession.turnsMap()
        else -> null
    }
    val activePrompt: String? = liveProgress?.prompt ?: selectedSession?.prompt
    val activeSynthesis: String? = when {
        liveProgress != null -> liveProgress.synthesis
        selectedSession != null -> selectedSession.synthesis
        else -> null
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.council_eyebrow),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.council_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.council_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = StatusGreen.copy(alpha = 0.14f),
                    modifier = Modifier.border(
                        1.dp,
                        StatusGreen.copy(alpha = 0.45f),
                        RoundedCornerShape(20.dp)
                    )
                ) {
                    Text(
                        text = if (isRunning) "● RUNNING" else "● READY",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(WorkspaceCatalog.councilOrder, key = { it.name }) { specialist ->
                    val isSpeaking = liveProgress?.activeSpecialist == specialist.name && isRunning
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                        modifier = Modifier
                            .width(134.dp)
                            .border(
                                width = if (isSpeaking) 1.5.dp else 1.dp,
                                color = if (isSpeaking) specialist.accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                                shape = MaterialTheme.shapes.medium
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(specialist.accentColor.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = specialist.letter,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = specialist.accentColor
                                )
                            }
                            Text(
                                text = specialist.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = specialist.role,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        if (!councilError.isNullOrBlank()) {
            item {
                ErrorNoticeBanner(message = councilError)
            }
        }

        if (activePrompt != null && activeTurns != null) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val label = if (selectedSession != null && !isRunning) {
                                "${stringResource(R.string.council_revisiting_banner)} · ${dateFormatter.format(Date(selectedSession.createdAtMillis))}"
                            } else {
                                "ACTIVE COUNCIL SESSION"
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = activePrompt,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (!isRunning) {
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = onStartNewSession,
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("council_new_session_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.council_action_new_session))
                            }
                        }
                    }
                }
            }

            items(WorkspaceCatalog.councilOrder, key = { "active_${it.name}" }) { specialist ->
                val turnText = activeTurns[specialist.name]?.takeIf { it.isNotBlank() }
                val isActive = isRunning && liveProgress?.activeSpecialist == specialist.name
                if (turnText != null || isActive) {
                    CouncilTurnCard(
                        agentName = specialist.name,
                        accentColor = specialist.accentColor,
                        content = turnText ?: "Analyzing from the ${specialist.role} perspective…",
                        isLoading = isActive && turnText == null
                    )
                }
            }

            val showSynthesis = !activeSynthesis.isNullOrBlank() ||
                (isRunning && liveProgress?.activeSpecialist == "Head")
            if (showSynthesis) {
                item(key = "active_synthesis") {
                    val synthesisText = activeSynthesis.orEmpty()
                    CouncilSynthesisCard(
                        synthesis = synthesisText.ifBlank { "Synthesizing all five specialist perspectives…" },
                        isLoading = isRunning && synthesisText.isBlank(),
                        onSave = if (synthesisText.isNotBlank()) {
                            {
                                onSaveSynthesisToLibrary(
                                    "Council: ${activePrompt.take(50)}",
                                    synthesisText
                                )
                            }
                        } else null
                    )
                }
            }
        } else if (localSessions.isEmpty() && searchQuery.isBlank()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), MaterialTheme.shapes.medium)
                ) {
                    Text(
                        text = stringResource(R.string.council_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }

        if (localSessions.isNotEmpty() || searchQuery.isNotBlank()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.council_history_heading),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(stringResource(R.string.council_history_search_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("council_history_search_input")
                )
            }

            if (localSessions.isEmpty()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                    ) {
                        Text(
                            text = stringResource(R.string.council_history_no_matches),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                items(localSessions, key = { "local_${it.id}" }) { session ->
                    val isSelected = selectedSession?.id == session.id && !isRunning
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable { onSelectSession(session) }
                            .testTag("council_history_item_${session.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateFormatter.format(Date(session.createdAtMillis)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                IconButton(
                                    onClick = { onDeleteSession(session.id) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("delete_council_session_${session.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = stringResource(R.string.cd_delete_item),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = session.prompt,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = session.synthesis,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.council_action_revisit),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                OutlinedButton(
                                    onClick = {
                                        onSaveSynthesisToLibrary(
                                            "Council: ${session.prompt.take(50)}",
                                            session.synthesis
                                        )
                                    },
                                    modifier = Modifier.height(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.BookmarkBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.action_save_library))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CouncilTurnCard(
    agentName: String,
    accentColor: Color,
    content: String,
    isLoading: Boolean
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), MaterialTheme.shapes.medium)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = agentName.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = accentColor
                    )
                }
            }
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CouncilSynthesisCard(
    synthesis: String,
    isLoading: Boolean,
    onSave: (() -> Unit)?
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "HEAD · COUNCIL SYNTHESIS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (onSave != null) {
                    IconButton(
                        onClick = onSave,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("save_council_synthesis_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(R.string.cd_save_to_library),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Text(
                text = synthesis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ActiveSpecialistBanner(
    specialist: AiSpecialist,
    onClear: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = specialist.accentColor.copy(alpha = 0.14f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, specialist.accentColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(specialist.accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = specialist.letter,
                        style = MaterialTheme.typography.labelLarge,
                        color = BrandInkDark
                    )
                }
                Column {
                    Text(
                        text = "Specialist active: ${specialist.name}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = specialist.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("clear_specialist_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Clear active specialist",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ErrorNoticeBanner(message: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f), MaterialTheme.shapes.small)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(12.dp)
        )
    }
}
