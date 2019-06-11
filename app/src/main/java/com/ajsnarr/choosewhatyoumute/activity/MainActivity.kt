package com.ajsnarr.choosewhatyoumute.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ajsnarr.choosewhatyoumute.data.App
import android.content.pm.PackageInfo
import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajsnarr.choosewhatyoumute.R
import com.ajsnarr.choosewhatyoumute.db.AppDatabase
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.ajsnarr.choosewhatyoumute.db.StoredApp
import com.ajsnarr.choosewhatyoumute.service.NotificationListenerService
import kotlinx.android.synthetic.main.activity_main.*

const val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
const val ACTION_NOTIFICATION_LISTENER_SETTINGS =
    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var recyclerAdapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // If the user did not turn the notification listener service on we prompt him to do so
        if (!isNotificationServiceEnabled()) {
            buildNotificationServiceAlertDialog().show()
        }

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
            viewModel.showingSystemApps.value ?: true,
            RecyclerItemActionListener(viewModel)
        )
        getDataFromDB()

        val recyclerManager = LinearLayoutManager(this)

        findViewById<RecyclerView>(R.id.recycler_main).apply {
            layoutManager = recyclerManager
            adapter = recyclerAdapter
        }


        // set up options at top of screen
        setupOptions()

        // start notification listener service
        startNotificationListenerService()
    }

    fun getDataFromDB() {
        // setup listener for updating data from DB
        viewModel.getAllFromDB(
            { dbApps: List<StoredApp> -> // onSuccess

                // update viewModel
                viewModel.updateAppList(dbApps)

                // update recycler
                for (dbApp in dbApps) {
                    recyclerAdapter.onAppUpdate(dbApp.packageName, dbApp.isMuted)
                }
            },
            { _ : Throwable? -> // onError

            }
        )
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
        recyclerAdapter.onShowingSystemApps(viewModel.showingSystemApps.value ?: true)
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

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
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

    /**
     * Verifies if the do not disturb permission is enabled.
     */
    private fun isDoNotDisturbPermissionEnabled(): Boolean {
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && !notificationManager.isNotificationPolicyAccessGranted()
    }

    /**
     * Builds the alert dialog that prompts the user for do not disturb
     * permission.
     */
    private fun buildDoNotDisturbPermissionAlertDialogue(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.do_not_disturb_permission)
        alertDialogBuilder.setMessage(R.string.do_not_disturb_permissoin_explanation)
        alertDialogBuilder.setPositiveButton(R.string.yes,
            DialogInterface.OnClickListener { dialog, id ->
                startActivity(
                    Intent(
                        Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                    )
                )
            })
        alertDialogBuilder.setNegativeButton(R.string.no,
            DialogInterface.OnClickListener { dialog, id ->
                // If you choose to not enable the notification listener
                // the app. will not work as expected
            })
        return alertDialogBuilder.create()
    }

    /**
     * Verifies if the notification listener service is enabled.
     *
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if enabled, false otherwise.
     */
    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(
            contentResolver,
            ENABLED_NOTIFICATION_LISTENERS
        )
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     *
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     *
     * @return An alert dialog which leads to the notification enabling screen
     */
    private fun buildNotificationServiceAlertDialog(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.notification_listener_service)
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation)
        alertDialogBuilder.setPositiveButton(R.string.yes,
            DialogInterface.OnClickListener { dialog, id ->
                startActivity(
                    Intent(
                        Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                    )
                )
            })
        alertDialogBuilder.setNegativeButton(R.string.no,
            DialogInterface.OnClickListener { dialog, id ->
                // If you choose to not enable the notification listener
                // the app. will not work as expected
            })
        return alertDialogBuilder.create()
    }

    class RecyclerItemActionListener(val viewModel: MainActivityViewModel)
        : AppAdapter.ActionListener {

        override fun onSwitchChecked(
            appPackageName: String,
            isChecked: Boolean
        ) {
            Log.d(TAG, "onSwitchCheched $appPackageName $isChecked")
            viewModel.onAppUpdate(appPackageName, isChecked)
        }

    }
}
