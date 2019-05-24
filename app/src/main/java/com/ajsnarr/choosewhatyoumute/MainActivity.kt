package com.ajsnarr.choosewhatyoumute

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ajsnarr.choosewhatyoumute.AppList.App
import com.ajsnarr.choosewhatyoumute.AppList.AppAdapter
import android.content.pm.PackageInfo
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // add to recycler view
        val recyclerAdapter = AppAdapter(getInstalledApps())
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
    private fun getInstalledApps(): Array<App> {
        val res = ArrayList<App>()
        val packs = packageManager.getInstalledPackages(0)

        for (p in packs) {
            if (false == isSystemPackage(p)) {
                val appName = p.applicationInfo.loadLabel(packageManager).toString()
                val icon = p.applicationInfo.loadIcon(packageManager)
                res.add(App(appName, icon))
            }
        }

        // convert to array and sort in alphabetical order
        return res.toTypedArray().apply {
            sortBy { it.name }
        }
    }

    private fun isSystemPackage(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }
}
