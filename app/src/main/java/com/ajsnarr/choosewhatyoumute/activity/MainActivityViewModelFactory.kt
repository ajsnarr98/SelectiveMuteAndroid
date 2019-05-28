package com.ajsnarr.choosewhatyoumute.activity

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ajsnarr.choosewhatyoumute.data.App
import com.ajsnarr.choosewhatyoumute.db.AppDAO

class MainActivityViewModelFactory(val db: AppDAO,
                                   val installedApps: List<App>,
                                   val application: Application)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(
                db,
                installedApps,
                application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}