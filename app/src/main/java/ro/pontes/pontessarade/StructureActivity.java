package ro.pontes.pontessarade;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class StructureActivity extends Activity implements
        OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structure);

        // For database:
        // Start things for our database:
        TestAdapter mDbHelper = new TestAdapter(this);
        mDbHelper.createDatabase();
        mDbHelper.open();

        // Get the total number of SARADE:
        String sql = "SELECT count(_id) FROM sarade";
        Cursor cursor = mDbHelper.getTestData(sql);
        String totalSarade = cursor.getString(0);

        // Get the STRUCTURAS:
        sql = "SELECT structura, count(structura) FROM sarade GROUP BY structura ORDER BY structura";
        cursor = mDbHelper.getTestData(sql);

        // Create a delimited string:
        StringBuilder sb = new StringBuilder("Toate - " + totalSarade);

        cursor.moveToFirst();
        do {
            sb.append("|");
            sb.append(cursor.getString(0));
            sb.append(" - ");
            sb.append(cursor.getString(1));
        } while (cursor.moveToNext());
        // end do ... while.

        cursor.close();

        Spinner dropdown = findViewById(R.id.spinner1);
        String[] items = sb.toString().split("\\|");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
    } // end onCreate.

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {

        String chosen = parent.getItemAtPosition(position).toString();

        String[] aChosen = chosen.split(" - ");
        if (aChosen[0].equals("Toate")) {
            MainActivity.chosenStructura = "0+0";
        } else {
            MainActivity.chosenStructura = aChosen[0];
            // GUITools.alert(this, "Hello!", MainActivity.chosenStructura);
        }

        // Show in a TextView about this choose:
        LinearLayout ll = findViewById(R.id.llStructure2);
        ll.removeAllViews();
        String temp;
        if (MainActivity.chosenStructura.equals("0+0")) {
            temp = "toate";
        } else {
            temp = MainActivity.chosenStructura;
        }
        TextView tv = new TextView(this);
        String message = String.format(getString(R.string.chosen_structure_is),
                temp);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
        tv.setText(message);
        ll.addView(tv);

    } // end implemented method for chosen item.

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub

    }

} // end settings structure class.

