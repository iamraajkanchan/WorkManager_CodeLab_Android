package com.example.background.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.TAG

class BlurWorker(context: Context, param: WorkerParameters) : Worker(context, param) {
    override fun doWork(): Result {
        val appContext = applicationContext
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        makeStatusNotification("Blurring image", appContext)
        return try {

            /*
            val picture =
                BitmapFactory.decodeResource(appContext.resources, R.drawable.android_cupcake)
            */
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid Input Uri")
                throw IllegalArgumentException("Invalid Input Uri")
            }
            val resolver = appContext.contentResolver
            val picture =
                BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))
            val output: Bitmap = blurBitmap(appContext, picture)
            val outputUri: Uri = writeBitmapToFile(appContext, picture)
            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
            Result.success(outputData)
        } catch (throwable: Throwable) {
            Log.e(TAG, "Error Applying Blur")
            throwable.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        const val KEY_BLUR_VALUE: String = "blur value"
        const val KEY_IMAGE_URI: String = "image uri"
    }
}