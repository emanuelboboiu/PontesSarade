package ro.pontes.pontessarade;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    // The following fields are used for the shake detection:
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    // End fields declaration for shake detector.

    public final static String EXTRA_MESSAGE = "ro.pontes.pontessarade.MESSAGE";
    private boolean isStarted = false;
    private boolean isResolved = true;
    public static int numberOfLaunches = 0;

    private TestAdapter mDbHelper;

    private int hintLevel = 0;
    public static String separator = "-";
    private String separatorWord = "";
    public static String chosenStructura = "0+0";
    public static boolean isSpeech = false;
    public static boolean isSound = true;
    public static boolean isShake = false;
    public static float onshakeMagnitude = 3.0F;
    public static boolean isWakeLock = false;
    public static boolean isImeAction = true;
    public static int textSize = 20; // for TextViews.
    private int mPaddingDP = 3;

    public static double averageOfMarks = 0.0;
    public static int sumOfMarks = 0;
    public static int numberOfMarks = 0;
    public static int totalCorrect = 0;
    private int curTotal = 0;
    private static int lastId = 1;
    private final int initialMark = 10;
    private int currentMark = 10;
    private static int curAuthorId = 3;

    private int first = 0;
    private int second = 0;
    private int total = 0;
    private String lastHintText = "";

    // Some global strings:
    private String curRezolvare = "";
    private String curStructura = "";
    private String tvCurStatusMark;
    private String tvLastStatusMark;

    private String tv_structure = "";
    private String tv_input_hint = "";

    // Some global buttons and TextViews:
    private Button btTry;
    private Button btHint;
    private Button btInfo;
    private TextView tvStatus;

    // To save data:
    private Settings set;

    // For speech during the game:
    private SpeakText speak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Charge settings:
        set = new Settings(this);
        set.chargeSettings();

        /*
         * Charge also the last SRD if it's the case. Here we determine if
         * isStarted is true or false and we also will have a lastId:
         */
        chargeLast();

        speak = new SpeakText(this);

        // To keep screen awake:
        if (MainActivity.isWakeLock) {
            getWindow()
                    .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } // end wake lock.

        // Calculate the pixels in DP for mPaddingDP, for TextViews of the lines
        // of SARADA:
        int paddingPixel = 3;
        float density = getResources().getDisplayMetrics().density;
        mPaddingDP = (int) (paddingPixel * density);
        // end calculate mPaddingDP

        // Start things for our database:
        mDbHelper = new TestAdapter(this);
        mDbHelper.createDatabase();
        mDbHelper.open();

        // Charge strings:
        tv_structure = getString(R.string.tv_structure);
        tv_input_hint = getString(R.string.tv_input_hint);
        tvCurStatusMark = getString(R.string.tv_current_mark);
        tvLastStatusMark = getString(R.string.tv_last_mark);
        separatorWord = getString(R.string.separator_word);

        // Charge global buttons:
        btTry = findViewById(R.id.btTry);
        btHint = findViewById(R.id.btHint);
        btInfo = findViewById(R.id.btInfo);
        tvStatus = findViewById(R.id.tvStatus);

        // Add listener for long tap on Info Button:
        btInfo.setOnLongClickListener(view -> {
            showAnswer();
            return true;
        });
        // End add listener for long click on info button.

        averageOfMarks = getAverage();

        // ShakeDetector initialisation
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setShakeThresholdGravity(MainActivity.onshakeMagnitude);
        /*
         * method you would use to setup whatever you want done once the
         * device has been shook.
         */
        mShakeDetector.setOnShakeListener(this::handleShakeEvent);
        // End initialisation of the shake detector.

        // New or old in next method:
        otherNow();

        GUITools.checkIfRated(this);
    } // end onCreate method.

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            goToSettings();
        } else if (id == R.id.display_settings) {
            goToDisplaySettings();
        } else if (id == R.id.structure_settings) {
            goToStructure();
        } else if (id == R.id.mnuRate) {
            GUITools.showRateDialog(this);
        } // end if rate option was chosen in menu.

        else if (id == R.id.reset_defaults) {
            // Get the strings:
            String tempTitle = getString(R.string.title_default_settings);
            String tempBody = getString(R.string.body_default_settings);
            new AlertDialog.Builder(this)
                    .setTitle(tempTitle)
                    .setMessage(tempBody)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.yes,
                            (dialog, whichButton) -> {
                                set.setDefaultSettings();
                                set.chargeSettings();
                            }).setNegativeButton(R.string.no, null).show();
        } // end if is for set to defaults clicked in main menu.
        else if (id == R.id.autoriSaradeTitle) {
            showAuthors();
        } // end if about game is chosen in main menu.
        else if (id == R.id.about_dialog) {
            GUITools.aboutDialog(this);
        } // end if about game is chosen in main menu.
        else if (id == R.id.online_help) {
            GUITools.openHelp(this);
        } // end if open help is chosen in menu.
        else if (id == R.id.sendSaradaTitle) {
            GUITools.openBrowser(this,
                    "http://www.android.pontes.ro/pontessarade/trimite.php");
        } // end if open help is chosen in menu.

        return super.onOptionsItemSelected(item);
    }

    public void goToSettings() {
        // Called when the user clicks the settings option in menu:
        Intent intent = new Intent(this, SettingsActivity.class);
        String message;
        message = "Pontes Sarade"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end go to settings method.

    public void goToDisplaySettings() {
        Intent intent = new Intent(this, DisplaySettingsActivity.class);
        String message;
        message = "Pontes Sarade"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end go to display settings method.

    public void goToStructure() {
        // Called when the user clicks the settings structure option in menu:
        Intent intent = new Intent(this, StructureActivity.class);
        String message;
        message = "Pontes Sarade"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end go to settings method.

    // A method to show all authors and their number of creations:
    public void showAuthors() {
        String sql = "SELECT autor, count(autor) as countNR FROM sarade GROUP BY autor ORDER BY countNR DESC";
        Cursor cursor = mDbHelper.getTestData(sql);

        StringBuilder theAuthorsListText = new StringBuilder();

        cursor.moveToFirst();
        do {
            int authorId = cursor.getInt(0);
            String sql2 = "SELECT nume FROM autori WHERE _id='" + authorId
                    + "'";
            Cursor cursor2 = mDbHelper.getTestData(sql2);
            String theAuthor = cursor2.getString(0);
            cursor2.close();
            theAuthorsListText.append(theAuthor).append(" - ").append(cursor.getString(1)).append(";\n");
        } while (cursor.moveToNext());
        // end do ... while.
        cursor.close();

        GUITools.alert(this, getString(R.string.autori_sarade_title),
                theAuthorsListText.toString());
    } // end show authors.

    @Override
    public void onResume() {
        super.onResume();
        if (MainActivity.isShake) {
            // Add the following line to register the Session Manager Listener
            // onResume
            mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                    SensorManager.SENSOR_DELAY_UI);
        }
    } // end onResume method.

    @Override
    public void onPause() {
        // Add here what you want to happens on pause:
        // Post statistics:
        if (curTotal > 0) {
            set.postStats("13", curTotal); // 13 is the id in soft counts.
            curTotal = 0;
        }
        if (MainActivity.isShake) {
            // Add the following line to unregister the Sensor Manager onPause
            mSensorManager.unregisterListener(mShakeDetector);
        }
        super.onPause();
    } // end onPause method.

    @Override
    public void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    } // end onDestroy method.

    // Methods for buttons:
    public void tryButton(View view) {
        tryNow();
    } // end tryButton method.

    public void tryDirectlyFromKeyboard() {
        if (isImeAction) {
            tryNow();
        }
    } // end try directly from keyboard.

    public void tryNow() {
        if (isStarted) {
            // Check if it is something written there, at least one letter:
            EditText et = findViewById(12345);
            if (et.getText().toString().length() > 0) {
                checkIfIsCorrect();

                // If it is not already resolved:
                if (!isResolved) {
                    SoundPlayer.playSimple(this, "lose_srd");
                    // Check which letters are correct written:
                    TextView tv = findViewById(123456);
                    String temp = getCurHint(hintLevel, curRezolvare);
                    tv.setText(temp);
                    // Content description to be said and set:
                    String contentDescription = makeContentDescriptionHint(temp);
                    tv.setContentDescription(contentDescription);
                    speak.say(contentDescription, true);
                    if (currentMark > 3) {
                        currentMark = currentMark - 1;
                    }
                    setStatusMark(currentMark);
                    set.saveIntSettings("currentMark", currentMark);
                } else {
                    isStarted = false;
                    set.saveBooleanSettings("isStarted", isStarted);
                    setStatusMark(currentMark);
                } // end if is already resolved.
            } // end if there is at least one letter written in the EditText.
        } // end if isStarted.
        enableOrDisableButtons();
    } // end tryNow method.

    // A method to verify if is correct resolved:
    public void checkIfIsCorrect() {
        // Get the text from EditView:
        EditText et = findViewById(12345);
        String userRes = replaceDiacritics(et.getText().toString());
        String realRes = replaceDiacritics(curRezolvare);
        // Compare the strings to see if is a good resolve:
        if (userRes.equals(realRes)) {
            isResolved = true;
            set.saveBooleanSettings("isResolved", isResolved);
            updateMarks(currentMark);
            // Update the EditText and the hint label:
            et.setText(curRezolvare);
            et.setEnabled(false);
            TextView tv = findViewById(123456);
            tv.setText(curRezolvare);
            tv.setContentDescription(curRezolvare);
            totalCorrect = totalCorrect + 1;
            set.saveIntSettings("totalCorrect", totalCorrect);
            SoundPlayer.playSimple(this, "win_srd");
            // Announce the finish:
            speak.say(String.format(getString(R.string.speak_was_resolved),
                    "" + currentMark), true);
        }
    } // end check if is correct method.

    // A method to replace the string with special letters with corresponding
    // non_diacritic:
    private String replaceDiacritics(String str) {
        // Make it lower case:
        str = str.toLowerCase(Locale.getDefault());

        StringBuilder diacritics = new StringBuilder("ăşţâîșț");
        StringBuilder nonDiacritics = new StringBuilder("astaist");
        StringBuilder sb = new StringBuilder(str);

        for (int i = 0; i < sb.length(); i++) {
            for (int j = 0; j < diacritics.length(); j++) {
                if (sb.charAt(i) == diacritics.charAt(j)) {
                    sb.setCharAt(i, nonDiacritics.charAt(j));
                    break;
                }
            } // end inner for.
        } // end outer for.

        return sb.toString();
    } // end replace special letters.

    public void handleShakeEvent(int count) {
        confirmOtherButtonOrShake();
    } // end handle shake detection.

    public void otherButton(View view) {
        confirmOtherButtonOrShake();
    } // end otherButton() method.

    private void confirmOtherButtonOrShake() {
        // We need confirmation only if it is started:
        if (isStarted) {
            // Get the strings:
            String tempTitle = getString(R.string.title_new_other);
            String tempBody = getString(R.string.body_new_other);
            new AlertDialog.Builder(this)
                    .setTitle(tempTitle)
                    .setMessage(tempBody)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.yes,
                            (dialog, whichButton) -> otherButtonOrShake()).setNegativeButton(R.string.no, null).show();
        } else { // the game is not started:
            otherButtonOrShake();
        }
    } // end confirmOtherButtonOrShake() method.

    private void otherButtonOrShake() {
        isStarted = false;
        // Check if it was already resolved, otherwise the mark is 3:
        if (!isResolved) {
            updateMarks(3);
        }
        otherNow();
    } // end otherButtonOrShake() method.

    public void otherNow() {
        // If it is not started:
        if (!isStarted) {
            lastId = getNewId();
            set.saveIntSettings("lastId", lastId);
        } // end if is not started, a new lastId..
        newSarada(lastId);
    } // end otherButton method.

    public void hintButton(View view) {
        hintLevel = hintLevel + 1; // increment the actual level for help.
        set.saveIntSettings("hintLevel", hintLevel);
        if (hintLevel <= 3) {
            SoundPlayer.playSimple(this, "hint_srd");
            // The hint label:
            TextView tv = findViewById(123456);
            String temp = getCurHint(hintLevel, curRezolvare);
            tv.setText(temp);
            String contentDescription = makeContentDescriptionHint(temp);
            tv.setContentDescription(contentDescription);
            speak.say(contentDescription, true);
            // Save also the lastHintText:
            set.saveStringSettings("lastHintText", temp);
            // Set also the new mark:
            if (currentMark >= 5) {
                currentMark = currentMark - 2;
            } // end if can be taken 2 point of mark.
            else {
                currentMark = 3;
            } // end if mark must be 3.
            setStatusMark(currentMark);
            set.saveIntSettings("currentMark", currentMark);
            enableOrDisableButtons();
        } // end if there is reason to show hints, the level is not maximum.
    } // end hintButton method.

    public void infoButton(View view) {
        // Determine the author:
        String sql = "SELECT * FROM autori WHERE _id=" + curAuthorId;
        Cursor cursor = mDbHelper.getTestData(sql);
        String curAuthor = cursor.getString(1);
        cursor.close();
        averageOfMarks = getAverage();
        GUITools.alert(this, getString(R.string.info_title), String
                .format(getString(R.string.info_message), curStructura,
                        curAuthor, "" + numberOfMarks, "" + totalCorrect, ""
                                + averageOfMarks));
    } // end infoButton method.

    // A method to show the correct answer, if Info Button is long clicked:
    public void showAnswer() {
        if (!isResolved) {
            // Update the EditText and the hint label:
            EditText et = findViewById(12345);
            // Fill the EditText with dashes:
            StringBuilder sb = new StringBuilder(curRezolvare.length());
            for (int i = 0; i < curRezolvare.length(); i++) {
                sb.append(separator);
            } // end for.
            et.setText(sb.toString());
            et.setEnabled(false); // disable the EditText.
            TextView tv = findViewById(123456);
            currentMark = 3;
            updateMarks(currentMark);
            tv.setText(curRezolvare);
            isResolved = true;
            set.saveBooleanSettings("isResolved", isResolved);
            isStarted = false;
            set.saveBooleanSettings("isStarted", isStarted);
            setStatusMark(currentMark);
            hintLevel = 3;
            enableOrDisableButtons();
            GUITools.alert(this, getString(R.string.info_long_title),
                    String.format(getString(R.string.info_long_message),
                            curRezolvare));
        } else {
            GUITools.alert(this, getString(R.string.info_long_title2), String
                    .format(getString(R.string.info_long_message2),
                            curRezolvare));
        } // end else if it was resolved.
    } // end showResultButton.

    // Get a new ID of a new SARADA:
    private int getNewId() {
        int r;
        String sql;
        if (chosenStructura.equals("0+0")) {
            sql = "SELECT _id FROM sarade ORDER BY random() LIMIT 1";
        } else {
            sql = "SELECT _id FROM sarade WHERE structura='" + chosenStructura
                    + "' ORDER BY random() LIMIT 1";
        }
        Cursor cursor = mDbHelper.getTestData(sql);
        r = cursor.getInt(0);
        cursor.close();
        return r;
    } // end get a new Id for a SARADA.

    /*
     * A method to determine the number of letter of the REZOLVARE and
     * STRUCTURA:
     */
    private void determineNumberOfLetters() {
        // Determine number of letters of the REZOLVARE and STRUCTURA:
        String[] aTemp = curStructura.split("\\+");

        first = Integer.parseInt(aTemp[0]);
        second = Integer.parseInt(aTemp[1]);
        total = first + second;
    } // end determine number of letters method.

    // A method to return a string for the hint label:
    private String getCurHint(int level, String rezolvare) {
        StringBuilder temp = new StringBuilder();
        StringBuilder temp2 = new StringBuilder(rezolvare);

        /*
         * First make dashes for each character wrong found in REZOLVARE of the
         * user. We get the already found text on the hint label to compare it
         * char by char with REZOLVARE:
         */
        StringBuilder temp0;
        if (currentMark == initialMark) {
            // Make all dashes:
            for (int i = 0; i < total; i++) {
                temp.append(separator);
            } // end for.
        } else { // the mark is less than 10:
            // If there are some chars already written on the label.
            temp0 = new StringBuilder(lastHintText);

            for (int i = 0; i < total; i++) {
                if (temp0.charAt(i) != temp2.charAt(i)) {
                    temp.append(separator);
                } else {
                    temp.append(temp2.charAt(i));
                }
            } // end for make dashes for each wrong letter found in REZOLVARE.
        } // end if there is something written on the hint label, the mark is
        // not 10.

        // Set matched letters after user typed something:
        EditText et = findViewById(12345);
        String temp3 = et.getText().toString();
        et.setText("");
        int temp3Length = temp3.length();
        if (temp3Length > 0) {
            StringBuilder sb = new StringBuilder(replaceDiacritics(temp3));
            StringBuilder temp2NonDiacritics = new StringBuilder(
                    replaceDiacritics(rezolvare));
            for (int i = 0; i < rezolvare.length() && i < temp3Length; i++) {
                if (temp2NonDiacritics.charAt(i) == sb.charAt(i)) {
                    temp.setCharAt(i, temp2.charAt(i));
                }
            } // end for.
        } // end if there are letters typed in the text view.

        // Changes depending of the hintLevel:
        if (level == 0) {
            // Do nothing, it's enough the work done above.
        } else if (level == 1) {
            // Add the a letter of the REZOLVARE instead first dash found:
            for (int i = 0; i < temp2.length(); i++) {
                if (temp.charAt(i) == '-') {
                    temp.setCharAt(i, temp2.charAt(i));
                    break;
                } // end if a dash was found.
            } // end for.
        } else if (level == 2) {
            // Add the first letters of each word in the current REZOLVARE:
            // First add a letter for first word:
            for (int i = 0; i < first; i++) {
                if (temp.charAt(i) == '-') {
                    temp.setCharAt(i, temp2.charAt(i));
                    break;
                } // end if a dash was found in first word.
            } // end for.
            // Check now for a dash found in the second word:
            for (int i = first; i < temp.length(); i++) {
                if (temp.charAt(i) == '-') {
                    temp.setCharAt(i, temp2.charAt(i));
                    break;
                } // end if a dash was found in second word.
            } // end for second word.
        } else if (level == 3) {
            // Show first word entirely:
            boolean areOnlyLetters = true; // if there are already only letters
            // in first word, we give another
            // hint, for first dash found.
            for (int i = 0; i < first; i++) {
                if (temp.charAt(i) == '-') {
                    areOnlyLetters = false;
                }
                temp.setCharAt(i, temp2.charAt(i));
            }
            temp.setCharAt(first, temp2.charAt(first)); // add also the first
            // letter of the second
            // word, it was added
            // anyway at previous
            // hint level.
            // Now if were only letters in first word, the word was guessed by
            // user, let's give him another letter:
            if (areOnlyLetters) {
                for (int i = 0; i < temp2.length(); i++) {
                    if (temp.charAt(i) == '-') {
                        temp.setCharAt(i, temp2.charAt(i));
                        break;
                    } // end if a dash was found.
                } // end for.
            } // end if were only letters.
        } // end depending of the level chosen.

        lastHintText = temp.toString();
        return temp.toString();
    }

    // Generate a new challenge:
    public void newSarada(int curID) {
        if (!isStarted) {
            SoundPlayer.playSimple(this, "new_srd");
            isResolved = true;
            curTotal = curTotal + 1; // the total shown SARADE for statistics DB
            hintLevel = 0;
            set.saveIntSettings("hintLevel", hintLevel);
            currentMark = initialMark;
            set.saveIntSettings("currentMark", currentMark);
        } // end if is not started.
        else { // if is started:
            SoundPlayer.playSimple(this, "old_srd");
        } // end if is an old SRD.

        // Now it must be started in any case:
        isStarted = true;
        if (isStarted) { // just an if without reason:
            set.saveBooleanSettings("isStarted", isStarted);
            isResolved = false;
            set.saveBooleanSettings("isResolved", isResolved);

            Cursor cursor = mDbHelper
                    .getTestData("SELECT * FROM sarade WHERE _id=" + curID + "");
            curStructura = cursor.getString(1);
            String curTemp = cursor.getString(2);
            curRezolvare = cursor.getString(3);
            curAuthorId = cursor.getInt(4);
            cursor.close();

            /*
             * Determine in a method the first, second and total, global fields
             * of this class.
             */
            determineNumberOfLetters();

            LinearLayout ll = findViewById(R.id.llMain);
            ll.removeAllViews();

            // A LayoutParams to add next items into the main layout:
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            // Create all TextViews and add them in llMain:
            TextView tv = new TextView(this);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize + 2);
            tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
            String tempStructura = String.format(tv_structure, curStructura);
            tv.setText(tempStructura);
            final String mfTempStructura = tempStructura;
            // Add a listener:
            tv.setOnClickListener(view -> speak.say(mfTempStructura, true));
            // End add listener for a line of text.

            ll.addView(tv, lp);
            // Speak now the tempStructura if speech is enabled:
            speak.say(tempStructura, true);

            // Create the labels with lines of the SARADA:
            String[] aCurTemp = curTemp.split("\\|");

            for (String s : aCurTemp) {
                tv = new TextView(this);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
                // Make each line in capital letter:
                String line = s.substring(0, 1).toUpperCase(
                        Locale.getDefault())
                        + s.substring(1);
                tv.setText(line);
                final String mfLine = line;
                // Add a listener:
                tv.setOnClickListener(view -> speak.say(mfLine, true));
                // End add listener for a line of text.

                ll.addView(tv, lp);
            } // end for.
            // end create labels with lines of the SARADA.

            // The edit text:
            // Set an EditText view to get user input
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            input.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            input.setHint(tv_input_hint);
            input.setId(12345);

            // Add also an action listener:
            input.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    tryDirectlyFromKeyboard();
                }
                return false;
            });
            // End add action listener.
            ll.addView(input, lp);
            // end edit text.

            // The hint label:
            tv = new TextView(this);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize + 2);
            tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
            tv.setId(123456);
            String tempHint = getCurHint(hintLevel, curRezolvare);
            tv.setText(tempHint);

            // A method which make a good contentDescription:a
            final String tempContentDescription = makeContentDescriptionHint(tempHint);
            tv.setContentDescription(tempContentDescription);

            // Add a listener:
            tv.setOnClickListener(view -> {
                // Only if the challenge is started:
                if (isStarted) {
                    speak.say(tempContentDescription, true);
                } // end if it's started.
            });
            // End add listener for a line of text.

            ll.addView(tv, lp);

            setStatusMark(currentMark);
        } // end if is started.
        enableOrDisableButtons();
    } // end newSarada method.

    // The method which makes the contentDescription:
    private String makeContentDescriptionHint(String text) {
        String tcd;
        StringBuilder sb = new StringBuilder(text);
        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < sb.length(); i++) {
            if (Character.toString(sb.charAt(i)).equals(separator)) {
                sb2.append(separatorWord);
            } else {=
                sb2.append(sb.charAt(i));
            } // end if is not the separator.
            // Append also a sign to have spaces at pronunciation:
            sb2.append(", ");
        } // end for.
        tcd = sb2.toString();
        return tcd.substring(0, tcd.length() - 1);
    } // makeContentDescription() method.

    private void enableOrDisableButtons() {
        if (!isStarted) {
            btTry.setEnabled(false);
            btHint.setEnabled(false);
        } else {
            btTry.setEnabled(true);
            if (hintLevel < 3) {
                btHint.setEnabled(true);
            } else {
                btHint.setEnabled(false);
            }
        }
    } // end enableOrDisableButtons.

    // A method to write current mark on the status label:
    private void setStatusMark(int mark) {
        if (isStarted) {
            tvStatus.setText(String.format(tvCurStatusMark, "" + mark, ""
                    + averageOfMarks));
        } else {
            tvStatus.setText(String.format(tvLastStatusMark, "" + mark, ""
                    + averageOfMarks));
        }
    } // end set mark on the status label method.

    private double getAverage() {
        double avg = 0.0;
        if (numberOfMarks > 0 && sumOfMarks > 0) {
            avg = (double) sumOfMarks / numberOfMarks;
            avg = Math.round(avg * 100.0) / 100.0;
        }

        return avg;
    } // end get average of the marks with 2 decimals.

    // A method to save numerOfMarks and sumOfMarks:
    private void saveMarks() {
        set.saveIntSettings("sumOfMarks", sumOfMarks);
        set.saveIntSettings("numberOfMarks", numberOfMarks);
    } // end save marks, sum and number of them.

    private void updateMarks(int mark) {
        sumOfMarks = sumOfMarks + mark;
        numberOfMarks = numberOfMarks + 1;
        saveMarks();
        averageOfMarks = getAverage();
    } // end update mark.

    // A method to charge the last SRD:
    private void chargeLast() {
        // First of all, we must know if isStarted is true:
        isStarted = set.getBooleanSettings("isStarted");
        // Now charge other variables if it is started:
        if (isStarted) {
            isResolved = set.getBooleanSettings("isResolved");
            lastId = set.getIntSettings("lastId");
            currentMark = set.getIntSettings("currentMark");
            hintLevel = set.getIntSettings("hintLevel");
            lastHintText = set.getStringSettings("lastHintText");
        } // end if is started.
    } // end chargeLast method.

} // end MainActivity.
