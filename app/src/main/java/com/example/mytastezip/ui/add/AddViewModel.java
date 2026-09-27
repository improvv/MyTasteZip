package com.example.mytastezip.ui.add;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddViewModel extends ViewModel {
    private final MutableLiveData<AddFragment.PinData> _pinData = new MutableLiveData<>();
    public LiveData<AddFragment.PinData> pinData = _pinData;

    public void addPinData(AddFragment.PinData data) {
        _pinData.setValue(data);
    }
}