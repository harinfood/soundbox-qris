package com.contoh.soundbox

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class AmountSpeaker(context: Context) {

    private var tts: TextToSpeech? = null
    private var siap = false
    private val antrean = mutableListOf<String>()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val hasil = tts?.setLanguage(Locale("id", "ID"))
                siap = hasil != TextToSpeech.LANG_MISSING_DATA &&
                       hasil != TextToSpeech.LANG_NOT_SUPPORTED
                if (siap) {
                    tts?.setSpeechRate(0.9f)
                    antrean.forEach { bacakan(it) }
                    antrean.clear()
                }
            }
        }
    }

    fun ucapkan(teks: String) {
        if (siap) bacakan(teks) else antrean.add(teks)
    }

    private fun bacakan(teks: String) {
        // QUEUE_ADD supaya beberapa pembayaran berurutan dibacakan satu per satu
        tts?.speak(teks, TextToSpeech.QUEUE_ADD, null, System.nanoTime().toString())
    }

    fun berhenti() {
        tts?.stop()
        tts?.shutdown()
    }
}
