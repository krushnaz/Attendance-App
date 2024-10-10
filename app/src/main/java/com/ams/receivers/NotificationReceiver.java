package com.ams.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.ams.R;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Create NotificationManager to handle the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification channel ID and name
        String channelId = "attendance_channel";
        String channelName = "Attendance Notifications";

        // For Android 8.0 (API 26) and above, create the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notifications)  // Set the notification icon
                .setContentTitle("Mark Absentees")          // Set the notification title
                .setContentText("Please mark absentees for the students.")  // Set the notification message
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // Set the priority
                .setAutoCancel(true);  // Dismiss the notification when tapped

        // Show the notification
        notificationManager.notify(1, builder.build());
    }
}
