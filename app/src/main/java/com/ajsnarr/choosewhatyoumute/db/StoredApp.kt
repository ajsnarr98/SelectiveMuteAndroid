package com.ajsnarr.choosewhatyoumute.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="apps")
data class StoredApp (

    @PrimaryKey
    var packageName: String = "", // unique package labelName

    @ColumnInfo
    var isMuted: Boolean = true
)