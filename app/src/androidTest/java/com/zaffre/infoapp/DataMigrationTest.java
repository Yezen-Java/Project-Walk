package com.zaffre.infoapp;

import android.test.ActivityInstrumentationTestCase2;

import com.zaffre.infoapp.backend.Country;
import com.zaffre.infoapp.backend.DataCaching;
import com.zaffre.infoapp.frontend.activities.MainActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DataMigrationTest extends ActivityInstrumentationTestCase2<MainActivity> {
    ArrayList<Country> dataList;
    public DataMigrationTest() {
        super(MainActivity.class);
    }

    public void setup(){
        MainActivity activity = getActivity();
        DataCaching dataCaching = activity.getDataCaching();
        dataList = dataCaching.readFromInternalStorage("netMigran");
    }
    // if tests fail (java.lang.NullPointerException: Attempt to invoke virtual method 'int java.util.ArrayList.size()') then restart the test that failed on its own
    // because of connection
    public void testnetMigrationListsize(){
        setup();
        int size = dataList.size();
        assertEquals(29, size);
    }


    public void testNetMigrationDataFrance(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("France")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("2012");
        assertEquals(yearvalue,"649998");
    }

    public void testNetMigrationDataUK(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("United Kingdom")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("2007");
        assertEquals(yearvalue,"840371");
    }

    public void testNetMigrationDataMexico(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("Mexico")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("2002");
        assertEquals(yearvalue,"-2929461");
    }

    public void testNetMigrationDataUS(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("United States")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("1982");
        assertEquals(yearvalue,"3636060");
    }

    public void testNetMigrationDataGermany(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("Germany")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("1977");
        assertEquals(yearvalue,"200002");
    }
}
