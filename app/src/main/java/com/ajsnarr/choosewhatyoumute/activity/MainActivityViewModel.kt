package com.ajsnarr.choosewhatyoumute.activity

import android.app.Application
import android.text.BoringLayout
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ajsnarr.choosewhatyoumute.data.App
import com.ajsnarr.choosewhatyoumute.db.AppDAO
import com.ajsnarr.choosewhatyoumute.db.StoredApp
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*

class MainActivityViewModel(val db: AppDAO, installedApps: List<App>,
                            application: Application): AndroidViewModel(application) {

    private var job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    var showingSystemApps = MutableLiveData<Boolean>().apply { value = true }

    var appList: List<MutableLiveData<App>>

    init {
        // initialize appList to match installedApps with default state
        // wrap each app in a livedata obj
        appList = installedApps
            .map { app: App -> MutableLiveData<App>().apply { value = app } }

        // subscribe to observe apps when they are retrieved from the db
        db.getAll()
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.io())
            .subscribe(
                { dbApps: List<StoredApp> -> // onSuccess
                    updateAppList(dbApps)
                },
                { _: Throwable? -> // onError

                }
        )
    }

    /**
     * Updates the list of apps to have the same 'muted' values as the DB.
     */
    private fun updateAppList(dbAppList: List<StoredApp>) {
        val mappedDbAppList = dbAppList.map { it.packageName to it }.toMap()

        for (appLD in appList) {
            val app = appLD.value ?: continue
            val dbApp = mappedDbAppList[app.packageName]
            if (dbApp != null) app.isMuted = dbApp.isMuted
        }
    }

    /**
     * Update value of isShowingSystemApps
     */
    fun onCheckedShowingSystemApps(isChecked: Boolean) {
        showingSystemApps.value = isChecked

        val msg = if (showingSystemApps.value ?: false) "Showing system apps..."
            else "Hiding system apps..."
        Log.i("MainActivityViewModel", msg)
    }

    /**
     * Update given app.
     */
    fun onAppUpdate(appLD: MutableLiveData<App>, isChecked: Boolean) {

        val app = appLD.value

        if (app != null) {
            Log.i("MainActivityViewModel", "onAppUpdate ${app.packageName}")

            // 'app's value is changed here too
            appLD.value = app.apply { isMuted = !isChecked }

            uiScope.launch {
                withContext(Dispatchers.IO) {
                    db.insertOrUpdate(app.toDBObj())
                }
            }
        }
    }
}