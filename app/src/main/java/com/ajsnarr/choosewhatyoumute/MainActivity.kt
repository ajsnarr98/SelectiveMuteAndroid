package com.ajsnarr.choosewhatyoumute

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ajsnarr.choosewhatyoumute.data.App
import android.content.pm.PackageInfo
import android.content.pm.ApplicationInfo
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajsnarr.choosewhatyoumute.db.AppDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get db instance
        val db = AppDatabase.getInstance(this.application).appDAO

        // create view model
        val factory = MainActivityViewModelFactory(db, getInstalledApps(),
            this.application)

        viewModel = ViewModelProviders.of(this, factory)
            .get(MainActivityViewModel::class.java)

        // add to recycler view
        val recyclerAdapter = AppAdapter(viewModel.appList,
            RecyclerItemActionListener(viewModel))
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
    private fun getInstalledApps(): List<App> {
        val res = ArrayList<App>()
        val packs = packageManager.getInstalledPackages(0)

        for (p in packs) {
            if (false == isSystemPackage(p)) {
                val pkgName = p.applicationInfo.packageName
                val appName = p.applicationInfo.loadLabel(packageManager).toString()
                val icon = p.applicationInfo.loadIcon(packageManager)

                res.add(App(packageName=pkgName, labelName=appName, icon=icon))
            }
        }

        // convert to array and sort in alphabetical order
        return res
    }

    private fun isSystemPackage(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
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
