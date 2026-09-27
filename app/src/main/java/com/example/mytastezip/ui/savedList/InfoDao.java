package com.example.mytastezip.ui.savedList;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InfoDao {

    @Insert
    void insert(Info info); // 하나의 Info 객체를 삽입

    @Update
    void update(Info info); // Info 객체를 업데이트

    @Delete
    void delete(Info info); // Info 객체를 삭제

    @Query("SELECT * FROM Info")
    List<Info> getAll(); // Info 테이블의 모든 데이터를 조회

    @Query("SELECT * FROM Info WHERE id = :id")
    Info getInfoById(int id);

    // 위치별 조회
    @Query("SELECT * FROM Info WHERE location = :location")
    LiveData<List<Info>> getInfosByLocation(String location);

    @Query("SELECT * FROM Info")
    List<Info> getAllInfosSync();

    @Query("SELECT * FROM Info")
    LiveData<List<Info>> getAllInfos();

    // 이름으로 검색
    @Query("SELECT * FROM Info WHERE name LIKE '%' || :name || '%'")
    LiveData<List<Info>> searchInfosByName(String name);

    // 위도, 경도 범위로 조회 (지도에서 특정 영역의 마커들 가져오기)
    @Query("SELECT * FROM Info WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    List<Info> getInfosInBounds(double minLat, double maxLat, double minLng, double maxLng);

}
