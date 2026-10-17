package com.contoh.soundbox

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView

class MainActivity : Activity() {

    private lateinit var speaker: AmountSpeaker
    private lateinit var daftarAplikasi: ListView
    private var daftarPaket: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        speaker = AmountSpeaker(applicationContext)
        daftarAplikasi = findViewById(R.id.listAplikasi)

        findViewById<Button>(R.id.btnIzin).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        findViewById<Button>(R.id.btnTes).setOnClickListener {
            speaker.ucapkan("Pembayaran sebesar dua puluh lima ribu rupiah diterima")
        }

        muatAplikasi()
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
