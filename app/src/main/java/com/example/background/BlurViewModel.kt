/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.BlurMaskFilter
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.example.background.workers.BlurWorker
import com.example.background.workers.CleanUpWorker
import com.example.background.workers.SaveImageToFileWorker


class BlurViewModel(application: Application) : ViewModel() {

    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null
    internal val outputWorkInformation: LiveData<List<WorkInfo>>
    private val workManager = WorkManager.getInstance(application)

    init {
        imageUri = getImageUri(application.applicationContext)
        outputWorkInformation = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
    }

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    internal fun applyBlur(blurLevel: Int) {
        /* Add WorkRequest to CleanUp temporary files from the file system. */
        var continuation = workManager.beginUniqueWork(
            IMAGE_MANIPULATION_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.from(CleanUpWorker::class.java)
        )
        for (i in 0 until blurLevel) {
            /* Add WorkRequest to blur an image. */
            val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()
            if (i == 0) {
                blurBuilder.setInputData(createInputDataURI())
            }
            continuation = continuation.then(blurBuilder.build())
        }
        /* Add WorkRequest to save the image to the file system. */
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>().addTag(TAG_OUTPUT).build()
        continuation = continuation.then(save)
        /* Actually start the work. */
        continuation.enqueue()
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    /**
     * getImageUri method returns the Uri of the image
     * @param context
     * */
    private fun getImageUri(context: Context): Uri {
        val resources = context.resources
        val imageUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.android_cupcake))
            .appendPath(resources.getResourceTypeName(R.drawable.android_cupcake))
            .appendPath(resources.getResourceEntryName(R.drawable.android_cupcake))
            .build()
        return imageUri
    }

    private fun createInputDataURI(): Data {
        val builder = Data.Builder()
        imageUri?.let {
            builder.putString(KEY_IMAGE_URI, it.toString())
        }
        return builder.build()
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }

    internal fun cancelWork() {
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }

    class BlurViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(BlurViewModel::class.java)) {
                BlurViewModel(application) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
