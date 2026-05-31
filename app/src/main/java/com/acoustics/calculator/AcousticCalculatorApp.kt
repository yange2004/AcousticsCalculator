package com.acoustics.calculator

import android.app.Application
import com.acoustics.calculator.data.local.preload.MaterialPreloader
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AcousticCalculatorApp : Application() {

    @Inject lateinit var materialPreloader: MaterialPreloader

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            materialPreloader.preloadIfNeeded()
        }
    }
}
