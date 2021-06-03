package com.app.videostream.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.app.videostream.Model.Topic;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class TopicHelper extends SQLiteOpenHelper {

    public Context context;
    public static String DATABASE_NAME="Topic.db";
    public static String TABLE_NAME="Topic";
    public String ID="ID";
    public String TITLE="TITLE";
    public String DESCRIPTION="DESCRIPTION";
    public String DATE="DATE";


    public TopicHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_NAME+" (ID TEXT, TITLE TEXT, DESCRIPTION TEXT, DATE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void insert(Topic topic){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(ID,topic.getId());
            values.put(TITLE, topic.getTitle());
            values.put(DESCRIPTION, topic.getDescription());
            values.put(DATE, topic.getDate());
            db.insert(TABLE_NAME, null, values);
            Log.d("TAGER", "Inserted to database");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<Topic> getAllData(){
        List<Topic> topicList= new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        Cursor cursor=db.rawQuery("SELECT * FROM "+TABLE_NAME, null);
        while (cursor.moveToNext()) {
            Topic topic = new Topic(cursor.getString(0),"",cursor.getString(1),cursor.getString(2),cursor.getString(3),"","");
            topicList.add(topic);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        cursor.close();
        db.close();
        return topicList;
    }

    public List<String> getAllIds(){
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> strings=new ArrayList<>();
        db.beginTransaction();
        Cursor cursor=db.rawQuery("SELECT * FROM "+TABLE_NAME, null);
        while (cursor.moveToNext()) {
            Topic topic = new Topic(cursor.getString(0),"",cursor.getString(1),cursor.getString(2),cursor.getString(3),"","");
            strings.add(topic.getId());
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        cursor.close();
        db.close();
        return strings;
    }

    public boolean delete(Topic topic){
        Log.d("TAGER", "Delete called");
        SQLiteDatabase db = this.getWritableDatabase();
        int i=db.delete(TABLE_NAME, ID+"=?", new String[]{topic.getId()});
        if(i==1){
            Log.d("TAGGER","i value1 return true deleted");
            return true;
        }
        else {
            Log.d("TAGER", "Not deleted");
            return false;
        }
    }


}
