package fr.citizenfood.citizenfood.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by eliam on 14/03/2018.
 */

public class InternalStockage extends SQLiteOpenHelper {

    public static final String TABLE_VOTES = "votes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_VOTESTATE = "state";
    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_AUTHOR = "author";
    private static final String DATABASE_NAME = "votes.db";
    private static final int DATABASE_VERSION = 1;
    Context c;
    // CREATE TABLE foo5 ( id integer auto_increment primary key, vote ENUM("0", "1") NOT NULL default "0" );
    // Commande sql pour la création de la base de données
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_VOTES + " ( " + COLUMN_ID
            + " integer primary key autoincrement, "
            +COLUMN_UID
            +" varchar(255) NOT NULL, "
            +
            COLUMN_VOTESTATE
            + " TEXT CHECK( "+COLUMN_VOTESTATE+" IN ('0','1') ) NOT NULL DEFAULT '0', "+ COLUMN_AUTHOR +" varchar(255) NOT NULL);";


    public InternalStockage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        c = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
        Cursor cursor = sqLiteDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = 'votes'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                Toast.makeText(c, "DATABASE CREATED !", Toast.LENGTH_SHORT).show();
                //return true;
            }
            Toast.makeText(c, "DATABASE NOT CREATED !", Toast.LENGTH_SHORT).show();
            cursor.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(InternalStockage.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOTES);
        onCreate(db);
    }

    public long insertVotes(Votes votes ) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_VOTESTATE, votes.getVoteState());
        values.put(COLUMN_AUTHOR, votes.getVoteAuthor());
        values.put(COLUMN_UID, votes.getUid_vote());

        // insert row
        long id = db.insert(TABLE_VOTES, null, values);
        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public Votes getVotes( String author ) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getReadableDatabase();

        // insert row
        Cursor c = db.rawQuery("SELECT * FROM votes WHERE author = '"+author+"'",  null);
        c.moveToFirst();
        Votes v = null;
        if (c.getCount()> 0)
        {
            v = new Votes();
            v.setVoteAuthor(author);
            v.setUid_vote(c.getString(c.getColumnIndex("uid")));
            v.setVoteId(c.getInt(c.getColumnIndex("id")));
            v.setVoteState(c.getString(c.getColumnIndex("state")));
        }

        // close db connection
        db.close();

        // return votes instance
        return v;
    }
    public boolean deleteVotes( String uid ) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getReadableDatabase();
        // insert row
        return  db.delete(TABLE_VOTES, COLUMN_UID + "='" + uid+"'", null) > 0;

    }
}
