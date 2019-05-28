package com.ajsnarr.choosewhatyoumute.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ajsnarr.choosewhatyoumute.data.App
import android.content.pm.PackageInfo
import android.content.pm.ApplicationInfo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajsnarr.choosewhatyoumute.R
import com.ajsnarr.choosewhatyoumute.db.AppDatabase
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ajsnarr.choosewhatyoumute.service.NotificationListenerService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var recyclerAdapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get db instance
        val db = AppDatabase.getInstance(this.application).appDAO

        // create view model
        val factory =
            MainActivityViewModelFactory(
                db, getInstalledApps(),
                this.application
            )

        viewModel = ViewModelProviders.of(this, factory)
            .get(MainActivityViewModel::class.java)

        // add to recycler view
        recyclerAdapter = AppAdapter(
            viewModel.appList,
            viewModel.showingSystemApps,
            RecyclerItemActionListener(viewModel)
        )
        val recyclerManager = LinearLayoutManager(this)

        findViewById<RecyclerView>(R.id.recycler_main).apply {
            setHasFixedSize(true) // for performance

            layoutManager = recyclerManager
            adapter = recyclerAdapter
        }

        // set up options at top of screen
        setupOptions()

        // start notification listener service
        startNotificationListenerService()
    }

    fun setupOptions() {
        switch_main_systemapps.isChecked = viewModel.showingSystemApps.value!!
        switch_main_systemapps.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onCheckedShowingSystemApps(isChecked)
            updateRecyclerView()
        }
    }

    fun updateRecyclerView() {
        Log.i("MainActivity" ,"updating recycler view...")
        recyclerAdapter.notifyDataSetChanged()
    }

    fun startNotificationListenerService() {
        Log.i("MainActivity", "starting service...")
        if (!isServiceRunning(NotificationListenerService::class.java)) {
            startService(Intent(this.applicationContext, NotificationListenerService::class.java))
        } else {
            Log.i("MainActivity","service already running")
        }
    }


    /**
     * Get installed apps, sorted in alphabetical order.
     */
    private fun getInstalledApps(): List<App> {
        val res = ArrayList<App>()
        val packs = packageManager.getInstalledPackages(0)

        for (p in packs) {
            val pkgName = p.applicationInfo.packageName
            val appName = p.applicationInfo.loadLabel(packageManager).toString()
            val icon = p.applicationInfo.loadIcon(packageManager)

            res.add(App(packageName=pkgName, isSystemApp=isSystemPackage(p),
                labelName=appName, icon=icon))
        }

        // convert to array and sort in alphabetical order
        return res.apply { sortBy { it.labelName } }
    }

    private fun isSystemPackage(pkgInfo: PackageInfo): Boolean {
        return (pkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }

    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        @Suppress("deprecation")
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    class RecyclerItemActionListener(val viewModel: MainActivityViewModel)
        : AppAdapter.ActionListener {

        override fun onSwitchChecked(
            app: MutableLiveData<App>,
            isChecked: Boolean
        ) {
            viewModel.onAppUpdate(app, isChecked)
        }

    }
}
