package com.example.mytastezip.ui.savedList;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.Room;

import java.util.List;

public class InfoViewModel extends AndroidViewModel {

    private final LiveData<List<Info>> allInfos;
    private final InfoDao infoDao;

    public InfoViewModel(@NonNull Application application) {
        super(application);
        RoomDB db = Room.databaseBuilder(application, RoomDB.class, "my-taste-zip-db").build();
        infoDao = db.infoDao();
        allInfos = infoDao.getAllInfos();
    }

    public LiveData<List<Info>> getAllInfos() {
        return allInfos;
    }
}
