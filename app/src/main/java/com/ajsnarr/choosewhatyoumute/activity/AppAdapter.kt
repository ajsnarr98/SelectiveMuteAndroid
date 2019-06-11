package com.ajsnarr.choosewhatyoumute.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.ajsnarr.choosewhatyoumute.data.App
import androidx.lifecycle.LifecycleOwner
import android.content.ContextWrapper
import android.text.BoringLayout
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.ajsnarr.choosewhatyoumute.R

private const val TAG = "AppAdapater"

class AppAdapter(private val fullAppList: List<App>,
                 private var isShowingSystemApps: Boolean,
                 private val actionListener: ActionListener
)
    : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    // used to update views
    private val appUpdateObservers = AppUpdateObservers(fullAppList)

    // filter by  '!= true' so null value is included
    private val nonSystemAppList = fullAppList.filter { !it.isSystemApp }

    // currently used list in recycler view
    var listInUse: List<App> =
        if (isShowingSystemApps)
            fullAppList
        else
            nonSystemAppList

    // map of package names to respective apps
    val appMap: Map<String, App> = fullAppList.map { it.packageName to it }.toMap()

    fun onAppUpdate(packageName: String, isMuted: Boolean) {

        if (!appMap.containsKey(packageName)) throw IllegalArgumentException("Unknown package name, $packageName")

        // only update apps that have changed
        val hasValueChanged = appMap[packageName]?.isMuted != isMuted
        Log.d(TAG, "onAppUpdate package $packageName has changed: $hasValueChanged")
        if (!hasValueChanged) return

        appUpdateObservers.onAppUpdate(packageName, isMuted)
        super.notifyItemChanged(listInUse.indexOfFirst { it.packageName == packageName })
    }

    fun onShowingSystemApps(isShowingSystemApps: Boolean) {
        this.isShowingSystemApps = isShowingSystemApps
        this.listInUse = if (isShowingSystemApps) fullAppList else nonSystemAppList
        this.appUpdateObservers.resetObservers()
        super.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_selection, parent, false)

        return AppViewHolder(view)
    }

    override fun getItemCount() = listInUse.size

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appName = holder.view.findViewById<TextView>(R.id.text_app_name)
        val switch = holder.view.findViewById<Switch>(R.id.switch_app_mute)
        val image = holder.view.findViewById<ImageView>(R.id.image_app_icon)

        // remove old observer on switch
        switch.setOnCheckedChangeListener { _, _ -> }

        val app = listInUse[position].copy()

        // set properties of text field
        appName.text = app.labelName
        switch.isChecked = !app.isMuted
        if (app.icon != null) image.setImageDrawable(app.icon)

        // update switch based on db value
        appUpdateObservers.setObserver(app.packageName, Observer { app ->
            switch.isChecked = !app.isMuted
        })

        // set change listener for switch
        switch.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "onCheckedChanedListener isChecked $isChecked for app ${app.packageName}")
            actionListener.onSwitchChecked(app.packageName, isChecked)
            onAppUpdate(app.packageName, !isChecked)
        }
    }

    class AppViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    interface ActionListener {
        fun onSwitchChecked(appPackageName: String, isChecked: Boolean)
    }

    private class AppUpdateObservers(val appList: List<App>) {

        // map of package names to respective apps
        val appMap: Map<String, App> = appList.map { it.packageName to it }.toMap()

        // map of package names to observers
        val obsMap: MutableMap<String, Observer<App>> = HashMap()

        init {
            // initialize obsMap with empty observers
            resetObservers()
        }

        fun resetObservers() {
            for (app in appList) {
                obsMap[app.packageName] = Observer { appMap[app.packageName]?.isMuted = it.isMuted }
            }
        }

        fun setObserver(appPackageName: String?, observer: Observer<App>) {
            appPackageName ?: return

            if (obsMap.containsKey(appPackageName))
                obsMap[appPackageName] = observer
            else
                throw IllegalArgumentException("Unknown package name, $appPackageName")
        }

        fun onAppUpdate(packageName: String, isMuted: Boolean) {
            if (obsMap.containsKey(packageName)) {
                val app: App = appMap[packageName]!!
                app.apply { this.isMuted = isMuted } // should change all pointer references

                obsMap[packageName]!!.onChanged(app)

            } else {
                throw IllegalArgumentException("Unknown package name, $packageName")
            }
        }
    }
}