package com.example.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SpeechCaptureHelper(
    private val context: Context,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onErrorMessage: (String) -> Unit,
    private val onListeningChanged: (Boolean) -> Unit
) {
    private var recognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        if (!isAvailable()) {
            onErrorMessage("El reconocimiento de voz no está disponible en este dispositivo. Puedes escribir o editar el comando manualmente.")
            onListeningChanged(false)
            return
        }

        stopListening()
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = speechRecognizer

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                onListeningChanged(true)
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                onListeningChanged(false)
            }

            override fun onError(error: Int) {
                onListeningChanged(false)
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No entendí el audio. Intenta de nuevo o escríbelo abajo."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz. Intenta de nuevo o escríbelo abajo."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permiso de micrófono requerido. Usa el campo de texto."
                    else -> "Reconocimiento no disponible ($error). Puedes escribir el comando directamente."
                }
                onErrorMessage(msg)
            }

            override fun onResults(results: Bundle?) {
                onListeningChanged(false)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull().orEmpty()
                if (text.isNotBlank()) {
                    onFinalResult(text)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull().orEmpty()
                if (text.isNotBlank()) {
                    onPartialResult(text)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        runCatching {
            onListeningChanged(true)
            speechRecognizer.startListening(intent)
        }.onFailure {
            onListeningChanged(false)
            onErrorMessage("No se pudo iniciar el micrófono. Escribe tu tarea o hábito directamente.")
        }
    }

    fun stopListening() {
        runCatching {
            recognizer?.stopListening()
            recognizer?.destroy()
        }
        recognizer = null
        onListeningChanged(false)
    }

    fun destroy() {
        stopListening()
    }
}
