package jakestets5.ksu.heatstressapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.PeriodicWorkRequestBuilder
import jakestets5.ksu.heatstressapp.R
import jakestets5.ksu.heatstressapp.activities.MainActivity

/**
 * A helper class to manage notification operations for the application. It facilitates the creation
 * of a notification channel and the dispatch of notifications.
 *
 * @param context The context in which this helper operates, used to access system services and application-level operations.
 */
class NotificationHelper(private val context: Context) {

    // Identifier for the notification channel
    private val channelId = "weather_alerts_channel"

    // Unique identifier for the notifications themselves
    private val notificationId = 1

    /**
     * Initializes the notification channel immediately upon the creation of an instance of this class,
     * ensuring that the channel is available for any notifications that need to be sent.
     */
    init {
        createNotificationChannel()
    }

    /**
     * Creates a notification channel if it does not already exist. Notification channels
     * are required for delivering notifications on Android Oreo and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Weather Alerts"
            val descriptionText = "Notifications for weather alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Constructs and dispatches a styled notification using the parameters provided.
     *
     * @param title The title text for the notification.
     * @param message The message text for the notification. This will be displayed in an expanded format.
     */
    fun sendNotification(title: String, message: String) {
        // Intent to launch the main activity when the notification is tapped.
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Pending intent that wraps the intent, which will execute the intent as if it was performed by another app.
        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or Intent.FILL_IN_ACTION)
        } else {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        // Building the notification with all parameters set.
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
