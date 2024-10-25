package com.mobile2app.eventtracker;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class NotificationReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        String title = bundle.getString("title");
        String description = bundle.getString("description");

        NotificationsManager.getInstance().sendEventNotification(context,title,description);
    }
}
