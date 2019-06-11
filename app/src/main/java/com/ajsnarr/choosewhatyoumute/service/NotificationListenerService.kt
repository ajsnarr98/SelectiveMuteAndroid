package com.ajsnarr.choosewhatyoumute.service

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.ajsnarr.choosewhatyoumute.db.AppDAO
import com.ajsnarr.choosewhatyoumute.db.AppDatabase
import com.ajsnarr.choosewhatyoumute.db.StoredApp
import io.reactivex.schedulers.Schedulers

private const val TAG = "NotificationLService"

private const val VIBRATE_DELAY_MS: Long = 5000L //ms

class NotificationListenerService : NotificationListenerService() {

    private lateinit var audioManager: AudioManager
    private lateinit var db: AppDAO

    private val handler: Handler = Handler()

    override fun onCreate() {
        Log.i(TAG, "onCreate")
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        val iBinder = super.onBind(intent)
        Log.i(TAG, "onBind")
        return iBinder
    }

    override fun onListenerConnected() {

        Log.i(TAG, "onListenerConnected")

        // get db instance
        db = AppDatabase.getInstance(this.application).appDAO

        // get audio manager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {

        Log.d(TAG, "onNotificationPosted | ID :" + (sbn?.id ?: "") + "\t"
                + (sbn?.notification?.tickerText ?: "")
                + "\t" + (sbn?.packageName ?: ""))

        if (sbn == null) return

        val pkg: String = sbn.packageName // the package that sent
                                           // the notification

        if (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT) {
            db.get(pkg)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .subscribe { dbApp: StoredApp ->
                    // onSuccess
                    if (false == dbApp.isMuted) {
                        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE

                        val silence = Runnable { audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT }
                        handler.postDelayed(silence, VIBRATE_DELAY_MS)
                    }
                    Log.i(TAG, "onNotificationPosted | found app" +
                            "(${dbApp.packageName}) in db (isMuted: ${dbApp.isMuted})")
               }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(TAG, "onNotificationRemoved ID :" + sbn.id + "\t" + sbn.notification.tickerText + "\t" + sbn.packageName)
    }

}
