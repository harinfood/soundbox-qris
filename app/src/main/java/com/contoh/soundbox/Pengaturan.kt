package com.contoh.soundbox

import android.content.Context

object Pengaturan {
    private const val NAMA_FILE = "soundbox_pengaturan"
    private const val KUNCI_PAKET = "paket_dipantau"

    /** Kosong berarti semua aplikasi dipantau. */
    fun paketDipantau(context: Context): Set<String> =
        context.getSharedPreferences(NAMA_FILE, Context.MODE_PRIVATE)
            .getStringSet(KUNCI_PAKET, emptySet()) ?: emptySet()

    fun simpanPaket(context: Context, paket: Set<String>) {
        context.getSharedPreferences(NAMA_FILE, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KUNCI_PAKET, paket.toSet())
            .apply()
    }
}
