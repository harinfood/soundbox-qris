package com.contoh.soundbox

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Urutan setiap pembayaran:
 * 1. Volume notifikasi dinaikkan ke maksimum
 * 2. Suara dibunyikan: mp3 pilihan, atau nada notifikasi HP jika belum memilih
 * 3. TTS membacakan nominal
 * 4. Setelah semua selesai, volume dikembalikan ke semula
 */
class AmountSpeaker(private val context: Context) {

    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private val atribut = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private var tts: TextToSpeech? = null
    private var siap = false
    private val antrean = mutableListOf<String>()
    private var prosesAktif = 0
    private var volumeAsli = -1
    private var urutan = 0

    init {
        tts = TextToSpeech(context) { status ->
            val bahasa = if (status == TextToSpeech.SUCCESS) tts?.setLanguage(Locale("id", "ID")) else null
            siap = status == TextToSpeech.SUCCESS &&
                   bahasa != TextToSpeech.LANG_MISSING_DATA &&
                   bahasa != TextToSpeech.LANG_NOT_SUPPORTED

            if (siap) {
                tts?.setAudioAttributes(atribut)
                tts?.setSpeechRate(0.9f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}

                    override fun onDone(utteranceId: String?) {
                        handler.post { selesai() }
                    }

                    override fun onError(utteranceId: String?) {
                        handler.post { selesai() }
                    }
                })
                antrean.forEach { bicara(it) }
                antrean.clear()
            } else {
                handler.post {
                    antrean.clear()
                    prosesAktif = 0
                    pulihkanVolume()
                }
            }
        }
    }

    fun ucapkan(kata: String) {
        handler.post {
            prosesAktif++
            if (volumeAsli < 0) {
                volumeAsli = audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            }
            audio.setStreamVolume(
                AudioManager.STREAM_NOTIFICATION,
                audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
                0
            )
            mainkanSuara { bicara(kata) }
        }
    }

    private fun mainkanSuara(lanjut: () -> Unit) {
        // Coba mp3 pilihan dulu. Jika gagal dibuka (file dihapus, izin hilang), pakai nada HP.
        val kustom = Pengaturan.suaraKustom(context)?.let { Uri.parse(it) }
        if (kustom != null && putarUri(kustom, lanjut)) return

        val bawaan = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (bawaan == null || !putarUri(bawaan, lanjut)) lanjut()
    }

    /** Mengembalikan false jika file tidak bisa dibuka. Dalam kasus itu, lanjut tidak dipanggil. */
    private fun putarUri(uri: Uri, lanjut: () -> Unit): Boolean {
        val pemutar = MediaPlayer()
        return try {
            pemutar.setAudioAttributes(atribut)
            pemutar.setDataSource(context, uri)
            pemutar.setOnCompletionListener {
                it.release()
                lanjut()
            }
            pemutar.setOnErrorListener { p, _, _ ->
                p.release()
                lanjut()
                true
            }
            pemutar.prepare()
            pemutar.start()
            true
        } catch (e: Exception) {
            pemutar.release()
            false
        }
    }

    private fun bicara(kata: String) {
        if (siap) {
            tts?.speak(kata, TextToSpeech.QUEUE_ADD, null, "pembayaran-${urutan++}")
        } else {
            antrean.add(kata)
        }
    }

    private fun selesai() {
        prosesAktif = (prosesAktif - 1).coerceAtLeast(0)
        if (prosesAktif == 0) pulihkanVolume()
    }

    private fun pulihkanVolume() {
        if (volumeAsli >= 0) {
            audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volumeAsli, 0)
            volumeAsli = -1
        }
    }

    fun berhenti() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        siap = false
        pulihkanVolume()
    }
}
