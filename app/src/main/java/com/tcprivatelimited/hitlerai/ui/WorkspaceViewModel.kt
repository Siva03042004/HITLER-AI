package com.tcprivatelimited.hitlerai.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.tcprivatelimited.hitlerai.data.local.CouncilSessionEntity
import com.tcprivatelimited.hitlerai.data.local.WorkspacePreferences
import com.tcprivatelimited.hitlerai.data.model.AiSpecialist
import com.tcprivatelimited.hitlerai.data.model.ChatMessage
import com.tcprivatelimited.hitlerai.data.model.Conversation
import com.tcprivatelimited.hitlerai.data.model.FeedPost
import com.tcprivatelimited.hitlerai.data.model.LibraryItem
import com.tcprivatelimited.hitlerai.data.model.WorkspaceCatalog
import com.tcprivatelimited.hitlerai.data.model.WorkspaceDestination
import com.tcprivatelimited.hitlerai.data.repository.AiRepository
import com.tcprivatelimited.hitlerai.data.repository.CouncilHistoryRepository
import com.tcprivatelimited.hitlerai.data.repository.CouncilProgress
import com.tcprivatelimited.hitlerai.data.repository.WorkspaceRepository
import com.tcprivatelimited.hitlerai.ui.theme.WorkspaceBackgroundPreset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val VM_TAG = "WorkspaceViewModel"

sealed interface DataUiState<out T> {
    data object Loading : DataUiState<Nothing>
    data class Success<T>(val data: T) : DataUiState<T>
    data class Error(val message: String) : DataUiState<Nothing>
}

