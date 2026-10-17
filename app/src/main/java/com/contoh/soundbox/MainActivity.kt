package com.contoh.soundbox

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button

class MainActivity : Activity() {

    private lateinit var speaker: AmountSpeaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        speaker = AmountSpeaker(applicationContext)

        findViewById<Button>(R.id.btnIzin).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        findViewById<Button>(R.id.btnTes).setOnClickListener {
            speaker.ucapkan("Pembayaran sebesar dua puluh lima ribu rupiah diterima")
        }
    }
}
