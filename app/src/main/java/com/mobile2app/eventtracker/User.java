package com.mobile2app.eventtracker;
import android.content.Context;

public class User {
    private static String currUser;

    public static String getCurrUsername(){
        if (currUser == null) return "default";
        return currUser;
    }

    public static boolean userLoggedIn(){
        return (currUser != null);
    }

    public static void Admin(){currUser = "admin";}// TODO:: Remove from production

    public static boolean register(Context context, String username, String password){
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        return db.addNewUser(username, password);
    }

    public static Boolean login(Context context, String username, String password){
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        boolean loginSuccess = db.login(username, password);
        if (loginSuccess) currUser = username;
        return loginSuccess;
    }
}
