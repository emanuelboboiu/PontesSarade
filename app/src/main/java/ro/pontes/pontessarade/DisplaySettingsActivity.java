package ro.pontes.pontessarade;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

public class DisplaySettingsActivity extends Activity {

    private Settings set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_settings);

        set = new Settings(this);

        // Check the radio button depending of the size already chosen:
        String rb = "rbRadio" + MainActivity.textSize;
        int resID = getResources().getIdentifier(rb, "id", getPackageName());
        RadioButton radioButton = (RadioButton) findViewById(resID);
        radioButton.setChecked(true);

    } // end onCreate method.

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked:
        int id = view.getId();

        if (id == R.id.rbRadio14 && checked) {
            MainActivity.textSize = 14;
        } else if (id == R.id.rbRadio16 && checked) {
            MainActivity.textSize = 16;
        } else if (id == R.id.rbRadio18 && checked) {
            MainActivity.textSize = 18;
        } else if (id == R.id.rbRadio20 && checked) {
            MainActivity.textSize = 20;
        } else if (id == R.id.rbRadio22 && checked) {
            MainActivity.textSize = 22;
        } else if (id == R.id.rbRadio24 && checked) {
            MainActivity.textSize = 24;
        } else if (id == R.id.rbRadio26 && checked) {
            MainActivity.textSize = 26;
        } else if (id == R.id.rbRadio28 && checked) {
            MainActivity.textSize = 28;
        } else if (id == R.id.rbRadio30 && checked) {
            MainActivity.textSize = 30;
        } else if (id == R.id.rbRadio32 && checked) {
            MainActivity.textSize = 32;
        } else if (id == R.id.rbRadio34 && checked) {
            MainActivity.textSize = 34;
        } else if (id == R.id.rbRadio36 && checked) {
            MainActivity.textSize = 36;
        }
// // end what radio was clicked.

        // Save now the setting:
        set.saveIntSettings("textSize", MainActivity.textSize);
    } // end onRadioButtonClicked.

} // end DisplaySettingsClass.
