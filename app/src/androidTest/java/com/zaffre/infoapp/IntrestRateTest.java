package com.zaffre.infoapp;

import android.test.ActivityInstrumentationTestCase2;

import com.zaffre.infoapp.backend.Country;
import com.zaffre.infoapp.backend.DataCaching;
import com.zaffre.infoapp.frontend.activities.MainActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class IntrestRateTest extends ActivityInstrumentationTestCase2<MainActivity> {
    ArrayList<Country> dataList;
    public IntrestRateTest() {
        super(MainActivity.class);
    }

    public void setup(){
        MainActivity activity = getActivity();
        DataCaching dataCaching = activity.getDataCaching();
        dataList = dataCaching.readFromInternalStorage("interestRate");
    }
    // if tests fail (java.lang.NullPointerException: Attempt to invoke virtual method 'int java.util.ArrayList.size()') then restart the test that failed on its own
    // because of connection
    public void testintrestRateListsize(){
        setup();
        int size = dataList.size();
        assertEquals(29, size);
    }


    public void testIntrestRateDataFrance(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("France")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("2004");
        assertEquals(yearvalue,"4.87421537697982");
    }

    public void testIntrestRateDataUK(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("United Kingdom")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("2014");
        assertEquals(yearvalue,"-1.24327393017421");
    }

    public void testIntrestRateDataMexico(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("Mexico")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("2012");
        assertEquals(yearvalue,"1.41604861772931");
    }

    public void testIntrestRateDataUS(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("United States")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("2003");
        assertEquals(yearvalue,"2.08683069605981");
    }

    public void testIntrestRateDataGermany(){
        setup();
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getName().contains("Germany")) {
                mapData = dataList.get(i).getDataMap();
            }
        }
        String yearvalue = mapData.get("1981");
        assertEquals(yearvalue,"10.0893373009308");
    }
}
