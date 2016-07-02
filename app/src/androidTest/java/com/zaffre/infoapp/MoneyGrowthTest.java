package com.zaffre.infoapp;

import android.test.ActivityInstrumentationTestCase2;

import com.zaffre.infoapp.backend.Country;
import com.zaffre.infoapp.backend.DataCaching;
import com.zaffre.infoapp.frontend.activities.MainActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MoneyGrowthTest extends ActivityInstrumentationTestCase2<MainActivity> {
    ArrayList<Country> dataList;
    public MoneyGrowthTest() {
        super(MainActivity.class);
    }

    public void setup(){
        MainActivity activity = getActivity();
        DataCaching dataCaching = activity.getDataCaching();
        dataList = dataCaching.readFromInternalStorage("moneyGrowth");
    }
    // if tests fail (java.lang.NullPointerException: Attempt to invoke virtual method 'int java.util.ArrayList.size()') then restart the test that failed on its own
    // because of connection
    public void testMoneyGrowthListsize(){
        setup();
        int size = dataList.size();
        assertEquals(28, size);
    }


    public void testMoneyGrowthDataFrance(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("France")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("2014");
        assertEquals(yearvalue,"3.19870637380407");
    }

    public void testMoneyGrowthRateDataUK(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("United Kingdom")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("2014");
        assertEquals(yearvalue,"-2.53439886754885");
    }

    public void testMoneyGrowthRateDataMexico(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("Mexico")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("2006");
        assertEquals(yearvalue,"6.69259611037929");
    }

    public void testMoneyGrowthRateDataUS(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("United States")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("1966");
        assertEquals(yearvalue,"4.71990148792604");
    }

    public void testMoneyGrowthRateDataGermany(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("Germany")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("2002");
        assertEquals(yearvalue,"5.90106518209575");
    }
}
