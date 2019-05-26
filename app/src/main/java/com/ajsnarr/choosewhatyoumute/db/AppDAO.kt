package com.ajsnarr.choosewhatyoumute.db

import androidx.room.*
import io.reactivex.Single

@Dao
interface AppDAO {

    /**
     * Try to insert an app into the DB.
     */
    @Insert
    fun insert(app: StoredApp)

    /**
     * Try to insert an app into the DB. If it already exists, update
     * the existing app.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(app: StoredApp)

    /**
     * Gets an app by package name.
     */
    @Query("SELECT * FROM apps WHERE packageName = :packageName")
    fun get(packageName: String): StoredApp?

    /**
     * Get all apps.
     */
    @Query("SELECT * FROM apps")
    fun getAll(): Single<List<StoredApp>>

    /**
     * Get all apps where muted is false.
     */
    @Query("SELECT * FROM apps WHERE isMuted = 0")
    fun getAllUnmuted(): Single<List<StoredApp>>

    /**
     * Clear all apps from the table.
     */
    @Query("DELETE FROM apps")
    fun clearAll()
}