package com.zaffre.infoapp.backend;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class JsonReader {

    private ArrayList<Country> countryArrayList = new ArrayList<>();
    private ArrayList<Country> netMigration = new ArrayList<>();
    private ArrayList<Country> interestRate = new ArrayList<>();
    private ArrayList<Country> moneyGrowth = new ArrayList<>();

    private ArrayList<ApiRequest> tasks;
    private boolean done;

    public static String[] isoCodes = {"AU", "BR", "UY", "DZ", "CM", "CA", "CF", "GB", "FR", "IT", "SWZ", "SWE", "EG", "ZA", "NG", "AR",
            "CO", "JM", "MX", "US", "CN", "IN", "JP", "RU", "TR", "AE", "DE", "GR", "PE"};

    public JsonReader() {
        done = false;
        tasks = new ArrayList<>();
        getDataMg();
        getDataInterestRate();
        getDataMoneyGrowth();
    }

    /**
     * Get migration data
     */
    public void getDataMg() {
        // Here I have picked some countries to test
        for (String isoCode : isoCodes) {
            //This construct separate link for each country
            ApiRequest task = new ApiRequest();
            task.execute("http://api.worldbank.org/countries/" + isoCode + "/indicators/SM.POP.NETM?per_page=13888&date=1960:2015&format=json");
            tasks.add(task);
        }
    }

    /**
     * Get interest data
     */
    public void getDataInterestRate() {
        for (String isoCode : isoCodes) {
            //This construct separate link for each country
            ApiRequest task = new ApiRequest();
            task.execute("http://api.worldbank.org/countries/"+isoCode+"/indicators/FR.INR.RINR?per_page=13888&date=1960:2015&format=json");
            tasks.add(task);
        }
    }

    /**
     * get FDI data
     */
    public void getDataMoneyGrowth() {
        for (String isoCode : isoCodes) {
            //This construct separate link for each country
            ApiRequest task = new ApiRequest();
            task.execute("http://api.worldbank.org/countries/" + isoCode + "/indicators/FM.LBL.MQMY.ZG?per_page=13888&date=1960:2015&format=json");
            tasks.add(task);
        }
    }

    /**
     * Create async task for each data get
     */
    private class ApiRequest extends AsyncTask<String, Double, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... params) {
            // check input
            if (params.length != 1) return null;
            try {
                // open connection
                URL url = new URL(params[0]);
                InputStream inStream = url.openStream();
                DataInputStream dataInStream = new DataInputStream(inStream);

                // buffer to hold chunks as they are read
                byte[] buffer = new byte[1024];
                int bufferLength;

                // string builder to hold output
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                // read from stream
                while ((bufferLength = dataInStream.read(buffer)) > 0) {
                    output.write(buffer, 0, bufferLength);
                }

                // convert to JSON array
                return new JSONArray(output.toString("UTF-8"));
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            if (result == null) return;
            try {
                //This to set up JsonArray
                JSONArray countryList = result.getJSONArray(1);
                JSONObject country;
                country = countryList.getJSONObject(1);
                //System.out.println(checkTopic);
                JSONObject countryCollection = (JSONObject) country.get("country");
                //System.out.println(countryCollection.get("value"));

                //This creates a Country object with the fetched country name.
                Country countryObject = new Country(countryCollection.get("value").toString());
                //This to iterate through the Json webpage format.
                for (int i = 0; i < countryList.length(); ++i) {
                    country = countryList.getJSONObject(i);
                    //System.out.println(country.getString("value"));
                    //System.out.println(country.getString("date"));
                    //  i += 4;
                    String checkValue = country.getString("value");
                    if (!checkValue.equals("null")) {
                        countryObject.CountryData(country.getString("date"), country.getString("value"));
                    }
                }

                countryArrayList.add(countryObject);

                //This adds the country object to an array to be used for the frontend graphics.
            } catch (Exception e) {
                //System.out.println("Something went wrong");
            }
        }
    }

    /**
     * waits until all data get tasks are complete
     */
    public void waitUntilDone() {
        for(ApiRequest task : tasks) {
            try {
                System.out.println("Waiting for " + task.toString());
                task.get();
            } catch (Exception e) {
                Log.e("Failed to get data", e.getMessage());
            }
        }
        done = true;
    }

    /**
     * returns done state
     * @return done state
     */
    public boolean isDone() {return done;}

    public ArrayList<Country> getCountryArrayList() {
        return countryArrayList;
    }

    /**
     * return NetMigrationArray
     * @return migration
     */
    public ArrayList<Country> getNetMigrationAarray() {
        return netMigration;
    }

    /**
     *return Interest rate data
     * @return interest
     */
    public ArrayList<Country> getInterestRateArray() {
        return interestRate;
    }

    /**
     * return moneyGrowthArray
     * @return money growth
     */

    public ArrayList<Country> getMoneyGrowthArray() {
        return moneyGrowth;
    }

    /**
     * This method sorts the data array into topics
     */
    public void sortTopics() {
        try {
            for (int i = 0; i < isoCodes.length; i++) {
                netMigration.add(countryArrayList.get(i));
            }
            System.out.println("topic 1= " + netMigration.size());

            for (int i = netMigration.size(); i < 58; i++) {
                interestRate.add(countryArrayList.get(i));
            }
            System.out.println("topic 2= " + interestRate.size());

            for (int i = 58; i < countryArrayList.size(); i++) {
                moneyGrowth.add(countryArrayList.get(i));
            }
            System.out.println("topic 3= " + moneyGrowth.size());
        } catch (Exception e){
            Log.e("No internet, no sync",e.getMessage());
        }
    }
}





