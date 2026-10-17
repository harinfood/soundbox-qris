package com.contoh.soundbox

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import java.io.File

object Pengaturan {
    private const val NAMA_FILE = "soundbox_pengaturan"
    private const val KUNCI_PAKET = "paket_dipantau"
    private const val KUNCI_NAMA_SUARA = "nama_suara"
    private const val NAMA_SALINAN = "suara_kustom.mp3"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(NAMA_FILE, Context.MODE_PRIVATE)

    /** Kosong berarti semua aplikasi dipantau. */
    fun paketDipantau(context: Context): Set<String> =
        prefs(context).getStringSet(KUNCI_PAKET, emptySet()) ?: emptySet()

    fun simpanPaket(context: Context, paket: Set<String>) {
        prefs(context).edit().putStringSet(KUNCI_PAKET, paket.toSet()).apply()
    }

    /** Salinan mp3 di penyimpanan internal aplikasi. */
    fun fileSuara(context: Context): File = File(context.filesDir, NAMA_SALINAN)

    fun adaSuaraKustom(context: Context): Boolean {
        val berkas = fileSuara(context)
        return berkas.exists() && berkas.length() > 0
    }

    fun namaSuara(context: Context): String? =
        prefs(context).getString(KUNCI_NAMA_SUARA, null)

    /**
     * Menyalin mp3 pilihan ke penyimpanan internal aplikasi.
     * Dengan begitu file tetap bisa diputar tanpa bergantung pada izin file picker.
     */
    fun salinSuara(context: Context, uri: Uri, nama: String?): Boolean {
        return try {
            val masuk = context.contentResolver.openInputStream(uri) ?: return false
            masuk.use { input ->
                fileSuara(context).outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            prefs(context).edit().putString(KUNCI_NAMA_SUARA, nama ?: "file mp3").apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun hapusSuara(context: Context) {
        fileSuara(context).delete()
        prefs(context).edit().remove(KUNCI_NAMA_SUARA).apply()
    }
}
