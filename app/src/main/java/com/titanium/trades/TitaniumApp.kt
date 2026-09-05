package com.titanium.trades

import android.app.Application
import com.titanium.trades.notif.NotificationHelper
import com.titanium.trades.worker.PriceAlertScheduler

class TitaniumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        PriceAlertScheduler.schedulePeriodic(this)
    }
}
