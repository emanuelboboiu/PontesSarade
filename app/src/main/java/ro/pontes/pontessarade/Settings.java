package ro.pontes.pontessarade;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

/*
 * Class started on Sunday, 01 March 2015, created by Manu.
 * This class contains useful methods like save or get settings.
 * */

public class Settings {

    // The file name for save and load preferences:
    private final static String PREFS_NAME = "psSettings";

    private final Context context;

    public Settings(Context context) {
        this.context = context;
    }

    // A method to post a new game and the number of hands played during the
    // sessions:
    public void postStats(final String gameIdInDB, final int numberOfGamesPlayed) {
        String url = "http://www.pontes.ro/ro/divertisment/games/soft_counts.php?pid=" + gameIdInDB + "&score=" + numberOfGamesPlayed;

        new GetWebData().execute(url);
    } // end post data statistics.

    // Methods for save and read preferences with SharedPreferences:

    // A method to detect if a preference exist or not:
    public boolean preferenceExists(String key) {
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.contains(key);
    } // end detect if a preference exists or not.

    // Save a boolean value:
    public void saveBooleanSettings(String key, boolean value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        // Commit the edits!
        editor.apply();
    } // end save boolean.

    // Read boolean preference:
    public boolean getBooleanSettings(String key) {
        boolean value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getBoolean(key, false);

        return value;
    } // end get boolean preference from SharedPreference.

    // Save a integer value:
    public void saveIntSettings(String key, int value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        // Commit the edits!
        editor.apply();
    } // end save integer.

    // Read integer preference:
    public int getIntSettings(String key) {
        int value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getInt(key, 0);

        return value;
    } // end get integer preference from SharedPreference.

    // For float values in shared preferences:
    public void saveFloatSettings(String key, float value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        // Commit the edits!
        editor.apply();
    } // end save integer.

    // Read integer preference:
    public float getFloatSettings(String key) {
        float value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getFloat(key, 3.0F); // a default value like the value
        // for moderate magnitude.

        return value;
    } // end get float preference from SharedPreference.

    // Save a String value:
    public void saveStringSettings(String key, String value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        // Commit the edits!
        editor.apply();
    } // end save String.

    // Read String preference:
    public String getStringSettings(String key) {
        String value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getString(key, null);

        return value;
    } // end get String preference from SharedPreference.
    // End read and write settings in SharedPreferences.

    // Charge Settings function:
    public void chargeSettings() {

        // Determine if is first launch of the program:
        boolean isNotFirstRunning = getBooleanSettings("isFirstRunning");

        if (!isNotFirstRunning) {
            saveBooleanSettings("isFirstRunning", true);
            // Make default values in SharedPreferences:
            setDefaultSettings();
        }

        // Now charge settings:
        // Charge STRUCTURA:
        // MainActivity.chosenStructura = getStringSettings("chosenStructura");
        MainActivity.separator = getStringSettings("separator");
        if (preferenceExists("separatorType")) {
            MainActivity.separatorType = getIntSettings("separatorType");
        }
        // Play or not the sounds and speech:
        MainActivity.isSpeech = getBooleanSettings("isSpeech");
        MainActivity.isSound = getBooleanSettings("isSound");

        // For done button of the keyboard to send a try:
        MainActivity.isImeAction = getBooleanSettings("isImeAction");

        // For text size:
        MainActivity.textSize = getIntSettings("textSize");
        // Is shake detector or not:
        MainActivity.isShake = getBooleanSettings("isShake");
        // The magnitude of the shake detector:
        MainActivity.onshakeMagnitude = getFloatSettings("onshakeMagnitude");

        // Wake lock, keep screen awake:
        MainActivity.isWakeLock = getBooleanSettings("isWakeLock");

        // Charge the sum of marks and number of them:
        MainActivity.sumOfMarks = getIntSettings("sumOfMarks");
        MainActivity.numberOfMarks = getIntSettings("numberOfMarks");
        MainActivity.totalCorrect = getIntSettings("totalCorrect");

        /* About number of launches, useful for information, rate and others: */
        // Get current number of launches:
        MainActivity.numberOfLaunches = getIntSettings("numberOfLaunches");
        // Increase it by one:
        MainActivity.numberOfLaunches++;
        // Save the new number of launches:
        saveIntSettings("numberOfLaunches", MainActivity.numberOfLaunches);
    } // end charge settings.

    public void setDefaultSettings() {
        MainActivity.separatorType = 1; // line and comma.
        saveIntSettings("separatorType", 1);
        // saveStringSettings("chosenStructura", "0+0");
        saveStringSettings("separator", "-");
        saveBooleanSettings("isStarted", false);
        // // Activate speech if accessibility, explore by touch is enabled:
        MainActivity.isSpeech = GUITools.isAccessibilityEnabled(context);
        saveBooleanSettings("isSpeech", MainActivity.isSpeech);
        saveBooleanSettings("isSound", true);
        saveBooleanSettings("isImeAction", true);
        // For text size for lines:
        saveIntSettings("textSize", 20);
        // Activate shake detection:
        saveBooleanSettings("isShake", false);
        // Set on shake magnitude to 2.2F: // now default value, medium.
        saveFloatSettings("onshakeMagnitude", 3.0F);
        // For keeping screen awake:
        saveBooleanSettings("isWakeLock", false);
        // For sumOfMarks and numberOfMarks:
        saveIntSettings("sumOfMarks", 0);
        saveIntSettings("numberOfMarks", 0);
        MainActivity.averageOfMarks = 0.0;
        saveIntSettings("totalCorrect", 0);
        MainActivity.totalCorrect = 0;
        // Save DataBase version to 0:
        saveIntSettings("dbVer", 0);
    } // end setDefaultSettings function.

    // This is a subclass:
    private static class GetWebData extends AsyncTask<String, String, String> {

        // execute before task:
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Execute task
        String urlText = "";

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder content = new StringBuilder();
            urlText = strings[0];
            try {
                // Create a URL object:
                URL url = new URL(urlText);
                // Create a URLConnection object:
                URLConnection urlConnection = url.openConnection();
                // Wrap the URLConnection in a BufferedReader:
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                // Read from the URLConnection via the BufferedReader:
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line);
                }
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return content.toString();
        } // end doInBackground() method.

        // Execute after task with the task result as string:
        @Override
        protected void onPostExecute(String s) {
            // Do nothing yet.
        } // end postExecute() method.
    } // end subclass.

} // end Settings Class.
