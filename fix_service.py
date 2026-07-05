import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

import_statement = """import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
"""

content = content.replace("import android.view.inputmethod.InputMethodManager\n", "import android.view.inputmethod.InputMethodManager\n" + import_statement)

# Add speech recognizer fields
fields = """    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var voiceText: TextView? = null
"""

content = content.replace("private var waveJob: Job? = null", fields + "\n    private var waveJob: Job? = null")

# Setup voice text reference
content = content.replace("previewPopup = keyboardView.findViewById(R.id.key_preview_popup)", "previewPopup = keyboardView.findViewById(R.id.key_preview_popup)\n            voiceText = keyboardView.findViewById(R.id.voice_text)")

close_btn_replacement = """            keyboardView.findViewById<View>(R.id.btn_close_voice)?.setOnClickListener {
                stopListening()
                waveJob?.cancel()
                voiceRoot.visibility = View.GONE
                keyboardRoot.visibility = View.VISIBLE
            }
            keyboardView.findViewById<View>(R.id.btn_mic_action)?.setOnClickListener {
                if (isListening) stopListening() else startListening()
            }"""
content = content.replace("keyboardView.findViewById<View>(R.id.btn_close_voice)?.setOnClickListener {\n                waveJob?.cancel()\n                voiceRoot.visibility = View.GONE\n                keyboardRoot.visibility = View.VISIBLE\n            }", close_btn_replacement)


mic_click_old = """        val micKey = view.findViewById<FrameLayout>(R.id.key_mic)
        micKey?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            keyboardRoot.visibility = View.GONE
            voiceRoot.visibility = View.VISIBLE
            startWaveAnimation(view.rootView)
            Toast.makeText(this, "Ouvindo...", Toast.LENGTH_SHORT).show()
        }"""

mic_click_new = """        val micKey = view.findViewById<FrameLayout>(R.id.key_mic)
        micKey?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, PermissionActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this, "Permissão de microfone necessária", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            
            keyboardRoot.visibility = View.GONE
            voiceRoot.visibility = View.VISIBLE
            voiceText?.text = "Fale agora..."
            startListening()
            startWaveAnimation(view.rootView)
        }"""
content = content.replace(mic_click_old, mic_click_new)

# Add speech recognizer methods
methods = """
    private fun startListening() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    voiceText?.text = "Ouvindo..."
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {
                    val scale = 1.0f + (rmsdB / 10f).coerceIn(0f, 1f)
                    keyboardView.findViewById<View>(R.id.voice_pulse_bg)?.animate()?.scaleX(scale)?.scaleY(scale)?.setDuration(50)?.start()
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
                        if (!isListening && voiceRoot.visibility == View.VISIBLE) {
                            stopListening()
                            voiceRoot.visibility = View.GONE
                            keyboardRoot.visibility = View.VISIBLE
                        }
                    }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        currentInputConnection?.commitText(text + " ", 1)
                        voiceText?.text = text
                    }
                    isListening = false
                    
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(1000)
                        if (!isListening && voiceRoot.visibility == View.VISIBLE) {
                            stopListening()
                            voiceRoot.visibility = View.GONE
                            keyboardRoot.visibility = View.VISIBLE
                        }
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        voiceText?.text = matches[0]
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
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
            Toast.makeText(this, "Reconhecimento de voz não disponível", Toast.LENGTH_SHORT).show()
            voiceRoot.visibility = View.GONE
            keyboardRoot.visibility = View.VISIBLE
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
        scope.cancel()
    }
"""

content = content.replace("    override fun onDestroy() {\n        super.onDestroy()\n        scope.cancel()\n    }", methods)

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
