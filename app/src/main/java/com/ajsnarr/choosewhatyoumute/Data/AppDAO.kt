package com.ajsnarr.choosewhatyoumute.Data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AppDAO {

    /**
     * Try to insert an app into the DB.
     */
    @Insert
    fun insert(app: App)

    /**
     * Gets an app by package name.
     */
    @Query("SELECT * FROM selective_mute_table WHERE id = :packageName")
    fun get(packageName: String): App?

    /**
     * Get all apps where muted is false.
     */
    @Query("SELECT * FROM selective_mute_table WHERE isMuted = 0")
    fun getAllUnmuted(): LiveData<List<App>>

    /**
     * Clear all apps from the table.
     */
    @Query("DELETE FROM selective_mute_table")
    fun clearAll()
}