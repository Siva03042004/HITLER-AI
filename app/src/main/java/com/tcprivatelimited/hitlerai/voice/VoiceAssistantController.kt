package com.tcprivatelimited.hitlerai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import com.tcprivatelimited.hitlerai.data.model.WorkspaceCatalog
import java.util.Locale

class VoiceAssistantController(
    private val context: Context,
    private val onDictationResult: (String) -> Unit,
    private val onWakeCommand: (agentName: String, command: String) -> Unit,
    private val onStatusMessage: (String) -> Unit,
    private val onWakeStateChanged: (Boolean) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var wakeActive = false
    private var pendingCommandAgent: String? = null
    private var singleShotMode = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ttsReady = true
            }
        }
    }

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startOneShotDictation() {
        if (!isAvailable()) {
            onStatusMessage("Speech recognition is not available on this device.")
            return
        }
        singleShotMode = true
        pendingCommandAgent = null
        startListeningInternal()
    }

    fun setWakeListening(enabled: Boolean) {
        if (!isAvailable()) {
            onStatusMessage("Voice wake-word listening is not supported on this device.")
            onWakeStateChanged(false)
            return
        }
        if (wakeActive == enabled) return
        wakeActive = enabled
        onWakeStateChanged(enabled)
        if (enabled) {
            singleShotMode = false
            pendingCommandAgent = null
            onStatusMessage("Wake-word listening enabled. Say Jarvis, Vision, Ultron, Friday, or Edith.")
            startListeningInternal()
        } else {
            stopRecognizer()
            onStatusMessage("Wake-word listening stopped.")
        }
    }

    private fun speak(text: String) {
        if (ttsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "hitler_ai_wake")
        }
    }

    private fun startListeningInternal() {
        mainHandler.post {
            stopRecognizer()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            speechRecognizer = recognizer
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit

                override fun onError(error: Int) {
                    if (singleShotMode) {
                        singleShotMode = false
                        onStatusMessage("Could not hear speech. Please try again.")
                    } else if (wakeActive) {
                        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                            setWakeListening(false)
                            onStatusMessage("Microphone permission is required for wake-word listening.")
                        } else {
                            scheduleWakeRestart(800L)
                        }
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val phrase = matches?.firstOrNull()?.trim().orEmpty()

                    if (singleShotMode) {
                        singleShotMode = false
                        if (phrase.isNotEmpty()) {
                            onDictationResult(phrase)
                        }
                        if (wakeActive) {
                            scheduleWakeRestart(600L)
                        }
                        return
                    }

                    val activePending = pendingCommandAgent
                    if (activePending != null) {
                        pendingCommandAgent = null
                        if (phrase.isNotEmpty()) {
                            onWakeCommand(activePending, phrase)
                        }
                        if (wakeActive) {
                            scheduleWakeRestart(800L)
                        }
                        return
                    }

                    if (wakeActive && phrase.isNotEmpty()) {
                        val matchedAgent = WorkspaceCatalog.specialists.firstOrNull { agent ->
                            Regex("\\b${Regex.escape(agent.name)}\\b", RegexOption.IGNORE_CASE)
                                .containsMatchIn(phrase)
                        }
                        if (matchedAgent != null) {
                            val remainder = phrase
                                .replace(Regex("\\b${Regex.escape(matchedAgent.name)}\\b", RegexOption.IGNORE_CASE), "")
                                .trimStart(',', ' ')
                                .trim()
                            speak("Yes Boss. How can I help you?")
                            if (remainder.isNotEmpty()) {
                                mainHandler.postDelayed({
                                    onWakeCommand(matchedAgent.name, remainder)
                                    if (wakeActive) scheduleWakeRestart(1200L)
                                }, 800L)
                            } else {
                                pendingCommandAgent = matchedAgent.name
                                scheduleWakeRestart(1400L)
                            }
                            return
                        }
                    }

                    if (wakeActive) {
                        scheduleWakeRestart(650L)
                    }
                }
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            try {
                recognizer.startListening(intent)
            } catch (_: Exception) {
                onStatusMessage("Voice input could not start.")
            }
        }
    }

    private fun scheduleWakeRestart(delayMs: Long) {
        mainHandler.postDelayed({
            if (wakeActive && !singleShotMode) {
                startListeningInternal()
            }
        }, delayMs)
    }

    private fun stopRecognizer() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
            // Ignore cleanup errors
        }
        speechRecognizer = null
    }

    fun destroy() {
        wakeActive = false
        mainHandler.removeCallbacksAndMessages(null)
        stopRecognizer()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
            // Ignore cleanup errors
        }
        tts = null
    }
}
