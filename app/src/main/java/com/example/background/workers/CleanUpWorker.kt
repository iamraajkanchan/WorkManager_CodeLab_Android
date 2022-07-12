package com.example.background.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.TAG

class CleanUpWorker(context: Context, param: WorkerParameters) : Worker(context, param) {
    override fun doWork(): Result {
        makeStatusNotification("Cleaning Up Old Temporary Files", applicationContext)
        sleep()
        return try {
            Result.success()
        } catch (throwable: Throwable) {
            Log.e(TAG, "Clean Up Operation Failed!!!")
            throwable.printStackTrace()
            Result.failure()
        }
    }
}