package com.ajsnarr.choosewhatyoumute

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.ajsnarr.choosewhatyoumute.Data.App

class AppAdapter(private val appList: Array<LiveData<App?>>) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    private val size: Int = appList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_selection, parent, false)

        return AppViewHolder(view)
    }

    override fun getItemCount() = appList.size

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appName = holder.view.findViewById<TextView>(R.id.text_app_name)

        appName.text = appList[position].value?.labelName ?: ""
    }


    class AppViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}