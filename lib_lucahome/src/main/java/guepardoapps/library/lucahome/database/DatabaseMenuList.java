package guepardoapps.library.lucahome.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.toolset.common.classes.SerializableList;

public class DatabaseMenuList {

    private static final String TAG = DatabaseMenuList.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_WEEKDAY = "_weekday";
    private static final String KEY_DAY = "_day";
    private static final String KEY_MONTH = "_month";
    private static final String KEY_YEAR = "_year";
    private static final String KEY_TITLE = "_title";
    private static final String KEY_DESCRIPTION = "_description";

    private static final String DATABASE_NAME = "DatabaseMenuListDb";
    private static final String DATABASE_TABLE = "DatabaseMenuListTable";
    private static final int DATABASE_VERSION = 1;

    private DatabaseHelper _databaseHelper;
    private final Context _context;
    private SQLiteDatabase _database;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(" CREATE TABLE " + DATABASE_TABLE + " ( "
                    + KEY_ROW_ID + " TEXT NOT NULL, "
                    + KEY_WEEKDAY + " TEXT NOT NULL, "
                    + KEY_DAY + " TEXT NOT NULL, "
                    + KEY_MONTH + " TEXT NOT NULL, "
                    + KEY_YEAR + " TEXT NOT NULL, "
                    + KEY_TITLE + " TEXT NOT NULL, "
                    + KEY_DESCRIPTION + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseMenuList(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
    }

    public DatabaseMenuList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(MenuDto newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_WEEKDAY, newEntry.GetWeekday());
        contentValues.put(KEY_DAY, String.valueOf(newEntry.GetDay()));
        contentValues.put(KEY_MONTH, String.valueOf(newEntry.GetMonth()));
        contentValues.put(KEY_YEAR, String.valueOf(newEntry.GetYear()));
        contentValues.put(KEY_TITLE, newEntry.GetTitle());
        contentValues.put(KEY_DESCRIPTION, newEntry.GetDescription());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public SerializableList<MenuDto> GetMenuList() {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_WEEKDAY,
                KEY_DAY,
                KEY_MONTH,
                KEY_YEAR,
                KEY_TITLE,
                KEY_DESCRIPTION};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<MenuDto> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int weekdayIndex = cursor.getColumnIndex(KEY_WEEKDAY);
        int dayIndex = cursor.getColumnIndex(KEY_DAY);
        int monthIndex = cursor.getColumnIndex(KEY_MONTH);
        int yearIndex = cursor.getColumnIndex(KEY_YEAR);
        int titleIndex = cursor.getColumnIndex(KEY_TITLE);
        int descriptionIndex = cursor.getColumnIndex(KEY_DESCRIPTION);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String weekday = cursor.getString(weekdayIndex);
            String dayString = cursor.getString(dayIndex);
            String monthString = cursor.getString(monthIndex);
            String yearString = cursor.getString(yearIndex);
            String title = cursor.getString(titleIndex);
            String description = cursor.getString(descriptionIndex);

            int id = -1;
            try {
                id = Integer.parseInt(idString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            int day = -1;
            int month = -1;
            int year = -1;
            try {
                day = Integer.parseInt(dayString);
                month = Integer.parseInt(monthString);
                year = Integer.parseInt(yearString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            MenuDto entry = new MenuDto(id, weekday, day, month, year, title, description);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(MenuDto deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}