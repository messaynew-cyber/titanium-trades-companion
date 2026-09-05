package com.titanium.trades.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.titanium.trades.data.AlertEngine
import com.titanium.trades.data.PriceApi
import com.titanium.trades.data.TradeRepository
import com.titanium.trades.notif.NotificationHelper

/**
 * Background worker (WorkManager) that fetches the latest price and, if a
 * stop-loss or take-profit is configured and crossed, posts an alert
 * notification. Survives app kill and runs on a quiet schedule by Android.
 */
class PriceAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val appContext = applicationContext
        val repo = TradeRepository(appContext)
        val cfg = repo.tradeConfig.value
        return try {
            val price = PriceApi().fetchSolPrice()
            if (price == null) {
                // Transient network issue; retrigger isn't urgent.
                Result.retry()
            } else {
                repo.cachePrice(price.usd)
                val alert = AlertEngine.evaluate(price, cfg)
                if (alert != null) {
                    NotificationHelper.post(
                        appContext,
                        alert.title,
                        alert.message,
                        isCritical = alert.severity.name == "CRITICAL"
                    )
                }
                Result.success()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

/** Schedules a periodic background price-check every 15 minutes. */
object PriceAlertScheduler {
    fun schedulePeriodic(context: Context) {
        val request = androidx.work.PeriodicWorkRequestBuilder<PriceAlertWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
            .setConstraints(androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build())
            .build()
        androidx.work.WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "price_alert_worker",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
}
