package com.example.background.periodic

import android.app.job.JobInfo
import android.content.ComponentName
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.background.databinding.ActivityMainPeriodicBinding
import com.example.background.periodic.scheduler.AppLog
import com.example.background.periodic.scheduler.LogScheduler
import com.example.background.periodic.workers.LogWorker
import java.util.concurrent.TimeUnit

class MainPeriodicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainPeriodicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPeriodicBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        binding.btnPeriodicClick.setOnClickListener { executePeriodicWork() }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            binding.btnSchedulerClick.setOnClickListener { executeJobScheduler() }
        }
    }

    private fun executePeriodicWork() {
        val periodicLogRequest = PeriodicWorkRequestBuilder<LogWorker>(16, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "MainLogWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicLogRequest
            )
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun executeJobScheduler() {
        val myScheduledJob = LogScheduler()
        val myJobInfo = JobInfo.Builder(
            123,
            ComponentName(this@MainPeriodicActivity, AppLog::class.java)
        ).setPeriodic(2L).build()
        myScheduledJob.schedule(myJobInfo)
    }
}