data class WorkspaceUiState(
    val destination: WorkspaceDestination = WorkspaceDestination.CHAT,
    val activeConversationId: String? = null,
    val activeSpecialist: AiSpecialist? = null,
    val isTemporaryChat: Boolean = false,
    val temporaryMessages: List<ChatMessage> = emptyList(),
    val drawerSearchQuery: String = "",
    val promptInput: String = "",
    val isSendingChat: Boolean = false,
    val streamingReply: String = "",
    val chatError: String? = null,
    val isCouncilRunning: Boolean = false,
    val liveCouncilProgress: CouncilProgress? = null,
    val selectedCouncilSession: CouncilSessionEntity? = null,
    val councilSearchQuery: String = "",
    val councilError: String? = null,
    val backgroundPreset: WorkspaceBackgroundPreset = WorkspaceBackgroundPreset.OBSIDIAN,
    val ambientGlowEnabled: Boolean = true,
    val wakeWordEnabled: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val statusMessage: String? = null,
    val userDisplayName: String = "Workspace User",
    val userEmail: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class WorkspaceViewModel(
    private val workspaceRepository: WorkspaceRepository,
    private val councilHistoryRepository: CouncilHistoryRepository,
    private val aiRepository: AiRepository,
    private val preferences: WorkspacePreferences
) : ViewModel() {

    private val currentUser = Firebase.auth.currentUser
    private val currentUserId = currentUser?.uid.orEmpty()

    private val _uiState = MutableStateFlow(
        WorkspaceUiState(
            backgroundPreset = preferences.loadBackgroundPreset(),
            ambientGlowEnabled = preferences.isAmbientGlowEnabled(),
            userDisplayName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "TC Workspace",
            userEmail = currentUser?.email.orEmpty()
        )
    )
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    private val activeConversationIdFlow = MutableStateFlow<String?>(null)
    private val councilSearchQueryFlow = MutableStateFlow("")
    private var chatJob: Job? = null
    private var councilJob: Job? = null

    val conversationsState: StateFlow<DataUiState<List<Conversation>>> =
        workspaceRepository.observeConversations()
            .map<List<Conversation>, DataUiState<List<Conversation>>> { DataUiState.Success(it) }
            .catch { error ->
                val fsError = WorkspaceRepository.unwrapFirestoreException(error)
                if (fsError == null && error is CancellationException) throw error
                emit(DataUiState.Error((fsError ?: error).message ?: "Unable to load conversations."))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataUiState.Loading)

    val messagesState: StateFlow<DataUiState<List<ChatMessage>>> =
        activeConversationIdFlow
            .flatMapLatest { convId ->
                if (convId.isNullOrBlank()) {
                    flowOf<DataUiState<List<ChatMessage>>>(DataUiState.Success(emptyList()))
                } else {
                    workspaceRepository.observeMessages(convId)
                        .map<List<ChatMessage>, DataUiState<List<ChatMessage>>> { DataUiState.Success(it) }
                        .catch { error ->
                            val fsError = WorkspaceRepository.unwrapFirestoreException(error)
                            if (fsError == null && error is CancellationException) throw error
                            emit(DataUiState.Error((fsError ?: error).message ?: "Unable to load messages."))
                        }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataUiState.Success(emptyList()))

    val localCouncilSessionsState: StateFlow<List<CouncilSessionEntity>> =
        councilSearchQueryFlow
            .flatMapLatest { query ->
                if (currentUserId.isBlank()) {
                    flowOf(emptyList())
                } else {
                    councilHistoryRepository.observeSessions(currentUserId, query)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val feedPostsState: StateFlow<DataUiState<List<FeedPost>>> =
        workspaceRepository.observeFeedPosts()
            .map<List<FeedPost>, DataUiState<List<FeedPost>>> { DataUiState.Success(it) }
            .catch { error ->
                val fsError = WorkspaceRepository.unwrapFirestoreException(error)
                if (fsError == null && error is CancellationException) throw error
                emit(DataUiState.Error((fsError ?: error).message ?: "Unable to load Social Feed."))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataUiState.Loading)

    val libraryItemsState: StateFlow<DataUiState<List<LibraryItem>>> =
        workspaceRepository.observeLibraryItems()
            .map<List<LibraryItem>, DataUiState<List<LibraryItem>>> { DataUiState.Success(it) }
            .catch { error ->
                val fsError = WorkspaceRepository.unwrapFirestoreException(error)
                if (fsError == null && error is CancellationException) throw error
                emit(DataUiState.Error((fsError ?: error).message ?: "Unable to load Library."))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataUiState.Loading)

    fun selectDestination(destination: WorkspaceDestination) {
        _uiState.update { it.copy(destination = destination) }
    }

    fun updatePromptInput(text: String) {
        _uiState.update { it.copy(promptInput = text) }
    }

    fun updateDrawerSearchQuery(query: String) {
        _uiState.update { it.copy(drawerSearchQuery = query) }
    }

    fun appendDictatedText(spoken: String) {
        _uiState.update { current ->
            val updated = if (current.promptInput.isBlank()) spoken else "${current.promptInput} $spoken"
            current.copy(promptInput = updated)
        }
    }

    fun toggleTemporaryChat() {
        chatJob?.cancel()
        val enteringTemp = !_uiState.value.isTemporaryChat
        if (enteringTemp) {
            activeConversationIdFlow.value = null
        }
        _uiState.update {
            it.copy(
                destination = WorkspaceDestination.CHAT,
                isTemporaryChat = enteringTemp,
                activeConversationId = if (enteringTemp) null else it.activeConversationId,
                temporaryMessages = emptyList(),
                isSendingChat = false,
                streamingReply = "",
                chatError = null
            )
        }
    }

    fun startNewConversation() {
        chatJob?.cancel()
        activeConversationIdFlow.value = null
        _uiState.update {
            it.copy(
                destination = WorkspaceDestination.CHAT,
                isTemporaryChat = false,
                temporaryMessages = emptyList(),
                activeConversationId = null,
                activeSpecialist = null,
                isSendingChat = false,
                streamingReply = "",
                chatError = null
            )
        }
    }

    fun openConversation(conversation: Conversation) {
        chatJob?.cancel()
        activeConversationIdFlow.value = conversation.id
        _uiState.update {
            it.copy(
                destination = WorkspaceDestination.CHAT,
                isTemporaryChat = false,
                temporaryMessages = emptyList(),
                activeConversationId = conversation.id,
                activeSpecialist = WorkspaceCatalog.specialistByName(conversation.activeAgent),
                isSendingChat = false,
                streamingReply = "",
                chatError = null
            )
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            val result = workspaceRepository.deleteConversation(conversationId)
            result.onSuccess {
                if (activeConversationIdFlow.value == conversationId) {
                    startNewConversation()
                }
                showStatus("Conversation removed.")
            }.onFailure { err ->
                showStatus(err.localizedMessage ?: "Failed to delete conversation.")
            }
        }
    }

    fun selectSpecialist(specialist: AiSpecialist?, prefillPrompt: Boolean = false) {
        _uiState.update { current ->
            current.copy(
                destination = WorkspaceDestination.CHAT,
                activeSpecialist = specialist,
                promptInput = if (prefillPrompt && specialist != null) specialist.promptPrefix else current.promptInput
            )
        }
    }

    fun launchTemplatePrompt(promptPrefix: String) {
        _uiState.update {
            it.copy(
                destination = WorkspaceDestination.CHAT,
                promptInput = promptPrefix
            )
        }
    }

    fun submitWakeCommand(agentName: String, command: String) {
        val specialist = WorkspaceCatalog.specialistByName(agentName)
        _uiState.update {
            it.copy(
                destination = WorkspaceDestination.CHAT,
                activeSpecialist = specialist
            )
        }
        sendPrompt(command)
    }

    fun sendPrompt(overrideText: String? = null) {
        val text = (overrideText ?: _uiState.value.promptInput).trim()
        if (text.isEmpty()) return

        if (_uiState.value.destination == WorkspaceDestination.COUNCIL) {
            runCouncil(text)
            return
        }

        if (_uiState.value.isSendingChat) return
        val specialist = _uiState.value.activeSpecialist
        val isTemp = _uiState.value.isTemporaryChat

        if (isTemp) {
            sendTemporaryPrompt(text, specialist)
            return
        }

        val priorMessages = (messagesState.value as? DataUiState.Success)?.data.orEmpty()

        _uiState.update {
            it.copy(
                promptInput = "",
                isSendingChat = true,
                streamingReply = "",
                chatError = null
            )
        }

        chatJob = viewModelScope.launch {
            try {
                var convId = activeConversationIdFlow.value
                if (convId.isNullOrBlank()) {
                    val created = workspaceRepository.createConversation(
                        title = text.take(60),
                        activeAgent = specialist?.name,
                        firstPreview = text.take(180)
                    )
                    convId = created.getOrElse { throw it }
                    activeConversationIdFlow.value = convId
                    _uiState.update { it.copy(activeConversationId = convId) }
                }

                val currentCount = priorMessages.size.toLong()
                workspaceRepository.appendMessage(
                    conversationId = convId,
                    role = "user",
                    senderLabel = "YOU",
                    content = text,
                    newMessageCount = currentCount + 1L
                ).getOrElse { throw it }

                var finalReply = ""
                aiRepository.streamChatReply(
                    history = priorMessages,
                    userPrompt = text,
                    activeSpecialist = specialist
                ).collect { partial ->
                    finalReply = partial
                    _uiState.update { it.copy(streamingReply = partial) }
                }

                val assistantLabel = specialist?.name?.uppercase() ?: "HITLER AI"
                workspaceRepository.appendMessage(
                    conversationId = convId,
                    role = "assistant",
                    senderLabel = assistantLabel,
                    content = finalReply,
                    newMessageCount = currentCount + 2L
                ).getOrElse { throw it }

                _uiState.update {
                    it.copy(
                        isSendingChat = false,
                        streamingReply = ""
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(VM_TAG, "Chat request failed", e)
                _uiState.update {
                    it.copy(
                        isSendingChat = false,
                        streamingReply = "",
                        chatError = e.localizedMessage ?: "Unable to complete AI request."
                    )
                }
            }
        }
    }

    private fun sendTemporaryPrompt(text: String, specialist: AiSpecialist?) {
        val priorMessages = _uiState.value.temporaryMessages
        val userMsg = ChatMessage(
            id = "temp_u_${System.currentTimeMillis()}",
            userId = currentUserId,
            role = "user",
            senderLabel = "YOU",
            content = text
        )

        _uiState.update {
            it.copy(
                promptInput = "",
                temporaryMessages = priorMessages + userMsg,
                isSendingChat = true,
                streamingReply = "",
                chatError = null
            )
        }

        chatJob = viewModelScope.launch {
            try {
                var finalReply = ""
                aiRepository.streamChatReply(
                    history = priorMessages,
                    userPrompt = text,
                    activeSpecialist = specialist
                ).collect { partial ->
                    finalReply = partial
                    _uiState.update { it.copy(streamingReply = partial) }
                }

                val assistantLabel = specialist?.name?.uppercase() ?: "HITLER AI"
                val assistantMsg = ChatMessage(
                    id = "temp_a_${System.currentTimeMillis()}",
                    userId = currentUserId,
                    role = "assistant",
                    senderLabel = assistantLabel,
                    content = finalReply
                )

                _uiState.update {
                    it.copy(
                        temporaryMessages = it.temporaryMessages + assistantMsg,
                        isSendingChat = false,
                        streamingReply = ""
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(VM_TAG, "Temporary chat request failed", e)
                _uiState.update {
                    it.copy(
                        isSendingChat = false,
                        streamingReply = "",
                        chatError = e.localizedMessage ?: "Unable to complete AI request."
                    )
                }
            }
        }
    }

    private fun runCouncil(prompt: String) {
        if (_uiState.value.isCouncilRunning) return
        _uiState.update {
            it.copy(
                promptInput = "",
                isCouncilRunning = true,
                selectedCouncilSession = null,
                councilError = null,
                liveCouncilProgress = CouncilProgress(
                    prompt = prompt,
                    activeSpecialist = WorkspaceCatalog.councilOrder.first().name,
                    turns = emptyMap(),
                    synthesis = "",
                    isComplete = false
                )
            )
        }

        councilJob = viewModelScope.launch {
            try {
                val result = aiRepository.runCouncilSession(prompt) { progress ->
                    _uiState.update { it.copy(liveCouncilProgress = progress) }
                }

                val savedLocal = if (currentUserId.isNotBlank()) {
                    councilHistoryRepository.saveSession(
                        userId = currentUserId,
                        prompt = result.prompt,
                        turns = result.turns,
                        synthesis = result.synthesis
                    )
                } else null

                workspaceRepository.saveCouncilSession(
                    prompt = result.prompt,
                    turns = result.turns,
                    synthesis = result.synthesis
                ).onFailure { err ->
                    Log.w(VM_TAG, "Cloud backup of council session failed: ${err.localizedMessage}")
                }

                _uiState.update {
                    it.copy(
                        isCouncilRunning = false,
                        selectedCouncilSession = savedLocal
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(VM_TAG, "Council execution failed", e)
                _uiState.update {
                    it.copy(
                        isCouncilRunning = false,
                        councilError = e.localizedMessage ?: "Five-AI Council failed."
                    )
                }
            }
        }
    }

    fun openLocalCouncilSession(session: CouncilSessionEntity) {
        _uiState.update {
            it.copy(
                destination = WorkspaceDestination.COUNCIL,
                selectedCouncilSession = session,
                liveCouncilProgress = null,
                councilError = null
            )
        }
    }

    fun startNewCouncilSession() {
        councilJob?.cancel()
        _uiState.update {
            it.copy(
                isCouncilRunning = false,
                selectedCouncilSession = null,
                liveCouncilProgress = null,
                councilError = null
            )
        }
    }

    fun updateCouncilSearchQuery(query: String) {
        councilSearchQueryFlow.value = query
        _uiState.update { it.copy(councilSearchQuery = query) }
    }

    fun deleteLocalCouncilSession(sessionId: Long) {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            try {
                councilHistoryRepository.deleteSession(sessionId, currentUserId)
                _uiState.update { current ->
                    val clearSelection = current.selectedCouncilSession?.id == sessionId
                    current.copy(
                        selectedCouncilSession = if (clearSelection) null else current.selectedCouncilSession,
                        liveCouncilProgress = if (clearSelection) null else current.liveCouncilProgress
                    )
                }
                showStatus("Council session deleted from local history.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(VM_TAG, "Failed to delete local council session", e)
                showStatus("Could not delete council session.")
            }
        }
    }

    fun publishFeedPost(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val result = workspaceRepository.createFeedPost(
                authorName = _uiState.value.userDisplayName,
                text = trimmed
            )
            result.onSuccess {
                showStatus("Post published to your synced workspace feed.")
            }.onFailure { err ->
                showStatus(err.localizedMessage ?: "Could not publish post.")
            }
        }
    }

    fun togglePostLike(post: FeedPost) {
        viewModelScope.launch {
            workspaceRepository.toggleFeedPostLike(post).onFailure { err ->
                showStatus(err.localizedMessage ?: "Could not update like.")
            }
        }
    }

    fun addPostComment(post: FeedPost, comment: String) {
        val trimmed = comment.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            workspaceRepository.addFeedPostComment(post, trimmed).onFailure { err ->
                showStatus(err.localizedMessage ?: "Could not add comment.")
            }
        }
    }

    fun deleteFeedPost(postId: String) {
        viewModelScope.launch {
            workspaceRepository.deleteFeedPost(postId).onSuccess {
                showStatus("Post deleted.")
            }.onFailure { err ->
                showStatus(err.localizedMessage ?: "Could not delete post.")
            }
        }
    }

    fun saveToLibrary(title: String, category: String, content: String) {
        viewModelScope.launch {
            workspaceRepository.saveLibraryItem(title, category, content).onSuccess {
                showStatus("Saved to Library.")
            }.onFailure { err ->
                showStatus(err.localizedMessage ?: "Could not save item.")
            }
        }
    }

    fun deleteLibraryItem(itemId: String) {
        viewModelScope.launch {
            workspaceRepository.deleteLibraryItem(itemId).onSuccess {
                showStatus("Removed from Library.")
            }.onFailure { err ->
                showStatus(err.localizedMessage ?: "Could not remove item.")
            }
        }
    }

    fun selectBackgroundPreset(preset: WorkspaceBackgroundPreset) {
        preferences.saveBackgroundPreset(preset)
        _uiState.update { it.copy(backgroundPreset = preset) }
    }

    fun setAmbientGlowEnabled(enabled: Boolean) {
        preferences.setAmbientGlowEnabled(enabled)
        _uiState.update { it.copy(ambientGlowEnabled = enabled) }
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        _uiState.update { it.copy(wakeWordEnabled = enabled) }
    }

    fun setSettingsDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = visible) }
    }

    fun showStatus(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    fun clearStatus() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appCtx = context.applicationContext
            return WorkspaceViewModel(
                workspaceRepository = WorkspaceRepository(appCtx),
                councilHistoryRepository = CouncilHistoryRepository(appCtx),
                aiRepository = AiRepository(),
                preferences = WorkspacePreferences(appCtx)
            ) as T
        }
    }
}
