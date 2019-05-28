package com.ajsnarr.choosewhatyoumute.service

import android.content.Context
import android.media.AudioManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.ajsnarr.choosewhatyoumute.db.AppDAO
import com.ajsnarr.choosewhatyoumute.db.AppDatabase
import com.ajsnarr.choosewhatyoumute.db.StoredApp
import io.reactivex.schedulers.Schedulers

class NotificationListenerService : NotificationListenerService() {

    private lateinit var audioManager: AudioManager
    private lateinit var db: AppDAO


    override fun onListenerConnected() {

        Log.i("NotificationLService", "onListenerConnected")

        // get db instance
        db = AppDatabase.getInstance(this.application).appDAO

        // get audio manager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {

        Log.i("NotificationLService", "onNotificationPosted")

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
                    }
                }
        }


    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

}
