package com.tcprivatelimited.hitlerai.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tcprivatelimited.hitlerai.R
import com.tcprivatelimited.hitlerai.data.model.Conversation
import com.tcprivatelimited.hitlerai.data.model.WorkspaceCatalog
import com.tcprivatelimited.hitlerai.data.model.WorkspaceDestination
import com.tcprivatelimited.hitlerai.firebase.signOutUser
import com.tcprivatelimited.hitlerai.ui.screens.AgentsScreen
import com.tcprivatelimited.hitlerai.ui.screens.ChatWorkspaceScreen
import com.tcprivatelimited.hitlerai.ui.screens.CouncilScreen
import com.tcprivatelimited.hitlerai.ui.screens.CustomizeScreen
import com.tcprivatelimited.hitlerai.ui.screens.DiscoverScreen
import com.tcprivatelimited.hitlerai.ui.screens.LibraryScreen
import com.tcprivatelimited.hitlerai.ui.screens.MessagesScreen
import com.tcprivatelimited.hitlerai.ui.screens.SocialFeedScreen
import com.tcprivatelimited.hitlerai.ui.screens.WorkStudioScreen
import com.tcprivatelimited.hitlerai.ui.screens.WorkspaceSettingsDialog
import com.tcprivatelimited.hitlerai.ui.theme.HitlerAiTheme
import com.tcprivatelimited.hitlerai.ui.theme.LocalWorkspacePreset
import com.tcprivatelimited.hitlerai.ui.theme.LogoCoral
import com.tcprivatelimited.hitlerai.ui.theme.LogoViolet
import com.tcprivatelimited.hitlerai.ui.theme.PrimaryPillBlue
import com.tcprivatelimited.hitlerai.ui.theme.StatusGreen
import com.tcprivatelimited.hitlerai.ui.theme.WorkspaceBackgroundCanvas
import com.tcprivatelimited.hitlerai.voice.VoiceAssistantController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceShell(
    currentUserId: String,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val vm: WorkspaceViewModel = viewModel(
        key = currentUserId,
        factory = WorkspaceViewModel.Factory(context)
    )

    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val conversationsState by vm.conversationsState.collectAsStateWithLifecycle()
    val messagesState by vm.messagesState.collectAsStateWithLifecycle()
    val localCouncilSessions by vm.localCouncilSessionsState.collectAsStateWithLifecycle()
    val feedPostsState by vm.feedPostsState.collectAsStateWithLifecycle()
    val libraryItemsState by vm.libraryItemsState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val promptFocusRequester = remember { FocusRequester() }
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var showQuickActionsSheet by remember { mutableStateOf(false) }

    val voiceController = remember(context) {
        VoiceAssistantController(
            context = context,
            onDictationResult = { spoken -> vm.appendDictatedText(spoken) },
            onWakeCommand = { agent, command -> vm.submitWakeCommand(agent, command) },
            onStatusMessage = { msg -> vm.showStatus(msg) },
            onWakeStateChanged = { enabled -> vm.setWakeWordEnabled(enabled) }
        )
    }

    DisposableEffect(voiceController) {
        onDispose { voiceController.destroy() }
    }

    var pendingVoiceAction by remember { mutableStateOf<String?>(null) }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            when (pendingVoiceAction) {
                "wake" -> voiceController.setWakeListening(true)
                "dictate" -> voiceController.startOneShotDictation()
            }
        } else {
            vm.showStatus("Microphone permission is required for voice features.")
        }
        pendingVoiceAction = null
    }

    fun requestAudioAndRun(action: String) {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            if (action == "wake") {
                voiceController.setWakeListening(!uiState.wakeWordEnabled)
            } else {
                voiceController.startOneShotDictation()
            }
        } else {
            pendingVoiceAction = action
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            vm.clearStatus()
        }
    }

    BackHandler(enabled = uiState.destination != WorkspaceDestination.CHAT) {
        vm.selectDestination(WorkspaceDestination.CHAT)
    }

    HitlerAiTheme(backgroundPreset = uiState.backgroundPreset) {
        val preset = uiState.backgroundPreset
        val allConversations = (conversationsState as? DataUiState.Success)?.data.orEmpty()
        val filteredConversations = remember(allConversations, uiState.drawerSearchQuery) {
            val q = uiState.drawerSearchQuery.trim()
            if (q.isEmpty()) allConversations
            else allConversations.filter { it.title.contains(q, ignoreCase = true) }
        }
        val activeMessagesCount = if (uiState.isTemporaryChat) {
            uiState.temporaryMessages.size
        } else {
            (messagesState as? DataUiState.Success)?.data?.size ?: 0
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 840.dp
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            val drawerBody: @Composable () -> Unit = {
                MinimalDrawerContent(
                    drawerBg = preset.drawerColor,
                    activeDestination = uiState.destination,
                    conversations = filteredConversations,
                    activeConversationId = uiState.activeConversationId,
                    searchQuery = uiState.drawerSearchQuery,
                    userName = uiState.userDisplayName,
                    onSearchQueryChange = { vm.updateDrawerSearchQuery(it) },
                    onNewChat = {
                        vm.startNewConversation()
                        if (!isWideScreen) scope.launch { drawerState.close() }
                    },
                    onSelectDestination = { dest ->
                        vm.selectDestination(dest)
                        if (!isWideScreen) scope.launch { drawerState.close() }
                    },
                    onSelectConversation = { conv ->
                        vm.openConversation(conv)
                        if (!isWideScreen) scope.launch { drawerState.close() }
                    },
                    onDeleteConversation = { convId -> vm.deleteConversation(convId) },
                    onOpenAccountSettings = {
                        vm.setSettingsDialogVisible(true)
                        if (!isWideScreen) scope.launch { drawerState.close() }
                    }
                )
            }

            val mainContent: @Composable () -> Unit = {
                val showInputDock = uiState.destination == WorkspaceDestination.CHAT ||
                    uiState.destination == WorkspaceDestination.COUNCIL

                Box(modifier = Modifier.fillMaxSize()) {
                    // Full-bleed customizable background behind the entire workspace
                    WorkspaceBackgroundCanvas(
                        preset = uiState.backgroundPreset,
                        ambientGlowEnabled = uiState.ambientGlowEnabled
                    )

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            MinimalWorkspaceTopBar(
                                showMenuButton = !isWideScreen,
                                destination = uiState.destination,
                                isTemporaryChat = uiState.isTemporaryChat,
                                showComposeButton = uiState.destination == WorkspaceDestination.CHAT &&
                                    (activeMessagesCount > 0 || uiState.activeConversationId != null),
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onCouncilPillClick = { vm.selectDestination(WorkspaceDestination.COUNCIL) },
                                onNewChatClick = { vm.startNewConversation() },
                                onToggleTemporaryChat = { vm.toggleTemporaryChat() }
                            )
                        },
                        bottomBar = {
                            if (showInputDock) {
                                FloatingCapsuleComposer(
                                    value = uiState.promptInput,
                                    placeholder = when {
                                        uiState.destination == WorkspaceDestination.COUNCIL ->
                                            stringResource(R.string.prompt_placeholder_council)
                                        uiState.isTemporaryChat ->
                                            stringResource(R.string.prompt_placeholder_temporary)
                                        else ->
                                            stringResource(R.string.prompt_placeholder_chat)
                                    },
                                    isTemporaryChat = uiState.isTemporaryChat &&
                                        uiState.destination == WorkspaceDestination.CHAT,
                                    isBusy = uiState.isSendingChat || uiState.isCouncilRunning,
                                    wakeWordEnabled = uiState.wakeWordEnabled,
                                    focusRequester = promptFocusRequester,
                                    onValueChange = { vm.updatePromptInput(it) },
                                    onOpenQuickActions = { showQuickActionsSheet = true },
                                    onVoiceDictate = { requestAudioAndRun("dictate") },
                                    onToggleWakeWord = { requestAudioAndRun("wake") },
                                    onSend = { vm.sendPrompt() }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .let { mod ->
                                    if (!showInputDock) mod.navigationBarsPadding() else mod
                                }
                        ) {
                            when (uiState.destination) {
                                WorkspaceDestination.CHAT -> ChatWorkspaceScreen(
                                    messagesState = messagesState,
                                    isTemporaryChat = uiState.isTemporaryChat,
                                    temporaryMessages = uiState.temporaryMessages,
                                    activeSpecialist = uiState.activeSpecialist,
                                    isSending = uiState.isSendingChat,
                                    streamingReply = uiState.streamingReply,
                                    chatError = uiState.chatError,
                                    onGoToCouncil = { vm.selectDestination(WorkspaceDestination.COUNCIL) },
                                    onSelectQuickPrompt = { prefix ->
                                        vm.launchTemplatePrompt(prefix)
                                        promptFocusRequester.requestFocus()
                                    },
                                    onSelectSpecialist = { specialist -> vm.selectSpecialist(specialist) },
                                    onClearSpecialist = { vm.selectSpecialist(null) },
                                    onSaveMessageToLibrary = { title, content ->
                                        vm.saveToLibrary(title, "chat", content)
                                    }
                                )

                                WorkspaceDestination.COUNCIL -> CouncilScreen(
                                    liveProgress = uiState.liveCouncilProgress,
                                    selectedSession = uiState.selectedCouncilSession,
                                    isRunning = uiState.isCouncilRunning,
                                    councilError = uiState.councilError,
                                    searchQuery = uiState.councilSearchQuery,
                                    localSessions = localCouncilSessions,
                                    onSearchQueryChange = { query -> vm.updateCouncilSearchQuery(query) },
                                    onSelectSession = { session -> vm.openLocalCouncilSession(session) },
                                    onStartNewSession = { vm.startNewCouncilSession() },
                                    onDeleteSession = { sessionId -> vm.deleteLocalCouncilSession(sessionId) },
                                    onSaveSynthesisToLibrary = { title, content ->
                                        vm.saveToLibrary(title, "council", content)
                                    }
                                )

                                WorkspaceDestination.AGENTS -> AgentsScreen(
                                    onWorkWithSpecialist = { specialist ->
                                        vm.selectSpecialist(specialist, prefillPrompt = true)
                                    }
                                )

                                WorkspaceDestination.FEED -> SocialFeedScreen(
                                    postsState = feedPostsState,
                                    authorName = uiState.userDisplayName,
                                    onPublishPost = { text -> vm.publishFeedPost(text) },
                                    onToggleLike = { post -> vm.togglePostLike(post) },
                                    onAddComment = { post, comment -> vm.addPostComment(post, comment) },
                                    onDeletePost = { postId -> vm.deleteFeedPost(postId) }
                                )

                                WorkspaceDestination.MESSAGES -> MessagesScreen(
                                    onOpenAiWorkspace = { vm.selectDestination(WorkspaceDestination.CHAT) }
                                )

                                WorkspaceDestination.DISCOVER -> DiscoverScreen(
                                    onExploreTopic = { prefix -> vm.launchTemplatePrompt(prefix) }
                                )

                                WorkspaceDestination.WORK -> WorkStudioScreen(
                                    onLaunchTemplate = { prefix -> vm.launchTemplatePrompt(prefix) }
                                )

                                WorkspaceDestination.LIBRARY -> LibraryScreen(
                                    libraryState = libraryItemsState,
                                    onDeleteItem = { itemId -> vm.deleteLibraryItem(itemId) }
                                )

                                WorkspaceDestination.CUSTOMIZE -> CustomizeScreen(
                                    selectedPreset = uiState.backgroundPreset,
                                    ambientGlowEnabled = uiState.ambientGlowEnabled,
                                    wakeWordEnabled = uiState.wakeWordEnabled,
                                    onSelectPreset = { vm.selectBackgroundPreset(it) },
                                    onToggleAmbientGlow = { vm.setAmbientGlowEnabled(it) },
                                    onToggleWakeWord = { requestAudioAndRun("wake") }
                                )
                            }
                        }
                    }
                }
            }

            if (isWideScreen) {
                PermanentNavigationDrawer(
                    drawerContent = {
                        PermanentDrawerSheet(
                            modifier = Modifier.width(292.dp),
                            drawerContainerColor = preset.drawerColor
                        ) {
                            drawerBody()
                        }
                    },
                    content = mainContent
                )
            } else {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.width(304.dp),
                            drawerContainerColor = preset.drawerColor
                        ) {
                            drawerBody()
                        }
                    },
                    content = mainContent
                )
            }
        }

        if (showQuickActionsSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showQuickActionsSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                QuickActionsBottomSheetContent(
                    wakeWordEnabled = uiState.wakeWordEnabled,
                    onGoToCouncil = {
                        showQuickActionsSheet = false
                        vm.selectDestination(WorkspaceDestination.COUNCIL)
                    },
                    onOpenCustomize = {
                        showQuickActionsSheet = false
                        vm.selectDestination(WorkspaceDestination.CUSTOMIZE)
                    },
                    onOpenWorkStudio = {
                        showQuickActionsSheet = false
                        vm.selectDestination(WorkspaceDestination.WORK)
                    },
                    onToggleWakeWord = {
                        showQuickActionsSheet = false
                        requestAudioAndRun("wake")
                    },
                    onSelectSpecialist = { specialist ->
                        showQuickActionsSheet = false
                        vm.selectSpecialist(specialist)
                        promptFocusRequester.requestFocus()
                    }
                )
            }
        }

        if (uiState.showSettingsDialog) {
            WorkspaceSettingsDialog(
                userName = uiState.userDisplayName,
                userEmail = uiState.userEmail,
                backgroundName = uiState.backgroundPreset.displayName,
                wakeWordEnabled = uiState.wakeWordEnabled,
                onOpenCustomize = { vm.selectDestination(WorkspaceDestination.CUSTOMIZE) },
                onToggleWakeWord = { requestAudioAndRun("wake") },
                onSignOut = {
                    voiceController.setWakeListening(false)
                    signOutUser(credentialManager, scope, onSignedOut)
                },
                onDismiss = { vm.setSettingsDialogVisible(false) }
            )
        }
    }
}

