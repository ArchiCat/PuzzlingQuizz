package com.example.android.puzzlingquiz.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.android.puzzlingquiz.quizz_data.QuestionData;

import static android.R.attr.id;
import static android.icu.text.MessagePattern.ArgType.SELECT;

/**
 * Created by Zsuzsanna Szebeni
 */

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DB_LOCATION = "data/data/com.example.android.puzzlingquiz/databases/";
    public static final String DB_NAME = "puzzling_quiz.db";

    private static final String LOG_TAG = "Quiz_DBHelper";

    private Context ctx;
    private SQLiteDatabase db;

    public DataBaseHelper (Context context) {
        super(context, DB_NAME, null, 1);
        this.ctx = context;

    }

    @Override
    public void onCreate (SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * opening databasse
     */
    public void openDatabase() {
        String dbPath = ctx.getDatabasePath(DB_NAME).getPath();
        if (db != null && db.isOpen()) {
            return;
        }
        db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    /**
     * closing database
     */
    public void closeDatabase() {
        if (db != null) {
            db.close();
        }
    }

    /**
     * Gets the question based on its id
     *
     * @param id - the id of the question
     * @return QuestionData object or null;
     */
    public QuestionData getQuestionFromDatabaseById(int id) {
        openDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM questions WHERE _id = " + id, null);

            cursor.moveToFirst();

            QuestionData questionData = new QuestionData(cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(7));
            cursor.close();
            closeDatabase();
            return questionData;
        } catch ( Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Gets the question count on its id
     *
     * @return the number of records
     */
    public int getCountOfQuestions() {
        openDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM questions", null);

            return cursor.getCount();
        } catch ( Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}


