package com.example.mytastezip.ui.savedList;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mytastezip.ui.community.Post;
import com.example.mytastezip.ui.community.PostDao;

//Info Entity변경시 version 1씩 올려주기
@Database(entities = {Post.class, Info.class}, version = 5, exportSchema = false)
public abstract class RoomDB extends RoomDatabase
{
    private static RoomDB database;
    private static String DATABASE_NAME = "database";

    public synchronized static RoomDB getInstance(Context context)
    {
        if (database == null)
        {
            database = Room.databaseBuilder(context.getApplicationContext(), RoomDB.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public abstract InfoDao infoDao();
    public abstract PostDao postDao();
}