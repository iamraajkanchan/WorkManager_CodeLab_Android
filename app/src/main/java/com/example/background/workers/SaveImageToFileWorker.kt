package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import com.example.background.TAG
import java.text.SimpleDateFormat
import java.util.*

class SaveImageToFileWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val title = "Blurred_Image"
    private val dateFormatter = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())
    override fun doWork(): Result {
        makeStatusNotification("Saving Image", applicationContext)
        sleep()
        val resolver = applicationContext.contentResolver
        try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            val bitmap =
                BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))
            val imageUrl = MediaStore.Images.Media.insertImage(
                resolver,
                bitmap,
                title,
                dateFormatter.format(Date())
            )
            return if (imageUrl != null) {
                val output = workDataOf(KEY_IMAGE_URI to imageUrl)
                Result.success(output)
            } else {
                Log.e(TAG, "Writing to Media Store Failed")
                Result.failure()
            }
        } catch (throwable: Throwable) {
            Log.i(TAG, "Save Image to File Failed!!!")
            throwable.printStackTrace()
            return Result.failure()
        }
    }
}