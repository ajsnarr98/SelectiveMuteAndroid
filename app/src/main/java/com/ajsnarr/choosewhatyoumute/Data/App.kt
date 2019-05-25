package com.ajsnarr.choosewhatyoumute.Data

import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * An app installed on the phone.
 *
 * Can be selectively set to vibrate or muted.
 */
@Entity(tableName = "selective_mute_table")
data class App(

    @PrimaryKey
    var id: String = "", // unique package labelName

    @ColumnInfo
    var isMuted: Boolean = true,

    @Ignore
    var labelName: String = "",

    @Ignore
    var icon: Drawable? = null
) {

    companion object {

        /**
         * Use this instead of the primary constructor.
         *
         * Checks DB for app with this package name, and keeps the same
         * 'muted' value if it exists.
         */
         suspend fun getApp(db: AsyncAppDAO,
                    packageName: String,
                    labelName: String = "",
                    icon: Drawable? = null,
                    isMuted: Boolean = true): App {

            var app = db.get(packageName)
            val lblName = if (labelName == "") packageName else labelName

            if (app != null) {
                // If already in db
                return app.apply { this.icon = icon; this.labelName = lblName }

            } else {
                // If not in db yet
                return App(id = packageName, isMuted = isMuted,
                    labelName = lblName, icon=icon)
            }
         }
    }
}