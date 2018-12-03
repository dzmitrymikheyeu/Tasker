package com.dev.tasker.list

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dev.tasker.R
import com.dev.tasker.create.CreateTaskActivity
import com.dev.tasker.service.TaskerService
import com.dev.tasker.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, TaskerService::class.java))
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.app_name)

        tabLayout.setupWithViewPager(pager)
        val adapter = TabPagerAdapter(this, supportFragmentManager)
        pager.adapter = adapter

        fab.setOnClickListener {
            startActivity(Intent(MainActivity@this, CreateTaskActivity::class.java))
        }

        img_settings.setOnClickListener {
            startActivity(Intent(MainActivity@this, SettingsActivity::class.java))
        }
    }
}
