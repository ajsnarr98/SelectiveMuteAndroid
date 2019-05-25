package com.ajsnarr.choosewhatyoumute.Data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AsyncAppDAO(val dao: AppDAO) {

    suspend fun insertApp(app: App) {
        return withContext(Dispatchers.IO) {
            dao.insert(app)
        }
    }

    suspend fun get(packageName: String): App? {
        return withContext(Dispatchers.IO) {
            dao.get(packageName)
        }
    }

    suspend fun getAllUnmuted(): LiveData<List<App>> {
        return withContext(Dispatchers.IO) {
            dao.getAllUnmuted()
        }
    }

    suspend fun clearAll() {
        return withContext(Dispatchers.IO) {
            dao.clearAll()
        }
    }
}