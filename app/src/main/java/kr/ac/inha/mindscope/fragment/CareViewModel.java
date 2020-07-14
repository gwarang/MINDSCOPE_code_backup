package kr.ac.inha.mindscope.fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CareViewModel extends ViewModel {


    private MutableLiveData<String> mText;

    public CareViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is care fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}