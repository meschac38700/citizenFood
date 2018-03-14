package fr.citizenfood.citizenfood.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by eliam on 14/03/2018.
 */

public class InternalStockage extends SQLiteOpenHelper {

    public static final String TABLE_VOTES = "votes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_VOTE = "vote";

    private static final String DATABASE_NAME = "votes.db";
    private static final int DATABASE_VERSION = 1;

    // CREATE TABLE foo5 ( id integer auto_increment primary key, vote ENUM("0", "1") NOT NULL default "0" );
    // Commande sql pour la création de la base de données
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_VOTES + " ( " + COLUMN_ID
            + " integer primary key auto_increment, " + COLUMN_VOTE
            + " enum('0','1') default '0' NOT NULL);";


    public InternalStockage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(InternalStockage.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOTES);
        onCreate(db);
    }
}
