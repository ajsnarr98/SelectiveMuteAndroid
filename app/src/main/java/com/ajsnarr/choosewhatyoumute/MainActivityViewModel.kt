package com.ajsnarr.choosewhatyoumute

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ajsnarr.choosewhatyoumute.data.App
import com.ajsnarr.choosewhatyoumute.db.AppDAO
import com.ajsnarr.choosewhatyoumute.db.StoredApp
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlin.math.log

class MainActivityViewModel(val db: AppDAO, installedApps: List<App>,
                            application: Application): AndroidViewModel(application) {

    private var job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private var isReadingFromDB = true
    var appList: List<MutableLiveData<App>>

    init {
        // initialize appList to match installedApps with default state
        // wrap each app in a livedata obj
        appList = installedApps
            .map({ app: App -> MutableLiveData<App>().apply { value = app } })

        // subscribe to observe apps when they are retrieved from the db
        db.getAll()
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.io())
            .subscribe(
                { dbApps: List<StoredApp> -> // onSuccess
                    updateAppList(dbApps)
                    isReadingFromDB = false
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