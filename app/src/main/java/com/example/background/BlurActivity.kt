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

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.example.background.databinding.ActivityBlurBinding
import com.example.background.periodic.MainPeriodicActivity

/**
 * CodeLab Source : https://codelabs.developers.google.com/codelabs/android-workmanager
 * Advanced CodeLab Source : https://developer.android.com/codelabs/android-adv-workmanager?index=..%2F..index#0
 * */

class BlurActivity : AppCompatActivity() {

    private val viewModel: BlurViewModel by viewModels {
        BlurViewModel.BlurViewModelFactory(application)
    }

    private lateinit var binding: ActivityBlurBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlurBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.goButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.applyBlur(blurLevel)
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    Toast.makeText(
                        this,
                        "Need to access external storage to store the blurred image.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_CODE_WRITE_EXTERNAL_STORAGE
                    )
                }
            }
        }
        /* Observer work status */
        viewModel.outputWorkInformation.observe(this, workInformationObserver())
        binding.seeFileButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.outputUri?.let { currentUri ->
                    val actionView = Intent(Intent.ACTION_VIEW, currentUri)
                    actionView.resolveActivity(packageManager)?.run {
                        startActivity(actionView)
                    }
                }
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    Toast.makeText(
                        this,
                        "Need to access external storage to read the blurred image.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_CODE_READ_EXTERNAL_STORAGE
                    )
                }
            }

        }
        binding.cancelButton.setOnClickListener {
            viewModel.cancelWork()
        }
        binding.btnPeriodicRequest.setOnClickListener {
            Intent(this@BlurActivity, MainPeriodicActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    /**
     * Define the observer function.
     * @return Observer<List<WorkInfo>>
     * */
    private fun workInformationObserver(): Observer<List<WorkInfo>> {
        return Observer {
            if (it.isNullOrEmpty()) {
                return@Observer
            }
            val workInfo = it[0]
            if (workInfo.state.isFinished) {
                showWorkFinished()
                val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)
                /* If there is an output file show "See File" Button */
                if (!outputImageUri.isNullOrEmpty()) {
                    viewModel.setOutputUri(outputImageUri)
                    binding.seeFileButton.visibility = View.VISIBLE
                }
            } else {
                showWorkInProgress()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_WRITE_EXTERNAL_STORAGE -> viewModel.applyBlur(blurLevel)
            REQUEST_CODE_READ_EXTERNAL_STORAGE -> {
                viewModel.outputUri?.let { currentUri ->
                    val actionView = Intent(Intent.ACTION_VIEW, currentUri)
                    actionView.resolveActivity(packageManager)?.run {
                        startActivity(actionView)
                    }
                }
            }
        }
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        with(binding) {
            progressBar.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            goButton.visibility = View.GONE
            seeFileButton.visibility = View.GONE
        }
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        with(binding) {
            progressBar.visibility = View.GONE
            cancelButton.visibility = View.GONE
            goButton.visibility = View.VISIBLE
        }
    }

    private val blurLevel: Int
        get() =
            when (binding.radioBlurGroup.checkedRadioButtonId) {
                R.id.radio_blur_lv_1 -> 1
                R.id.radio_blur_lv_2 -> 2
                R.id.radio_blur_lv_3 -> 3
                else -> 1
            }
}
