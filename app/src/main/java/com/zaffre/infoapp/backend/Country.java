package com.zaffre.infoapp.backend;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class Country implements Serializable {

    private String name;
    private String year;
    private String value;
    private LinkedHashMap<String,String> dataMap;

    /**
     * COuntry constructor
     * @param name country name
     */
    public Country(String name){
        this.name = name;
        dataMap = new LinkedHashMap<>();
    }

    /**
     * return the name of the country
     * @return country name
     */
    public String getName() {
        return name;
    }

    /**
     * set the name of the country
     * @param name name of country
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * to get the year of the value
     * @return year
     */
    public String getYear() {
        return year;
    }

    /**
     * to set the year of data.
     * @param year year
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * get the value of a country
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * set the value of specific year.
     * @param value value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * this to add years with their values.
     * @param year year
     * @param value value
     */
    public void CountryData(String year, String value){
        this.year = year;
        this.value = value;
        dataMap.put(year,value);
    }

    /**
     * to return map data for each country.
     * @return data map
     */
    public LinkedHashMap<String, String> getDataMap() {
        return dataMap;
    }
}
