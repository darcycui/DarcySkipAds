package com.darcy.skipads.app

import android.app.Application
import android.content.Context

class DarcySkipApp : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

}