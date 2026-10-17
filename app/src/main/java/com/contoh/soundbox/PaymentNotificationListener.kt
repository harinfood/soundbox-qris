package com.contoh.soundbox

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class PaymentNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "SoundboxQRIS"
        private const val MAKS_ID_DIINGAT = 200
    }

    private var speaker: AmountSpeaker? = null
    private val idTerbaca = LinkedHashSet<String>()

    override fun onListenerConnected() {
        if (speaker == null) speaker = AmountSpeaker(applicationContext)
        Log.d(TAG, "Listener aktif")
    }

    override fun onListenerDisconnected() {
        speaker?.berhenti()
        speaker = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val dipantau = Pengaturan.paketDipantau(this)
        if (dipantau.isNotEmpty() && sbn.packageName !in dipantau) return

        val extras = sbn.notification.extras
        val judul = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val isi = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val teks = "$judul $isi"

        Log.d(TAG, "pkg=${sbn.packageName} | $teks")

        val pembayaran = PaymentParser.parse(teks) ?: return

        // Notifikasi yang sama sering muncul ulang, jadi cek berdasarkan nomor transaksi
        if (!idTerbaca.add(pembayaran.idTransaksi)) return
        if (idTerbaca.size > MAKS_ID_DIINGAT) idTerbaca.remove(idTerbaca.first())

        val kata = IndonesianNumber.toWords(pembayaran.nominal)
        val penyiar = speaker ?: AmountSpeaker(applicationContext).also { speaker = it }
        penyiar.ucapkan("Pembayaran sebesar $kata rupiah diterima")
    }
}

data class Pembayaran(val nominal: Long, val idTransaksi: String)

object PaymentParser {
    // Format ShopeePay: "Pembayaran sebesar Rp367 diterima"
    private val polaShopee = Regex(
        """Pembayaran sebesar\s*Rp\s*([\d.]+)\s*diterima""",
        RegexOption.IGNORE_CASE
    )

    // Format umum untuk aplikasi lain: angka Rp dengan kata "diterima" atau "masuk"
    private val polaUmum = Regex("""Rp\s*([\d.]+)""", RegexOption.IGNORE_CASE)
    private val kataMasuk = Regex("""diterima|masuk""", RegexOption.IGNORE_CASE)

    // Contoh isi: "... pada transaksi 100484556171652396."
    private val polaId = Regex("""transaksi\s+(\d+)""", RegexOption.IGNORE_CASE)

    fun parse(teks: String): Pembayaran? {
        val angka = polaShopee.find(teks)?.groupValues?.get(1)
            ?: polaUmum.find(teks)
                ?.takeIf { kataMasuk.containsMatchIn(teks) }
                ?.groupValues?.get(1)
            ?: return null

        val nominal = angka.replace(".", "").toLongOrNull() ?: return null
        if (nominal <= 0) return null

        val id = polaId.find(teks)?.groupValues?.get(1) ?: teks
        return Pembayaran(nominal, id)
    }
}