@Composable
private fun MinimalWorkspaceTopBar(
    showMenuButton: Boolean,
    destination: WorkspaceDestination,
    isTemporaryChat: Boolean,
    showComposeButton: Boolean,
    onMenuClick: () -> Unit,
    onCouncilPillClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onToggleTemporaryChat: () -> Unit
) {
    val preset = LocalWorkspacePreset.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (showMenuButton) {
                // Circular pill drawer icon with 2-line offset bars (matches Screenshots 1 & 3)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onMenuClick)
                        .testTag("drawer_menu_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(preset.pillContainerColor.copy(alpha = 0.85f))
                            .border(1.dp, preset.borderColor.copy(alpha = 0.65f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val barColor = MaterialTheme.colorScheme.onBackground
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val stroke = 2.dp.toPx()
                            drawLine(
                                color = barColor,
                                start = Offset(2.dp.toPx(), size.height * 0.34f),
                                end = Offset(size.width - 2.dp.toPx(), size.height * 0.34f),
                                strokeWidth = stroke,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = barColor,
                                start = Offset(2.dp.toPx(), size.height * 0.66f),
                                end = Offset(size.width * 0.62f, size.height * 0.66f),
                                strokeWidth = stroke,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            if (destination == WorkspaceDestination.CHAT) {
                if (isTemporaryChat) {
                    Text(
                        text = stringResource(R.string.temporary_chat_badge),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    // Rounded accent pill matching Screenshot 1 top pill
                    Surface(
                        shape = CircleShape,
                        color = preset.pillContainerColor.copy(alpha = 0.9f),
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.dp, preset.borderColor.copy(alpha = 0.7f), CircleShape)
                            .clickable(onClick = onCouncilPillClick)
                            .testTag("top_council_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = LogoViolet,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.pill_five_ai_council),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = destination.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showComposeButton) {
                IconButton(
                    onClick = onNewChatClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("top_new_chat_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(preset.pillContainerColor.copy(alpha = 0.85f))
                            .border(1.dp, preset.borderColor.copy(alpha = 0.65f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.cd_new_chat),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Circular Temporary / Incognito Chat Toggle (matches Screenshots 1, 3, 4)
            IconButton(
                onClick = onToggleTemporaryChat,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("temporary_chat_toggle_button")
            ) {
                val circleBg = if (isTemporaryChat) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    preset.pillContainerColor.copy(alpha = 0.85f)
                }
                val iconTint = if (isTemporaryChat) {
                    MaterialTheme.colorScheme.background
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(circleBg)
                        .border(
                            width = 1.dp,
                            color = if (isTemporaryChat) circleBg else preset.borderColor.copy(alpha = 0.65f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = stringResource(R.string.cd_toggle_temporary_chat),
                        tint = iconTint,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingCapsuleComposer(
    value: String,
    placeholder: String,
    isTemporaryChat: Boolean,
    isBusy: Boolean,
    wakeWordEnabled: Boolean,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onOpenQuickActions: () -> Unit,
    onVoiceDictate: () -> Unit,
    onToggleWakeWord: () -> Unit,
    onSend: () -> Unit
) {
    val preset = LocalWorkspacePreset.current
    val dashColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    val solidBorderColor = preset.borderColor.copy(alpha = 0.75f)
    val hasText = value.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pillShape = RoundedCornerShape(30.dp)
        Surface(
            shape = pillShape,
            color = preset.pillContainerColor.copy(alpha = 0.92f),
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    val strokeWidth = 1.25.dp.toPx()
                    val radius = 30.dp.toPx()
                    drawRoundRect(
                        color = if (isTemporaryChat) dashColor else solidBorderColor,
                        cornerRadius = CornerRadius(radius, radius),
                        style = if (isTemporaryChat) {
                            Stroke(
                                width = strokeWidth,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                            )
                        } else {
                            Stroke(width = strokeWidth)
                        }
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular '+' button matching Screenshots 1 & 3
                IconButton(
                    onClick = onOpenQuickActions,
                    enabled = !isBusy,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("quick_actions_plus_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(preset.elevatedSurfaceColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.cd_open_quick_actions),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (!isBusy && hasText) onSend() }),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .testTag("prompt_text_input")
                )

                // Microphone dictation button
                IconButton(
                    onClick = onVoiceDictate,
                    enabled = !isBusy,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("voice_dictate_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(preset.elevatedSurfaceColor.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = stringResource(R.string.cd_voice_dictation),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // Rightmost circular high-contrast Send or Voice Wake-Word button (matches Screenshots 1 & 3)
                IconButton(
                    onClick = {
                        if (hasText) {
                            onSend()
                        } else {
                            onToggleWakeWord()
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(if (hasText) "send_prompt_button" else "wake_word_toggle_button")
                ) {
                    val bgColor = when {
                        hasText -> MaterialTheme.colorScheme.onBackground
                        wakeWordEnabled -> StatusGreen
                        else -> MaterialTheme.colorScheme.onBackground
                    }
                    val iconColor = when {
                        wakeWordEnabled && !hasText -> Color(0xFF0A1410)
                        else -> MaterialTheme.colorScheme.background
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasText) Icons.Outlined.ArrowUpward else Icons.Outlined.GraphicEq,
                            contentDescription = if (hasText) {
                                stringResource(R.string.cd_send_message)
                            } else {
                                stringResource(R.string.cd_toggle_wake)
                            },
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalDrawerContent(
    drawerBg: Color,
    activeDestination: WorkspaceDestination,
    conversations: List<Conversation>,
    activeConversationId: String?,
    searchQuery: String,
    userName: String,
    onSearchQueryChange: (String) -> Unit,
    onNewChat: () -> Unit,
    onSelectDestination: (WorkspaceDestination) -> Unit,
    onSelectConversation: (Conversation) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onOpenAccountSettings: () -> Unit
) {
    val preset = LocalWorkspacePreset.current
    var showSearchField by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(drawerBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        // Drawer Top Header: Brand title + Circular Search Icon (matches Screenshot 2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = stringResource(R.string.cd_app_logo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 21.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = {
                    showSearchField = !showSearchField
                    if (!showSearchField) onSearchQueryChange("")
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("drawer_search_toggle_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(preset.pillContainerColor)
                        .border(1.dp, preset.borderColor.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (showSearchField) Icons.Outlined.Close else Icons.Outlined.Search,
                        contentDescription = stringResource(R.string.cd_search_chats),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        AnimatedVisibility(visible = showSearchField) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(stringResource(R.string.drawer_search_hint)) },
                singleLine = true,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp)
                    .testTag("drawer_search_input")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Clean icon + label navigation rows matching Screenshot 2
            item {
                DrawerFlatItem(
                    label = stringResource(R.string.nav_council),
                    icon = Icons.Outlined.AutoAwesome,
                    badge = "5",
                    selected = activeDestination == WorkspaceDestination.COUNCIL,
                    onClick = { onSelectDestination(WorkspaceDestination.COUNCIL) }
                )
            }
            item {
                DrawerFlatItem(
                    label = stringResource(R.string.nav_library),
                    icon = Icons.Outlined.Bookmarks,
                    selected = activeDestination == WorkspaceDestination.LIBRARY,
                    onClick = { onSelectDestination(WorkspaceDestination.LIBRARY) }
                )
            }
            item {
                DrawerFlatItem(
                    label = stringResource(R.string.nav_customize),
                    icon = Icons.Outlined.Tune,
                    selected = activeDestination == WorkspaceDestination.CUSTOMIZE,
                    onClick = { onSelectDestination(WorkspaceDestination.CUSTOMIZE) }
                )
            }
            item {
                DrawerFlatItem(
                    label = stringResource(R.string.nav_agents),
                    icon = Icons.Outlined.SmartToy,
                    selected = activeDestination == WorkspaceDestination.AGENTS,
                    onClick = { onSelectDestination(WorkspaceDestination.AGENTS) }
                )
            }
            item {
                DrawerFlatItem(
                    label = stringResource(R.string.nav_work),
                    icon = Icons.Outlined.WorkOutline,
                    selected = activeDestination == WorkspaceDestination.WORK,
                    onClick = { onSelectDestination(WorkspaceDestination.WORK) }
                )
            }
            item {
                DrawerFlatItem(
                    label = stringResource(R.string.nav_discover),
                    icon = Icons.Outlined.Explore,
                    selected = activeDestination == WorkspaceDestination.DISCOVER,
                    onClick = { onSelectDestination(WorkspaceDestination.DISCOVER) }
                )
            }
            item {
                DrawerFlatItem(
                    label = stringResource(R.string.nav_feed),
                    icon = Icons.Outlined.DynamicFeed,
                    selected = activeDestination == WorkspaceDestination.FEED,
                    onClick = { onSelectDestination(WorkspaceDestination.FEED) }
                )
            }
            item {
                DrawerFlatItem(
                    label = stringResource(R.string.nav_messages),
                    icon = Icons.Outlined.Forum,
                    selected = activeDestination == WorkspaceDestination.MESSAGES,
                    onClick = { onSelectDestination(WorkspaceDestination.MESSAGES) }
                )
            }

            if (conversations.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.drawer_section_chats),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, top = 20.dp, bottom = 8.dp)
                    )
                }
                items(conversations, key = { it.id }) { conv ->
                    val isSelected = conv.id == activeConversationId &&
                        activeDestination == WorkspaceDestination.CHAT
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) preset.elevatedSurfaceColor else Color.Transparent
                            )
                            .clickable { onSelectConversation(conv) }
                            .padding(start = 10.dp, end = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = conv.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onDeleteConversation(conv.id) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(R.string.cd_delete_item),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom anchored row matching Screenshot 2: Primary pill 'Chat' button left + Circular user avatar right
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onNewChat,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPillBlue,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("sidebar_new_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.drawer_action_chat),
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp)
                )
            }

            IconButton(
                onClick = onOpenAccountSettings,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("sidebar_settings_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(LogoViolet, LogoCoral))
                        )
                        .border(1.5.dp, preset.borderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(2).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerFlatItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    badge: String? = null,
    onClick: () -> Unit
) {
    val preset = LocalWorkspacePreset.current
    val bgColor = if (selected) preset.elevatedSurfaceColor else Color.Transparent
    val textColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp)
            .testTag("nav_item_${label.lowercase().replace(" ", "_")}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) LogoViolet else textColor.copy(alpha = 0.88f),
            modifier = Modifier.size(21.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        if (badge != null) {
            Surface(
                shape = CircleShape,
                color = preset.pillContainerColor
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsBottomSheetContent(
    wakeWordEnabled: Boolean,
    onGoToCouncil: () -> Unit,
    onOpenCustomize: () -> Unit,
    onOpenWorkStudio: () -> Unit,
    onToggleWakeWord: () -> Unit,
    onSelectSpecialist: (com.tcprivatelimited.hitlerai.data.model.AiSpecialist) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.quick_sheet_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        SheetActionRow(
            icon = Icons.Outlined.AutoAwesome,
            title = "Five-AI Council",
            subtitle = "Get 5 specialist perspectives + Head synthesis",
            onClick = onGoToCouncil
        )
        SheetActionRow(
            icon = Icons.Outlined.Palette,
            title = "Customize background",
            subtitle = "Switch studio themes and ambient logo glow",
            onClick = onOpenCustomize
        )
        SheetActionRow(
            icon = Icons.Outlined.WorkOutline,
            title = "Work Studio templates",
            subtitle = "Emails, proposals, project plans, specs",
            onClick = onOpenWorkStudio
        )
        SheetActionRow(
            icon = Icons.Outlined.RecordVoiceOver,
            title = if (wakeWordEnabled) "Turn off wake word" else "Enable hands-free wake word",
            subtitle = "Say Jarvis, Vision, Ultron, Friday, or Edith",
            onClick = onToggleWakeWord
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Text(
            text = "Focus with a specialist",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WorkspaceCatalog.specialists.forEach { specialist ->
                Surface(
                    shape = CircleShape,
                    color = specialist.accentColor.copy(alpha = 0.16f),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(CircleShape)
                        .border(1.dp, specialist.accentColor.copy(alpha = 0.45f), CircleShape)
                        .clickable { onSelectSpecialist(specialist) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = specialist.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
