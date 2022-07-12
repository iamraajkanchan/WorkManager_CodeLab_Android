package com.example.background.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.OUTPUT_PATH
import com.example.background.TAG
import java.io.File

class CleanUpWorker(context: Context, param: WorkerParameters) : Worker(context, param) {
    override fun doWork(): Result {
        makeStatusNotification("Cleaning Up Old Temporary Files", applicationContext)
        sleep()
        return try {
            val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)
            if (outputDirectory.exists()) {
                val files = outputDirectory.listFiles()
                if (files != null) {
                    for (file in files) {
                        val fileName = file.name
                        if (fileName.isNotEmpty() && fileName.endsWith(".png")) {
                            val deletedFile = file.delete()
                            Log.i(TAG, "Deleted $fileName - $deletedFile")
                        }
                    }
                }
            }
            Result.success()
        } catch (throwable: Throwable) {
            Log.e(TAG, "Clean Up Operation Failed!!!")
            throwable.printStackTrace()
            Result.failure()
        }
    }
}