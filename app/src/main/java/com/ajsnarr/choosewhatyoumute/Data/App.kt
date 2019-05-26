package com.ajsnarr.choosewhatyoumute.data

import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.ajsnarr.choosewhatyoumute.db.StoredApp

/**
 * An app installed on the phone.
 *
 * Can be selectively set to vibrate or muted.
 */
data class App(
    var packageName: String = "", // unique package labelName
    var isMuted: Boolean = true,
    var labelName: String = "empty",
    var icon: Drawable? = null
) {

    companion object {
        /**
         * Creates an app from an app stored in the DB.
         */
        fun fromDBObj(dbApp: StoredApp,
                    labelName: String = "",
                    icon: Drawable? = null): App {

            // use package name for label name if label is not included
            val lblName = if (labelName == "") dbApp.packageName else labelName
            return App(packageName=dbApp.packageName, isMuted=dbApp.isMuted,
                labelName=lblName, icon=icon)
         }
    }

    fun toDBObj() = StoredApp(packageName=packageName, isMuted=isMuted)
}