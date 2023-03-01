package com.example.background.periodic.scheduler

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobWorkItem
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class LogScheduler : JobScheduler() {
    override fun schedule(job: JobInfo): Int {
        println("LogWorker :: schedule :: Running...")
        return RESULT_SUCCESS
    }

    override fun enqueue(job: JobInfo, work: JobWorkItem): Int {
        if (job.isPeriodic) {
            println("LogWorker :: enqueue :: ${job.id} is Running...")
        }
        return job.id
    }

    override fun cancel(jobId: Int) {
        println("LogWorker :: cancel :: $jobId is cancelled...")
    }

    override fun cancelAll() {
        println("LogWorker :: cancelAll :: all jobs are cancelled!")
    }

    override fun getAllPendingJobs(): MutableList<JobInfo> {
        TODO("Not yet implemented")
    }

    override fun getPendingJob(jobId: Int): JobInfo? {
        TODO("Not yet implemented")
    }
}