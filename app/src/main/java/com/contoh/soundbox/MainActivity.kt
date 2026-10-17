package com.contoh.soundbox

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView

class MainActivity : Activity() {

    companion object {
        private const val KODE_PILIH_MP3 = 101
    }

    private lateinit var speaker: AmountSpeaker
    private lateinit var daftarAplikasi: ListView
    private lateinit var txtSuara: TextView
    private var daftarPaket: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        speaker = AmountSpeaker(applicationContext)
        daftarAplikasi = findViewById(R.id.listAplikasi)
        txtSuara = findViewById(R.id.txtSuara)

        findViewById<Button>(R.id.btnIzin).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        findViewById<Button>(R.id.btnTes).setOnClickListener {
            speaker.ucapkan("Pembayaran sebesar dua puluh lima ribu rupiah diterima")
        }

        findViewById<Button>(R.id.btnPilihMp3).setOnClickListener {
            bukaPemilihMp3()
        }

        findViewById<Button>(R.id.btnResetMp3).setOnClickListener {
            Pengaturan.simpanSuaraKustom(this, null)
            tampilkanSuara()
        }

        muatAplikasi()
        tampilkanSuara()
    }

    private fun bukaPemilihMp3() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }
        startActivityForResult(intent, KODE_PILIH_MP3)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == KODE_PILIH_MP3 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            try {
                // Izin baca dibuat permanen agar tetap bisa dibuka setelah aplikasi ditutup
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                // Sumber tertentu tidak mendukung izin permanen, tetap simpan URI-nya
            }
            Pengaturan.simpanSuaraKustom(this, uri.toString())
            tampilkanSuara()
        }
    }

    private fun tampilkanSuara() {
        val tersimpan = Pengaturan.suaraKustom(this)
        txtSuara.text = if (tersimpan == null) {
            "Suara: nada notifikasi HP (default)"
        } else {
            "Suara: ${namaFile(Uri.parse(tersimpan))}"
        }
    }

    private fun namaFile(uri: Uri): String {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) return it.getString(0)
        }
        return uri.lastPathSegment ?: "file dipilih"
    }

    private fun muatAplikasi() {
        val pm = packageManager
        val aplikasi = pm.getInstalledApplications(0)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null && it.packageName != packageName }
            .sortedBy { pm.getApplicationLabel(it).toString().lowercase() }

        daftarPaket = aplikasi.map { it.packageName }
        val label = aplikasi.map { pm.getApplicationLabel(it).toString() }
        val dipilih = Pengaturan.paketDipantau(this)

        daftarAplikasi.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        daftarAplikasi.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            label
        )
        daftarPaket.forEachIndexed { i, paket ->
            daftarAplikasi.setItemChecked(i, paket in dipilih)
        }
        daftarAplikasi.setOnItemClickListener { _, _, _, _ -> simpanPilihan() }
    }

    private fun simpanPilihan() {
        val terpilih = mutableSetOf<String>()
        val posisi = daftarAplikasi.checkedItemPositions
        for (i in daftarPaket.indices) {
            if (posisi.get(i)) terpilih.add(daftarPaket[i])
        }
        Pengaturan.simpanPaket(this, terpilih)
    }
}
