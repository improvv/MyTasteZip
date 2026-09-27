package com.example.mytastezip.ui.community;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "posts")
public class Post {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "timestamp")
    public long timestamp;
}

