package com.mobile2app.eventtracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class appEvent {
    private final Integer id;
    private final String title;
    private final String description;

    private final List<String> owners;
    private final List<String> participants;

    public final Long startTime;
    public final Long reminderTime;
    Intent intent;

    public appEvent(int id, String title, String description, List<String> owners, List<String> participants, Long startTime, Long reminderTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.owners = owners;
        this.participants = participants;
        this.startTime = startTime;
        this.reminderTime = reminderTime;
    }


    public void setAlarm(Context context) {
        if (startTime == null || reminderTime == null) return;

        String alarmTitle = "Upcoming Event!";
        String alarmDescription = title + " is scheduled to occur on " + dateFormatShort(startTime) + " at " + timeFormatShort(startTime);
        intent = new Intent(context, NotificationReciever.class);

        intent.putExtra("title", alarmTitle);
        intent.putExtra("description", alarmDescription);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        }else {
            pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent);
    }

    public void cancelAlarm(Context context){
        if (intent == null) return;
        PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT).cancel();
    }

    // ############################################################################################
    // ################################## Time handling ###########################################
    // ############################################################################################

    // Methods to apply the string formatting variable
    public static String dateFormat(Long l){
        if (l == null) return null;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(l);
        return DateFormat.getDateInstance().format(c.getTime());}

    public static String dateFormatShort(Long l){
        if (l == null) return null;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(l);
        return DateFormat.getDateInstance(DateFormat.SHORT).format(c.getTime());}

    public static String timeFormatShort(Long l){
        if (l == null) return null;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(l);
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());}


    // ############################################################################################
    // ############################### Ownership handling #########################################
    // ############################################################################################


    public List<String> getOwners(){
        return owners;
    }

    public String getOwnersString(){
        if (getOwners() == null) return null;
        return String.join(", ", getOwners());
    }

    public List<String> getParticipants(){
        return participants;
    }

    public String getParticipantsString(){
        if (getParticipants() == null) return null;
        return String.join(", ", getParticipants());
    }

    // ############################################################################################
    // ##################################### Other ################################################
    // ############################################################################################

    public String getTitle(){
        return title;
    }

    public String getDescription(){return description;}

    public int getId(){return id;}

}