package ro.pontes.pontessarade;

import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TestAdapter {

    // protected static final String TAG = "DataAdapter";

    private SQLiteDatabase mDb;
    private final DataBaseHelper mDbHelper;

    public TestAdapter(Context context) {
        mDbHelper = new DataBaseHelper(context);
    }

    public void createDatabase() throws SQLException {
        try {
            mDbHelper.createDataBase();
        } catch (IOException mIOException) {
            // Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
    }

    public void open() throws SQLException {
        mDbHelper.openDataBase();
        mDbHelper.close();
        mDb = mDbHelper.getReadableDatabase();
    }

    public void close() {
        mDbHelper.close();
    }

    public Cursor getTestData(String sql) {
        Cursor mCur = mDb.rawQuery(sql, null);
        if (mCur != null) {
            mCur.moveToNext();
        }
        return mCur;
    }

} // end class TestAdapter.
