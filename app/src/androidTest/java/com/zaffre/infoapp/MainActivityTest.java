package com.zaffre.infoapp;

import android.test.ActivityInstrumentationTestCase2;

import com.zaffre.infoapp.backend.Country;
import com.zaffre.infoapp.backend.DataCaching;
import com.zaffre.infoapp.frontend.activities.MainActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    ArrayList<Country> dataList;
    public MainActivityTest() {
        super(MainActivity.class);
    }

    public void testActivityExists() {
        MainActivity activity = getActivity();
        assertNotNull(activity);
    }
    // if test fails restart (it failed due to connection)
    public void testCache(){
        MainActivity activity = getActivity();
        DataCaching dataCaching = activity.getDataCaching();
        dataList = dataCaching.readFromInternalStorage("interestRate");
        assertNotNull(dataList);
    }
}
