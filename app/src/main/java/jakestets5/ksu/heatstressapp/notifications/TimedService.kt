package jakestets5.ksu.heatstressapp.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import jakestets5.ksu.heatstressapp.R
import jakestets5.ksu.heatstressapp.activities.ForecastActivity
import jakestets5.ksu.heatstressapp.activities.MainActivity
import jakestets5.ksu.heatstressapp.helpers.api.ForecastApiHelper
import jakestets5.ksu.heatstressapp.helpers.database.SavedLocationsDatabaseHelper

/**
 * A service class that manages a foreground service designed to perform background tasks
 * related to fetching and updating weather details periodically.
 */
class TimedService : Service() {

    /**
     * Binds to the service.
     *
     * @param p0 The Intent that was used to bind to this service.
     * @return Always returns null as no binding is allowed.
     */
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    /**
     * Handles the service start commands. The service can be started to perform tasks.
     *
     * @param intent The Intent supplied to `startService`.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return The return value indicates what semantics the system should use for the service's current started state.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Starts the service in the foreground to ensure the system does not kill it,
     * even if the app is in the background.
     */
    @SuppressLint("ForegroundServiceType")
    private fun start(){
        val notification = NotificationCompat.Builder(this, "running_channel")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("getting weather details")
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
    }

    /**
     * Defines the actions that this Service can handle.
     */
    enum class Actions{
        START, STOP
    }
}
