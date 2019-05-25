package com.ajsnarr.choosewhatyoumute

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        // put connecting code here
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {

        if (sbn == null) return

        val pkg: String? = sbn?.packageName // the package that sent
                                            // the notification

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

}
