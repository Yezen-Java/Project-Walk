package com.zaffre.infoapp;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.zaffre.infoapp.backend.Country;

import java.util.Map;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class CountryTest extends ApplicationTestCase<Application> {
    public CountryTest() {
        super(Application.class);
    }

    public void testGetNameMethod(){
        Country country = new Country("UK");
        String name = country.getName();
        assertEquals("UK",name);
    }
    public void testSetNameMethod(){
        Country country = new Country("UK");
        country.setName("Germany");
        String name = country.getName();
        assertEquals("Germany",name);
    }

    public void testSetandGetYearMethods(){
        Country country = new Country("UK");
        country.setYear("1995");
        String year = country.getYear();
        assertEquals("1995",year);
    }

    public void testSetandGetValueMethods(){
        Country country = new Country("UK");
        country.setValue("55");
        String value = country.getValue();
        assertEquals("55",value);
    }

    public void testGetdataMapMethod(){
        Country country = new Country("UK");
        country.CountryData("1995","55");
        Map<String, String> data = country.getDataMap();
        assertEquals("55", data.get("1995"));
    }
}