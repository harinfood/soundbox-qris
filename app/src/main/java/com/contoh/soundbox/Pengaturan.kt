package com.contoh.soundbox

import android.content.Context
import android.content.SharedPreferences

object Pengaturan {
    private const val NAMA_FILE = "soundbox_pengaturan"
    private const val KUNCI_PAKET = "paket_dipantau"
    private const val KUNCI_SUARA = "suara_kustom"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(NAMA_FILE, Context.MODE_PRIVATE)

    /** Kosong berarti semua aplikasi dipantau. */
    fun paketDipantau(context: Context): Set<String> =
        prefs(context).getStringSet(KUNCI_PAKET, emptySet()) ?: emptySet()

    fun simpanPaket(context: Context, paket: Set<String>) {
        prefs(context).edit().putStringSet(KUNCI_PAKET, paket.toSet()).apply()
    }

    /** URI file mp3 pilihan. Null berarti memakai nada notifikasi HP. */
    fun suaraKustom(context: Context): String? =
        prefs(context).getString(KUNCI_SUARA, null)

    fun simpanSuaraKustom(context: Context, uri: String?) {
        val editor = prefs(context).edit()
        if (uri == null) editor.remove(KUNCI_SUARA) else editor.putString(KUNCI_SUARA, uri)
        editor.apply()
    }
}
