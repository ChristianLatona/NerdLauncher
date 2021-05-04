package com.christianlatona.android.nerdlauncher

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ScaleDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


private const val TAG = "NerdLauncherActivity"
private lateinit var recyclerView: RecyclerView
class NerdLauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nerd_launcher)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupAdapter()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun setupAdapter(){
        val startupIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        } // the URI was the specific path of the application
        // that we wanted to opened
        val activities = packageManager.queryIntentActivities(startupIntent, 0)
        activities.sortWith(Comparator { a, b ->
            String.CASE_INSENSITIVE_ORDER.compare(
                    a.loadLabel(packageManager).toString(),
                    b.loadLabel(packageManager).toString()
            )
        })
        recyclerView.adapter = ActivityAdapter(activities)
        // Log.i(TAG, "Found ${activities.size} activities")
    }

    private class ActivityHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{
        // we re not doing this inner
        private val nameTextView = itemView as TextView // the view here is only a label
        private lateinit var resolveInfo: ResolveInfo

        init {
            nameTextView.setOnClickListener(this)
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bindActivity(resolveInfo: ResolveInfo){
            this.resolveInfo = resolveInfo
            val packageManager = itemView.context.packageManager // it should be the activity context
            val appName = resolveInfo.loadLabel(packageManager).toString()
            // icons have different sizes:
            // solution 1: scaling the bitmap. My solutions didn't resized the Bitmap
            // solution 2: setting a custom drawable with android:width and height, not able to retrieve attributes
            // programmatically, i still didn't understood what ScaleDrawable does (was like useless)
            // i just had to set bounds, calling the method without IntrinsicBounds

            val appIcon = resolveInfo.loadIcon(packageManager)
            appIcon.setBounds(0,0,120,120)

            nameTextView.setCompoundDrawablesRelative(appIcon, null, null, null)
            nameTextView.text = appName
        }


        override fun onClick(v: View?) {
            val activityInfo = resolveInfo.activityInfo
            // start activity with the explicit intent

            val intent = Intent(Intent.ACTION_MAIN).apply { // to tell which activity to start (explicit intent)
                // we need package name and class name
                setClassName(activityInfo.packageName, activityInfo.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            v?.context?.startActivity(intent) // since we re not in a context subclass, we need to retrieve it from view
        }
    }

    private class ActivityAdapter(val activities: List<ResolveInfo>): RecyclerView.Adapter<ActivityHolder>(){
        // we re not using inner
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityHolder {
            // val layoutInflater = LayoutInflater.from(parent.context) // ????? seems not necessary, i'll skip this
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            // we re using Android's default list item, seems like he bugs on inner classes, also i dislike this
            // impossibility of editing
            return ActivityHolder(view)
        }

        override fun onBindViewHolder(holder: ActivityHolder, position: Int) {
            val activity = activities[position]
            holder.bindActivity(activity)
        }

        override fun getItemCount(): Int = activities.size

    }
}