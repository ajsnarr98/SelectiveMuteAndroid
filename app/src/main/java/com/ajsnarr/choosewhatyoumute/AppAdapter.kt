package com.ajsnarr.choosewhatyoumute

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
import androidx.lifecycle.MutableLiveData


class AppAdapter(private val appList: List<MutableLiveData<App>>,
                 private val actionListener: ActionListener)
    : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    private val size: Int = appList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_selection, parent, false)

        return AppViewHolder(view)
    }

    override fun getItemCount() = appList.size

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appName = holder.view.findViewById<TextView>(R.id.text_app_name)
        val switch = holder.view.findViewById<Switch>(R.id.switch_app_mute)

        val app = appList[position].value
        if (app != null) {
            appName.text = app.labelName
            switch.isChecked = !app.isMuted
        }

        appList[position].observe(getLifecycleOwner(holder.view), Observer {
            switch.isChecked = !it.isMuted
        })

        switch.setOnCheckedChangeListener { _, isChecked ->
            if (app != null) {
                actionListener.onSwitchChecked(appList[position], isChecked)
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