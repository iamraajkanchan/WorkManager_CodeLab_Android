package com.example.background.periodic.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class LogWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        println("LogWorker :: doWork :: Running...")
        return Result.success()
    }
}