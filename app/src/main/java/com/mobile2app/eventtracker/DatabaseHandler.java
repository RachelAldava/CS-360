package com.mobile2app.eventtracker;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.IpSecAlgorithm;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EventTracker.db";
    private static final int VERSION = 1;
    private static DatabaseHandler instance;
    static SharedPreferences prefs = null;

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    // Returns the instance, or creates if one does not exist
    public static DatabaseHandler getInstance(Context context){
        prefs = context.getSharedPreferences("com.mobile2app.eventtracker", MODE_PRIVATE);
        if (instance == null){
            instance = new DatabaseHandler(context);

            // Read from the database to see if it's empty //todo:: Ignore this in production
            if (prefs.getBoolean("firstrun", true)) {
                instance.seedDB();
                prefs.edit().putBoolean("firstrun", false).commit();
            }
        }
        return instance;
    }

    // --------------------------- Define Tables -----------------------------------
    private static final class EventTable {
        private static final String TABLE = "events";
        private static final String COL_ID = "_id";
        private static final String COL_TITLE = "Title";
        private static final String COL_DESCRIPTION = "Description";
        private static final String COL_OWNER = "Owner";
        private static final String COL_PARTICIPANTS = "Participants";
        private static final String COL_TIME = "EventTime";
        private static final String COL_REMINDER = "ReminderTime";
    }

    private static final class LoginTable {
        private static final String TABLE = "users";
        private static final String COL_USERNAME = "Username";
        private static final String COL_SALT = "Salt";
        private static final String COL_HASH = "Hash";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + EventTable.TABLE + " (" +
                EventTable.COL_ID + " integer primary key, " +
                EventTable.COL_TITLE + " text, " +
                EventTable.COL_DESCRIPTION + " text, " +
                EventTable.COL_OWNER + " text, " +
                EventTable.COL_PARTICIPANTS + " text, " +
                EventTable.COL_TIME + " text, " +
                EventTable.COL_REMINDER + " text)");

        db.execSQL("create table " + LoginTable.TABLE + " (" +
                LoginTable.COL_USERNAME + " text primary key, " +
                LoginTable.COL_SALT + " text, " +
                LoginTable.COL_HASH + " text)");


    }

    private void seedDB() {
        DatabaseHandler instance = this;

        final long rightNow = new Date().getTime();

        final long oneDay = 86400000;
        final long oneYear = oneDay*365;

        instance.addNewUser("Alice", "password");
        instance.addNewUser("Bob", "password");
        instance.addNewUser("Claire", "qwerty");

        String sallust = "Deinde se ex curia domum proripuit. Ibi multa ipse secum volvens, quod neque insidiae consuli procedebant et ab incendio intellegebat urbem vigiliis munitam, optumum factu credens exercitum augere ac prius quam legiones scriberentur multa antecapere, quae bello usui forent, nocte intempesta cum paucis in Manliana castra profectus est. Sed Cethego atque Lentulo ceterisque, quorum cognoverat promptam audaciam, mandat quibus rebus possent opes factionis confirment, insidias consuli maturent, caedem, incendia, aliaque belli facinora parent: sese prope diem cum magno exercitu ad urbem accessurum.";

        instance.addNewEvent(
                "Et title de Lorem numero unum",
                "et description des Ipsum.",
                new ArrayList<String>(Arrays.asList("Alice, Bob".split(","))),
                new ArrayList<String>(Arrays.asList("Claire".split(","))),
                rightNow + (oneDay * 2),
                rightNow + (oneDay * 1));

        instance.addNewEvent(
                "Et title de Lorem numero duo",
                "et description des Ipsum. Wo ist der Konsul? Er schuldet mir Geld! Um die Schulden zu begleichen, werde ich ihm alle seine Ziegen nehmen.",
                new ArrayList<String>(Arrays.asList("Alice".split(","))),
                new ArrayList<String>(Arrays.asList("Claire".split(","))),
                rightNow + oneYear,
                null);

        instance.addNewEvent(
                "Et title de Lorem numero tria",
                sallust,
                new ArrayList<String>(Arrays.asList("Bob".split(","))),
                null,
                null,
                null);
    }

    //##############################################################################################
    //##############################################################################################
    //################################## EVENT DATABASE ############################################
    //##############################################################################################
    //##############################################################################################

    private void addNewEvent(String title, String description, List<String> owners, List<String> participants, Long startTime, Long reminderTime){
        //todo:: Add logic which sanitizes all string inputs to prevent SQL injection
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = getEventContentVals(title, description, owners, participants, startTime, reminderTime);

        int id = getNextEventID();
        values.put(EventTable.COL_ID, id);

        // Insert new row.
        db.insert(EventTable.TABLE, null, values);

        // close the database
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        db.execSQL("drop table if exists " + EventTable.TABLE);
        onCreate(db);
    }

    private ArrayList<appEvent> eventReadQuery(String queryParameters)
    {
        // on below line we are creating a
        // database for reading our database.
        SQLiteDatabase db = this.getReadableDatabase();

        // on below line we are creating a cursor with query to
        // read data from database.
        // todo:: implement input sanitization
        Cursor cursorEvents
                = db.rawQuery("SELECT * FROM " + EventTable.TABLE + queryParameters, null);

        // on below line we are creating a new array list.
        ArrayList<appEvent> eventArrayList
                = new ArrayList<>();

        // moving our cursor to first position.

        if (cursorEvents.moveToFirst()) {
            int id;
            String title, description, ownersString, participantsString, startTimeString, reminderTimeString;
            do {
                id = cursorEvents.getInt(0);
                title = cursorEvents.getString(1);
                description = cursorEvents.getString(2);
                ownersString = cursorEvents.getString(3);
                participantsString = cursorEvents.getString(4);
                startTimeString = cursorEvents.getString(5);
                reminderTimeString = cursorEvents.getString(6);

                List<String> owners;
                try {
                    owners = new ArrayList<String>(Arrays.asList(ownersString.split(",")));
                } catch(Exception e){
                    owners = null;
                }

                List<String> participants;
                try {
                    participants = new ArrayList<String>(Arrays.asList(participantsString.split(",")));
                } catch(Exception e){
                    participants = null;
                }

                Long startTime;
                try {
                    startTime = Long.parseLong(startTimeString);
                } catch(Exception e){
                    startTime = null;
                }

                Long reminderTime;
                try {
                    reminderTime = Long.parseLong(reminderTimeString);
                } catch(Exception e){
                    reminderTime = null;
                }

                // on below line we are adding the data from
                // cursor to our array list.
                eventArrayList.add(new
                        appEvent( id, title, description, owners,
                        participants, startTime, reminderTime));
            } while (cursorEvents.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        cursorEvents.close();
        return eventArrayList;
    }

    private int getNextEventID(){
        String query = "SELECT MAX(" + EventTable.COL_ID + ") AS max_id FROM " + EventTable.TABLE;
        Cursor cursor = this.getWritableDatabase().rawQuery(query, null);
        int id = 0;
        if (cursor.moveToFirst())
        {
            do
            {
                id = cursor.getInt(0);
            } while(cursor.moveToNext());
        }
        return id + 1;
    }

    private ContentValues getEventContentVals(String title, String description, List<String> owners, List<String> participants, Long startTime, Long reminderTime){
        // process data
        String ownersString;
        if (owners == null) ownersString = null;
        else {
            ownersString = String.join(", ", owners);
            ownersString = "admin, " + ownersString;
        }

        String participantsString;
        if (participants == null) participantsString = null;
        else {
            participantsString = String.join(", ", participants);
            participantsString = "admin, " + participantsString;
        }

        String startTimeString;
        if (startTime == null) startTimeString = null;
        else startTimeString = startTime.toString();

        String reminderTimeString;
        if (reminderTime == null) reminderTimeString = null;
        else reminderTimeString = reminderTime.toString();

        // Preparing K:V
        ContentValues values = new ContentValues();
        values.put(EventTable.COL_TITLE, title);
        values.put(EventTable.COL_DESCRIPTION, description);
        values.put(EventTable.COL_OWNER, ownersString);
        values.put(EventTable.COL_PARTICIPANTS, participantsString);
        values.put(EventTable.COL_TIME, startTimeString);
        values.put(EventTable.COL_REMINDER, reminderTimeString);

        return values;
    }

    public appEvent writeEvent(Integer id, String title, String description, List<String> owners, List<String> participants, Long startTime, Long reminderTime){
        if (id == null) {// if we are creating a new event)
            int predictedID = getNextEventID();
            addNewEvent(title, description, owners,  participants, startTime, reminderTime);
            return readOneEvent(predictedID);
        }
        else{
            updateEvent(id, title, description, owners,  participants, startTime, reminderTime);
            return readOneEvent(id);
        }
    }

    public void updateEvent(Integer id, String title, String description, List<String> owners, List<String> participants, Long startTime, Long reminderTime){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = getEventContentVals(title, description, owners, participants, startTime, reminderTime);

        db.update(EventTable.TABLE, values, EventTable.COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public ArrayList<appEvent> readEvents(){
        String user = User.getCurrUsername();
        if (user == null) return new ArrayList<appEvent>();
        if (user.equals("admin")) return eventReadQuery("");
        String queryPRE = " WHERE ";
        String queryPOST = " LIKE '%, " + user + "%'" ;

        String ownersQuery = queryPRE + EventTable.COL_OWNER + queryPOST;
        String participantsQuery = queryPRE + EventTable.COL_OWNER + queryPOST;

        ArrayList<appEvent> owned = eventReadQuery(ownersQuery);
        ArrayList<appEvent> participating = eventReadQuery(participantsQuery);
        ArrayList<appEvent> total = participating;
        for (appEvent a : owned){
            boolean contains = false;
            for (appEvent b : participating){
                if (a.getId() == b.getId()) contains = true;
            }
            if (!contains) total.add(a);
        }

        return total;
    }

    public appEvent readOneEvent(int id){
        String query = " WHERE _id = '" + id + "'";
        ArrayList<appEvent> list = eventReadQuery(query);
        if (list.isEmpty()) return null;
        else return list.get(0);
    }

    public long deleteEvent(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(EventTable.TABLE, "_id=?", new String[]{"" + id});
    }


    //##############################################################################################
    //##############################################################################################
    //################################### USER DATABASE ############################################
    //##############################################################################################
    //##############################################################################################

    public Boolean addNewUser(String username, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        //todo:: Add logic which sanitizes all string inputs to prevent SQL injection

        // process data
        if (username.isEmpty()
                || username.equalsIgnoreCase("admin")
                || username.equalsIgnoreCase("default")) return false;// Prohibited names
        if (password.isEmpty()) return false;
        if (!isUsernameAvailable(username)) return false;

        // Generating salt improves password security if database is breached
        Random random = new Random();
        String salt = String.valueOf(random.nextInt());

        // Create loginData
        LoginData data = new LoginData(username, salt);

        // generate hash
        data.setHash(password);

        // Preparing K:V
        values.put(LoginTable.COL_USERNAME, data.username);
        values.put(LoginTable.COL_SALT, data.salt);
        values.put(LoginTable.COL_HASH, data.hash);

        // Insert new row.
        db.insert(LoginTable.TABLE, null, values);

        // close the database
        db.close();
        return true;
    }

    private class LoginData{
        public String username;
        public String salt;
        public String hash;

        public LoginData(String username, String salt){
            this.username = username;
            this.salt = salt;
        }
        public LoginData(String username, String salt, String hash){
            this.username = username;
            this.salt = salt;
            this.hash = hash;
        }

        public boolean checkPassword(String password){
            String passhash = this.getHash(password + salt);
            return this.hash.equals(passhash);
        }

        public void setHash(String password){
            this.hash = getHash(password + salt);
        }

        private String getHash(String string){
            String hash;
            try{
                MessageDigest md = MessageDigest.getInstance("SHA-512");
                byte[] messageDigest = md.digest(string.getBytes());
                BigInteger no = new BigInteger(1, messageDigest);
                hash = no.toString(16);
                while (hash.length() < 32) {
                    hash = "0" + hash;
                }
            }
            catch( Exception e) {return null;}
            return hash;
        }
    }

    private LoginData getUser(String username)
    {
        // on below line we are creating a
        // database for reading our database.
        SQLiteDatabase db = this.getReadableDatabase();

        // on below line we are creating a cursor with query to
        // read data from database.
        // todo:: implement input sanitization
        Cursor cursorEvents
                = db.rawQuery("SELECT * FROM " + LoginTable.TABLE + " WHERE username = '" + username + "'", null);


        // moving our cursor to first position.
        LoginData user;

        if (cursorEvents.moveToFirst()) {
            String retrievedUsername, retrievedSalt, retrievedHash;
            do {
                retrievedUsername = cursorEvents.getString(0);
                retrievedSalt = cursorEvents.getString(1);
                retrievedHash = cursorEvents.getString(2);

                user = new LoginData(retrievedUsername, retrievedSalt, retrievedHash);

            } while (cursorEvents.moveToNext());
            // moving our cursor to next.
        }
        else user = null;
        // at last closing our cursor
        // and returning our array list.
        cursorEvents.close();
        return user;
    }

    public boolean isUsernameAvailable(String username){
        if (getUser(username) == null) return true;
        else return false;
    }

    public boolean login(String user, String password){
        LoginData logindata = getUser(user);
        if (logindata == null) return false;
        return logindata.checkPassword(password);
    }
}


