import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

methods = """
    private fun startListening() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {
                    voiceText?.text = "Ouvindo..."
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {
                    val scale = 1.0f + (rmsdB / 10f).coerceIn(0f, 1f)
                    keyboardView.findViewById<android.view.View>(R.id.voice_pulse_bg)?.animate()?.scaleX(scale)?.scaleY(scale)?.setDuration(50)?.start()
                }
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    voiceText?.text = "Processando..."
                }
                override fun onError(error: Int) {
                    val errorMsg = when(error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Não entendi"
                        SpeechRecognizer.ERROR_NETWORK -> "Erro de rede"
                        SpeechRecognizer.ERROR_AUDIO -> "Erro de áudio"
                        else -> "Erro: $error"
                    }
                    voiceText?.text = errorMsg
                    isListening = false
                    
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(1500)
                        if (!isListening && voiceRoot.visibility == android.view.View.VISIBLE) {
                            stopListening()
                            voiceRoot.visibility = android.view.View.GONE
                            keyboardRoot.visibility = android.view.View.VISIBLE
                        }
                    }
                }
                override fun onResults(results: android.os.Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        currentInputConnection?.commitText(text + " ", 1)
                        voiceText?.text = text
                    }
                    isListening = false
                    
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(1000)
                        if (!isListening && voiceRoot.visibility == android.view.View.VISIBLE) {
                            stopListening()
                            voiceRoot.visibility = android.view.View.GONE
                            keyboardRoot.visibility = android.view.View.VISIBLE
                        }
                    }
                }
                override fun onPartialResults(partialResults: android.os.Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        voiceText?.text = matches[0]
                    }
                }
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            voiceText?.text = "Preparando..."
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Reconhecimento de voz não disponível", android.widget.Toast.LENGTH_SHORT).show()
            voiceRoot.visibility = android.view.View.GONE
            keyboardRoot.visibility = android.view.View.VISIBLE
        }
    }

    private fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }
    
    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
"""

content = content[:content.rfind('}')] + methods + "\n}"

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
