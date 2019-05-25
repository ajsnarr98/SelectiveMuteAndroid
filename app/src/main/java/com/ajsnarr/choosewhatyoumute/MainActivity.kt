package com.ajsnarr.choosewhatyoumute

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ajsnarr.choosewhatyoumute.Data.App
import android.content.pm.PackageInfo
import android.content.pm.ApplicationInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajsnarr.choosewhatyoumute.Data.AppDatabase
import com.ajsnarr.choosewhatyoumute.Data.AsyncAppDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private var job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get db instance
        val db = AsyncAppDAO(AppDatabase.getInstance(this.application).appDAO)

        // add to recycler view
        val recyclerAdapter =
            AppAdapter(getInstalledApps(db))
        val recyclerManager = LinearLayoutManager(this)

        findViewById<RecyclerView>(R.id.recycler_main).apply {
            setHasFixedSize(true) // for performance

            layoutManager = recyclerManager
            adapter = recyclerAdapter
        }
    }

    /**
     * Get installed apps, sorted in alphabetical order.
     */
    private fun getInstalledApps(db: AsyncAppDAO): Array<LiveData<App?>> {
        val res = ArrayList<LiveData<App?>>()
        val packs = packageManager.getInstalledPackages(0)

        for (p in packs) {
            if (false == isSystemPackage(p)) {
                val appName = p.applicationInfo.loadLabel(packageManager).toString()
                val icon = p.applicationInfo.loadIcon(packageManager)
                val pkgName = p.applicationInfo.packageName

                // Here the database is checked for an old setting for that app
                val data = MutableLiveData<App?>()
                uiScope.launch {
                    data.value = App.getApp(
                        db = db,
                        packageName = pkgName,
                        labelName = appName,
                        icon = icon
                    )
                }
                res.add(data)
            }
        }

        // convert to array and sort in alphabetical order
        return res.toTypedArray().apply {
            sortBy { it.value?.labelName ?: "" }
        }
    }

    private fun isSystemPackage(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }
}
