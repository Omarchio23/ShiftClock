package es.aipp.shiftclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("ShiftClock", "Dispositivo reiniciado. Reprogramando alarmas...")
            // Para la versión escalable con Room, aquí consultaríamos la BD y llamaríamos a AlarmScheduler
        }
    }
}
