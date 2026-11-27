package com.tcprivatelimited.hitlerai.data.repository

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.tcprivatelimited.hitlerai.R
import com.tcprivatelimited.hitlerai.data.model.ChatMessage
import com.tcprivatelimited.hitlerai.data.model.Conversation
import com.tcprivatelimited.hitlerai.data.model.CouncilSession
import com.tcprivatelimited.hitlerai.data.model.FeedPost
import com.tcprivatelimited.hitlerai.data.model.LibraryItem
import com.tcprivatelimited.hitlerai.firebase.OperationType
import com.tcprivatelimited.hitlerai.firebase.handleFirestoreError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class WorkspaceRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        }
        return uid
    }

    private fun userRoot(uid: String) = db.collection("users").document(uid)

    fun observeConversations(): Flow<List<Conversation>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/conversations"
        emitAll(
            userRoot(uid).collection("conversations")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(30)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        Conversation::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val fsError = unwrapFirestoreException(error)
                    if (fsError == null && error is CancellationException) throw error
                    val toLog = fsError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> = flow {
        val uid = requireUserId()
        if (conversationId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        val path = "users/$uid/conversations/$conversationId/messages"
        emitAll(
            userRoot(uid).collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        ChatMessage::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val fsError = unwrapFirestoreException(error)
                    if (fsError == null && error is CancellationException) throw error
                    val toLog = fsError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createConversation(
        title: String,
        activeAgent: String?,
        firstPreview: String
    ): Result<String> {
        var path = "users/unauthenticated/conversations"
        return try {
            val uid = requireUserId()
            val safeTitle = title.trim().ifBlank { "New conversation" }.take(120)
            val safePreview = firstPreview.trim().take(300)
            val safeAgent = activeAgent?.trim()?.take(40)?.takeIf { it.isNotEmpty() }

            val docRef = userRoot(uid).collection("conversations").document()
            path = docRef.path

            val payload = linkedMapOf<String, Any?>(
                "userId" to uid,
                "title" to safeTitle,
                "activeAgent" to safeAgent,
                "lastMessagePreview" to safePreview,
                "messageCount" to 0L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun appendMessage(
        conversationId: String,
        role: String,
        senderLabel: String,
        content: String,
        newMessageCount: Long
    ): Result<Unit> {
        var path = "users/unauthenticated/conversations/$conversationId/messages"
        return try {
            val uid = requireUserId()
            require(conversationId.isNotBlank()) { "Conversation ID cannot be blank." }
            val safeRole = if (role == "assistant") "assistant" else "user"
            val safeLabel = senderLabel.trim().ifBlank { if (safeRole == "user") "YOU" else "HITLER AI" }.take(40)
            val safeContent = content.trim().take(16000)
            require(safeContent.isNotEmpty()) { "Message content cannot be empty." }

            val convRef = userRoot(uid).collection("conversations").document(conversationId)
            val msgRef = convRef.collection("messages").document()
            path = msgRef.path

            val msgPayload = mapOf(
                "userId" to uid,
                "role" to safeRole,
                "senderLabel" to safeLabel,
                "content" to safeContent,
                "createdAt" to FieldValue.serverTimestamp()
            )
            msgRef.set(msgPayload).await()

            path = convRef.path
            convRef.update(
                mapOf(
                    "lastMessagePreview" to safeContent.take(300),
                    "messageCount" to newMessageCount.coerceIn(0L, 10000L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteConversation(conversationId: String): Result<Unit> {
        var path = "users/unauthenticated/conversations/$conversationId"
        return try {
            val uid = requireUserId()
            require(conversationId.isNotBlank()) { "Conversation ID cannot be blank." }
            val convRef = userRoot(uid).collection("conversations").document(conversationId)
            path = convRef.path
            val messages = convRef.collection("messages").get().await()
            for (doc in messages.documents) {
                doc.reference.delete().await()
            }
            convRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeCouncilSessions(): Flow<List<CouncilSession>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/councilSessions"
        emitAll(
            userRoot(uid).collection("councilSessions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        CouncilSession::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val fsError = unwrapFirestoreException(error)
                    if (fsError == null && error is CancellationException) throw error
                    val toLog = fsError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveCouncilSession(
        prompt: String,
        turns: Map<String, String>,
        synthesis: String
    ): Result<Unit> {
        var path = "users/unauthenticated/councilSessions"
        return try {
            val uid = requireUserId()
            val safePrompt = prompt.trim().take(4000)
            require(safePrompt.isNotEmpty()) { "Prompt cannot be empty." }

            val docRef = userRoot(uid).collection("councilSessions").document()
            path = docRef.path

            val payload = mapOf(
                "userId" to uid,
                "prompt" to safePrompt,
                "edithResponse" to (turns["Edith"] ?: "").take(8000),
                "visionResponse" to (turns["Vision"] ?: "").take(8000),
                "ultronResponse" to (turns["Ultron"] ?: "").take(8000),
                "fridayResponse" to (turns["Friday"] ?: "").take(8000),
                "jarvisResponse" to (turns["Jarvis"] ?: "").take(8000),
                "synthesis" to synthesis.take(10000),
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeFeedPosts(): Flow<List<FeedPost>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/feedPosts"
        emitAll(
            userRoot(uid).collection("feedPosts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        FeedPost::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val fsError = unwrapFirestoreException(error)
                    if (fsError == null && error is CancellationException) throw error
                    val toLog = fsError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createFeedPost(authorName: String, text: String): Result<Unit> {
        var path = "users/unauthenticated/feedPosts"
        return try {
            val uid = requireUserId()
            val safeAuthor = authorName.trim().ifBlank { "TC Workspace" }.take(80)
            val safeText = text.trim().take(2000)
            require(safeText.isNotEmpty()) { "Post text cannot be empty." }

            val docRef = userRoot(uid).collection("feedPosts").document()
            path = docRef.path

            val payload = mapOf(
                "userId" to uid,
                "authorName" to safeAuthor,
                "text" to safeText,
                "likes" to 0L,
                "likedByMe" to false,
                "comments" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleFeedPostLike(post: FeedPost): Result<Unit> {
        var path = "users/unauthenticated/feedPosts/${post.id}"
        return try {
            val uid = requireUserId()
            require(post.id.isNotBlank()) { "Post ID cannot be blank." }
            val nextLiked = !post.likedByMe
            val nextLikes = (post.likes + if (nextLiked) 1L else -1L).coerceAtLeast(0L)

            val docRef = userRoot(uid).collection("feedPosts").document(post.id)
            path = docRef.path
            docRef.update(
                mapOf(
                    "likes" to nextLikes,
                    "likedByMe" to nextLiked,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun addFeedPostComment(post: FeedPost, commentText: String): Result<Unit> {
        var path = "users/unauthenticated/feedPosts/${post.id}"
        return try {
            val uid = requireUserId()
            require(post.id.isNotBlank()) { "Post ID cannot be blank." }
            val safeComment = commentText.trim().take(500)
            require(safeComment.isNotEmpty()) { "Comment cannot be empty." }
            require(post.comments.size < 50) { "Maximum of 50 comments reached for this post." }

            val updatedComments = post.comments + safeComment
            val docRef = userRoot(uid).collection("feedPosts").document(post.id)
            path = docRef.path
            docRef.update(
                mapOf(
                    "comments" to updatedComments,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteFeedPost(postId: String): Result<Unit> {
        var path = "users/unauthenticated/feedPosts/$postId"
        return try {
            val uid = requireUserId()
            require(postId.isNotBlank()) { "Post ID cannot be blank." }
            val docRef = userRoot(uid).collection("feedPosts").document(postId)
            path = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeLibraryItems(): Flow<List<LibraryItem>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/libraryItems"
        emitAll(
            userRoot(uid).collection("libraryItems")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        LibraryItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val fsError = unwrapFirestoreException(error)
                    if (fsError == null && error is CancellationException) throw error
                    val toLog = fsError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveLibraryItem(title: String, category: String, content: String): Result<Unit> {
        var path = "users/unauthenticated/libraryItems"
        return try {
            val uid = requireUserId()
            val safeTitle = title.trim().ifBlank { "Saved Material" }.take(140)
            val safeCategory = if (category in listOf("chat", "council", "work")) category else "chat"
            val safeContent = content.trim().take(16000)
            require(safeContent.isNotEmpty()) { "Content cannot be empty." }

            val docRef = userRoot(uid).collection("libraryItems").document()
            path = docRef.path

            val payload = mapOf(
                "userId" to uid,
                "title" to safeTitle,
                "category" to safeCategory,
                "content" to safeContent,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteLibraryItem(itemId: String): Result<Unit> {
        var path = "users/unauthenticated/libraryItems/$itemId"
        return try {
            val uid = requireUserId()
            require(itemId.isNotBlank()) { "Library item ID cannot be blank." }
            val docRef = userRoot(uid).collection("libraryItems").document(itemId)
            path = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    companion object {
        fun unwrapFirestoreException(error: Throwable): FirebaseFirestoreException? =
            generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
    }
}
