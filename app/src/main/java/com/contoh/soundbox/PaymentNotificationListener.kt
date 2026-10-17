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
        val extras = sbn.notification.extras
        val judul = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val isi = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val teks = "$judul $isi"

        // Log semua notifikasi untuk memantau format teks
        Log.d(TAG, "pkg=${sbn.packageName} | $teks")

        val pembayaran = PaymentParser.parse(teks) ?: return

        // Notifikasi yang sama sering muncul ulang, jadi cek berdasarkan nomor transaksi
        if (!idTerbaca.add(pembayaran.idTransaksi)) return
        if (idTerbaca.size > MAKS_ID_DIINGAT) idTerbaca.remove(idTerbaca.first())

        val kata = IndonesianNumber.toWords(pembayaran.nominal)
        speaker?.ucapkan("Pembayaran sebesar $kata rupiah diterima")
    }
}

data class Pembayaran(val nominal: Long, val idTransaksi: String)

object PaymentParser {
    // Contoh judul: "Pembayaran sebesar Rp367 diterima" atau "Pembayaran sebesar Rp1.250.000 diterima"
    private val polaNominal = Regex(
        """Pembayaran sebesar\s*Rp\s*([\d.]+)\s*diterima""",
        RegexOption.IGNORE_CASE
    )

    // Contoh isi: "... pada transaksi 100484556171652396."
    private val polaId = Regex("""transaksi\s+(\d+)""", RegexOption.IGNORE_CASE)

    fun parse(teks: String): Pembayaran? {
        val cocok = polaNominal.find(teks) ?: return null
        val nominal = cocok.groupValues[1].replace(".", "").toLongOrNull() ?: return null
        val id = polaId.find(teks)?.groupValues?.get(1) ?: teks
        return Pembayaran(nominal, id)
    }
}
