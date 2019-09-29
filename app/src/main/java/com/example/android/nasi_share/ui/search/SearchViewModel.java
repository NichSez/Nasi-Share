package com.example.android.nasi_share.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SearchViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SearchViewModel() {
        mText = new MutableLiveData<>();

    }

    public LiveData<String> getText() {
        return mText;
    }
}