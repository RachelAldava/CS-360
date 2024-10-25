package com.mobile2app.eventtracker;
import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import androidx.core.app.NotificationCompat;


public class NotificationsManager{
    // Common
    private static NotificationsManager instance;

    public PermissionGroup PUSH;
    public PermissionGroup SMS;

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private final String channelID = "EventNotifications";


    //##############################################################################################
    //################################### Shared Methods ###########################################
    //##############################################################################################

    // Constructor
    private NotificationsManager(){}

    // Singleton public constructor
    public static void initialize( Activity activity){
        if (instance == null){
            instance = new NotificationsManager();

            // Request push notification permissions and create handler.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                String[] pushPerms = {
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                        Manifest.permission.SCHEDULE_EXACT_ALARM
                };
                instance.PUSH = new PermissionGroup(activity, pushPerms, "UserPushPerms");
                instance.createPushNotification(activity.getApplicationContext(), "Title", "Body");
            }

            // Request SMS notification permissions and create handler.
            String[] SMSPerms = {
                    Manifest.permission.RECEIVE_SMS};
            instance.SMS = new PermissionGroup(activity, SMSPerms, "UserSMSPerms");
        }
    }

    public static NotificationsManager getInstance(){
        // Todo: implement null error handling
        return instance;
    }

    public static void saveUserPermPUSH(boolean isEnabled){
        instance.PUSH.saveUserPerm(User.getCurrUsername(), isEnabled);
    }

    public static boolean canPUSH(Context context){
        return instance.PUSH.userPermIsGranted(User.getCurrUsername());
    }

    public void createPushNotification(Context context, String title, String body) {
        builder =
                new NotificationCompat.Builder(context, channelID)
                        .setSmallIcon(R.drawable.ic_alarm)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager = getSystemService(context, NotificationManager.class);
        // create an unused notification channel to prompt Android to push permissions.
        new NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_DEFAULT);
    }
    public void sendPush(){
        //Toast.makeText((root.getContext()), "Potato", Toast.LENGTH_LONG).show();
        if (PUSH == null) return;

        NotificationsManager instance = NotificationsManager.getInstance();
        boolean userAllows = instance.PUSH.userPermIsGranted(User.getCurrUsername());

        if (userAllows) {
            NotificationChannel notificationChannel = new NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Notification Description");
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.notify(10, builder.build());
        }
    }

    public void pushEventNotification(Context context, String title, String body) {
        builder =
                new NotificationCompat.Builder(context, channelID)
                        .setSmallIcon(R.drawable.ic_alarm)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager = getSystemService(context, NotificationManager.class);
        // create an unused notification channel to prompt Android to push permissions.
        new NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_DEFAULT);

        sendPush();
    }

    public void sendEventNotification (Context context, String title, String body) {
        if (PUSH != null && PUSH.permIsGranted(context, User.getCurrUsername())) pushEventNotification(context, title, body);
    }

}
