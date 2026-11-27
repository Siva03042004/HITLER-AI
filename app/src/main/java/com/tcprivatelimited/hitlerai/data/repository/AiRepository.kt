package com.tcprivatelimited.hitlerai.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.tcprivatelimited.hitlerai.data.model.AiSpecialist
import com.tcprivatelimited.hitlerai.data.model.ChatMessage
import com.tcprivatelimited.hitlerai.data.model.WorkspaceCatalog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val AI_TAG = "AiRepository"
private const val MODEL_NAME = "gemini-3.8-flash"

data class CouncilProgress(
    val prompt: String,
    val activeSpecialist: String?,
    val turns: Map<String, String>,
    val synthesis: String,
    val isComplete: Boolean
)

class AiRepository {

    private val defaultModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            systemInstruction = content {
                text(
                    "You are the AI assistant inside Hitler AI Workspace by TC Private Limited. " +
                        "Be clear, helpful, accurate, structured, and constructive. " +
                        "Never fabricate tool actions you did not perform."
                )
            }
        )
    }

    private fun specialistModel(specialist: AiSpecialist) = Firebase.ai.generativeModel(
        modelName = MODEL_NAME,
        systemInstruction = content {
            text(
                "You are ${specialist.name}, the ${specialist.role} specialist in Hitler AI V4. " +
                    "${specialist.councilStyle} You are one specialist persona in a collaborative AI workspace, not a real person. " +
                    "Respond clearly in 2-4 concise, useful paragraphs. Do not claim tools or actions you did not perform."
            )
        }
    )

    private val headModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            systemInstruction = content {
                text(
                    "You are Head, the chairperson of a five-specialist AI council (Edith, Vision, Ultron, Friday, Jarvis). " +
                        "Synthesize the specialists' contributions into one clear, structured answer. " +
                        "Resolve disagreements, call out key uncertainties, and give concrete next steps. Do not invent consensus."
                )
            }
        )
    }

    fun streamChatReply(
        history: List<ChatMessage>,
        userPrompt: String,
        activeSpecialist: AiSpecialist?
    ): Flow<String> = flow {
        val model = if (activeSpecialist != null) specialistModel(activeSpecialist) else defaultModel
        val recentHistory = history.takeLast(14).map { msg ->
            content(role = if (msg.role == "assistant") "model" else "user") {
                text(msg.content)
            }
        }
        val chat = model.startChat(history = recentHistory)
        val accumulated = StringBuilder()

        try {
            withTimeout(60_000L) {
                chat.sendMessageStream(userPrompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("Gemini returned an empty response.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Chat streaming timed out", e)
            throw IllegalStateException("Response timed out after 60 seconds. Please try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Chat streaming failed", e)
            throw e
        }
    }

    suspend fun runCouncilSession(
        prompt: String,
        onProgress: (CouncilProgress) -> Unit
    ): CouncilProgress {
        val turns = linkedMapOf<String, String>()
        val transcript = StringBuilder()

        try {
            for (specialist in WorkspaceCatalog.councilOrder) {
                onProgress(
                    CouncilProgress(
                        prompt = prompt,
                        activeSpecialist = specialist.name,
                        turns = turns.toMap(),
                        synthesis = "",
                        isComplete = false
                    )
                )

                val contextText = if (transcript.isEmpty()) "(No prior discussion)" else transcript.toString()
                val turnPrompt = "User request: $prompt\n\nCouncil discussion so far:\n$contextText"

                val response = withTimeout(60_000L) {
                    specialistModel(specialist).generateContent(turnPrompt)
                }
                val reply = response.text?.trim().orEmpty().ifBlank {
                    "No perspective returned by ${specialist.name}."
                }
                turns[specialist.name] = reply
                transcript.append("${specialist.name}: $reply\n\n")

                onProgress(
                    CouncilProgress(
                        prompt = prompt,
                        activeSpecialist = specialist.name,
                        turns = turns.toMap(),
                        synthesis = "",
                        isComplete = false
                    )
                )
            }

            onProgress(
                CouncilProgress(
                    prompt = prompt,
                    activeSpecialist = "Head",
                    turns = turns.toMap(),
                    synthesis = "",
                    isComplete = false
                )
            )

            val synthesisPrompt = buildString {
                append("Original request: $prompt\n\n")
                turns.forEach { (agent, reply) ->
                    append("$agent: $reply\n\n")
                }
            }

            val synthesisResponse = withTimeout(60_000L) {
                headModel.generateContent(synthesisPrompt)
            }
            val synthesisText = synthesisResponse.text?.trim().orEmpty().ifBlank {
                "Council synthesis was empty."
            }

            val finalProgress = CouncilProgress(
                prompt = prompt,
                activeSpecialist = null,
                turns = turns.toMap(),
                synthesis = synthesisText,
                isComplete = true
            )
            onProgress(finalProgress)
            return finalProgress
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Council generation timed out", e)
            throw IllegalStateException("Five-AI Council timed out. Please try a shorter prompt or retry.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Council generation failed", e)
            throw e
        }
    }
}
