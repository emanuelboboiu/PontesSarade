package ro.pontes.pontessarade;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Check or check the check boxes, depending of current boolean values:

        // For sounds in game:
        CheckBox cbtSoundsSetting = (CheckBox) findViewById(R.id.cbtSoundsSetting);
        cbtSoundsSetting.setChecked(MainActivity.isSound);

        // For speech settings:
        CheckBox cbtSpeechSetting = (CheckBox) findViewById(R.id.cbtSpeechSetting);
        cbtSpeechSetting.setChecked(MainActivity.isSpeech);

        // For shake:
        CheckBox cbtOnshakeSetting = (CheckBox) findViewById(R.id.cbtOnshakeSetting);
        cbtOnshakeSetting.setChecked(MainActivity.isShake);

        // For keeping screen awake:
        CheckBox cbtScreenAwakeSetting = (CheckBox) findViewById(R.id.cbtScreenAwakeSetting);
        cbtScreenAwakeSetting.setChecked(MainActivity.isSpeech);

        // For IME DONE button of the keyboard:
        CheckBox cbtImeSetting = (CheckBox) findViewById(R.id.cbtImeSetting);
        cbtImeSetting.setChecked(MainActivity.isImeAction);
    } // end onCreate.

    // Let's see what happens when a check box is clicked in audio settings:
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        Settings set = new Settings(this); // to save changes.

        // Check which check box was clicked
        switch (view.getId()) {
            case R.id.cbtSoundsSetting:
                MainActivity.isSound = checked;
                set.saveBooleanSettings("isSound", MainActivity.isSound);
                break;
            case R.id.cbtSpeechSetting:
                MainActivity.isSpeech = checked;
                set.saveBooleanSettings("isSpeech", MainActivity.isSpeech);
                break;
            case R.id.cbtOnshakeSetting:
                MainActivity.isShake = checked;
                set.saveBooleanSettings("isShake", MainActivity.isShake);
                break;
            case R.id.cbtScreenAwakeSetting:
                MainActivity.isWakeLock = checked;
                set.saveBooleanSettings("isWakeLock", MainActivity.isWakeLock);
                break;
            case R.id.cbtImeSetting:
                MainActivity.isImeAction = checked;
                set.saveBooleanSettings("isImeAction", MainActivity.isImeAction);
                break;
        } // end switch.
    } // end onClick method.

} // end settings activity class.
