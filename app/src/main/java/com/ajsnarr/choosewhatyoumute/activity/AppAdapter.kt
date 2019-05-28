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
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.ajsnarr.choosewhatyoumute.R


class AppAdapter(private val fullAppList: List<MutableLiveData<App>>,
                 private val isShowingSystemApps: MutableLiveData<Boolean>,
                 private val actionListener: ActionListener
)
    : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    // filter by  '!= true' so null value is included
    val nonSystemAppList = fullAppList.filter { it.value?.isSystemApp != true }

    // currently used list in recycler view
    var listInUse: List<MutableLiveData<App>> =
        if (isShowingSystemApps.value ?: true)
            fullAppList
        else
            nonSystemAppList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_selection, parent, false)

        isShowingSystemApps.observe(getLifecycleOwner(parent), Observer { showingSysApps ->
            listInUse = if (showingSysApps) fullAppList else nonSystemAppList
        })

        return AppViewHolder(view)
    }

    override fun getItemCount() = listInUse.size

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appName = holder.view.findViewById<TextView>(R.id.text_app_name)
        val switch = holder.view.findViewById<Switch>(R.id.switch_app_mute)
        val image = holder.view.findViewById<ImageView>(R.id.image_app_icon)

        // store appLD as separate variable so the app referred to at this
        // position is independent to changes to 'listInUse'
        val appLD = listInUse[position]
        val app = appLD.value

        if (app != null) {
            appName.text = app.labelName
            switch.isChecked = !app.isMuted
            if (app.icon != null) image.setImageDrawable(app.icon)
        }

        // update switch based on db value
        listInUse[position].observe(getLifecycleOwner(holder.view), Observer {
            switch.isChecked = !it.isMuted
        })

        // set change listener for switch
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (app != null) {
                actionListener.onSwitchChecked(appLD, isChecked)
            }
        }
    }

    private fun getLifecycleOwner(view: View): LifecycleOwner {
        var context = view.getContext()
        while (context !is LifecycleOwner) {
            context = (context as ContextWrapper).baseContext
        }
        return context
    }

    class AppViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    interface ActionListener {
        fun onSwitchChecked(app: MutableLiveData<App>, isChecked: Boolean)
    }
}