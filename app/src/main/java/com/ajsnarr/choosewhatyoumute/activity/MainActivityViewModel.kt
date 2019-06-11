package com.ajsnarr.choosewhatyoumute.activity

import android.app.Application
import android.text.BoringLayout
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ajsnarr.choosewhatyoumute.data.App
import com.ajsnarr.choosewhatyoumute.db.AppDAO
import com.ajsnarr.choosewhatyoumute.db.StoredApp
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*

private const val TAG = "MainActivityViewModel"

class MainActivityViewModel(val db: AppDAO, installedApps: List<App>,
                            application: Application): AndroidViewModel(application) {

    private var job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    var showingSystemApps = MutableLiveData<Boolean>().apply { value = true }

    var appList: List<App> = installedApps

    /**
     * Subscribe to observe apps when they are retrieved from the DB. Calls consumers on success and on error
     */
    fun getAllFromDB(onSuccess: (List<StoredApp>) -> Unit, onError: (Throwable?) -> Unit) {
        db.getAll()
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.io())
            .subscribe(onSuccess, onError)
    }

    /**
     * Updates the list of apps to have the same 'muted' values as the DB.
     */
    fun updateAppList(dbAppList: List<StoredApp>) {
        val mappedDbAppList = dbAppList.map { it.packageName to it }.toMap()

        for (app in appList) {
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
    fun onAppUpdate(appPackageName: String, isChecked: Boolean) {

        // get app with matching package name
        val app = appList.last { it.packageName == appPackageName }

        Log.i(TAG, "onAppUpdate ${app.packageName} $isChecked")

        // 'app's value is changed here too
        app.apply { isMuted = !isChecked }

        uiScope.launch {
            withContext(Dispatchers.IO) {
                val res = db.insertOrUpdate(app.toDBObj())
                Log.i(TAG, "finished inserting app with result $res")
            }
        }
    }
}