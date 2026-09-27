package com.example.mytastezip.ui.savedList;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Info {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "location")
    public String location;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "link")
    public String link;

    @ColumnInfo(name = "memo")
    public String memo;

    @ColumnInfo(name = "longitude") //경도
    public double longitude;

    @ColumnInfo(name = "latitude")  //위도
    public double latitude;

    // 생성자
    public Info() {}

    public Info(String location, String category, String name, String link, String memo, double longitude, double latitude) {
        this.location = location;
        this.category = category;
        this.name = name;
        this.link = link;
        this.memo = memo;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // Getter 메서드
    public int getId() { return id; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public String getName() { return name; }
    public String getLink() { return link; }
    public String getMemo() { return memo; }
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }

    // Setter 메서드
    public void setId(int id) { this.id = id; }
    public void setLocation(String location) { this.location = location; }
    public void setCategory(String category) { this.category = category; }
    public void setName(String name) { this.name = name; }
    public void setLink(String link) { this.link = link; }
    public void setMemo(String memo) { this.memo = memo; }

}
