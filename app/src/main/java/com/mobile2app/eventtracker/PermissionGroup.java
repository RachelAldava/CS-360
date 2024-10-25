package com.mobile2app.eventtracker;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.Vector;

public class PermissionGroup {
    private static int nextCode = 1;
    public Vector<Integer> requestCodes = new Vector<>();
    public final String[] permissionStrings;

    public final String SharedPrefName;
    private final SharedPreferences sharedPrefToggle;
    private final SharedPreferences.Editor editor;

    public PermissionGroup( Activity activity, String[] permissionStrings, String SharedPrefName){
        this.permissionStrings = permissionStrings;
        for (int i = 0; i < permissionStrings.length; i++) {
            this.requestCodes.addElement(nextCode);
            nextCode++;
        }

        this.SharedPrefName = SharedPrefName;
        sharedPrefToggle = activity.getApplicationContext().getSharedPreferences(SharedPrefName, Context.MODE_PRIVATE);
        editor = sharedPrefToggle.edit();
        editor.apply();

        this.requestPermissions(activity);
    }

    // Iterates through permissions in the group, requesting each.
    public void requestPermissions(Activity activity) {

        ActivityCompat.requestPermissions(activity, permissionStrings, 1);

        /*
        for (int i = 0; i < permissionStrings.length; i++) {
            String text = permissionStrings[i];


            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), permissionStrings[i]) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(activity, new String[]{permissionStrings[i]}, requestCodes.elementAt(i));
                text = requestCodes.elementAt(i) + " sytrue: " + text;
            }
            else{
                text = requestCodes.elementAt(i)+ " false: " + text;
            }

            Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_LONG).show();
        }
         */
    }

    public void saveUserPerm(String username, boolean isEnabled){
        editor.putBoolean(username, isEnabled);
        editor.apply();
    }

    //##############################################################################################
    //##################################### Shared Pref ############################################
    //##############################################################################################

    // Iterates through permissions in the group, returning false if one is not granted
    public boolean systemPermIsGranted(Context context) {
        for (int i = 0; i < permissionStrings.length; i++) {
            if (ContextCompat.checkSelfPermission(context, permissionStrings[i]) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public boolean userPermIsGranted(String username){
        return sharedPrefToggle.getBoolean(username, true);
    }

    public boolean permIsGranted(Context context, String username)
    {
        return systemPermIsGranted(context) && userPermIsGranted(username);
    }
}
