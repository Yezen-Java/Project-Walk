package com.zaffre.infoapp.frontend.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.zaffre.infoapp.R;
import com.zaffre.infoapp.backend.Country;
import com.zaffre.infoapp.backend.DataCaching;
import com.zaffre.infoapp.backend.JsonReader;
import com.zaffre.infoapp.frontend.helpers.CountryAdapter;
import com.zaffre.infoapp.frontend.helpers.CountryListElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements Animation.AnimationListener {

    ListView list;

    ArrayList<CountryListElement> countryListVisual;
    CountryAdapter countryAdapter;
    private JsonReader readerJson;
    private ArrayList<Country> interestRate;
    private ArrayList<Country> netMigran;
    private ArrayList<Country> moneyGrowth;
    private boolean connection;

    public DataCaching getDataCaching() {
        return dataCaching;
    }

    private DataCaching dataCaching = null;

    private Animation popupShow;
    private Animation popupHide;
    private LinearLayout popup;


    private CountryListElement currentCountry;
    private SeekBar dateBar;
    private int[] dates;
    private PieChart migrationChart;
    private BarChart investmentChart;
    private LineChart interestChart;

    private ImageView imageMap;
    private ImageView imagePin;
    private final Random rand = new Random();
    private LinkedHashMap<String, int[]> countryLocations;
    private int screenX;
    private float pinX = 0, pinY = 0;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //create data caching
        dataCaching = new DataCaching(this.getApplicationContext());
        //verify launch of app with internet check
        verifyAppLaunch();

        //Run thread to check if data get is done, without blocking the main thread
        Thread wait = new Thread() {
            public void run() {
                //Wait until data get tasks are done
                if(readerJson != null && !readerJson.isDone()) {
                    while(true) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(readerJson.isDone()) {
                            break;
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Set up all view elements
                        try {
                            initViewElements();
                            //show info popup
                            showInfoPopup();
                        }catch (Exception e){
                            System.out.println("actually here");
                            System.out.println(e);
                            createNetErrorDialog();
                        }
                    }
                });
            }
        };
        wait.start();
    }

    /**
     * Checks if there is an internet connection.
     * If a connection is found, sync data from server, otherwise load data from cache.
     * If this is the first launch, display error.
     */
    private void verifyAppLaunch(){
        checkInternetConnection();
        if(!connection && firstLaunch()) {
            System.out.println("here");
            createNetErrorDialog();
        }else if(connection) {
            syncDataFromServer();
            firstLaunch();
        } else {
            customToast();
        }
    }

    /**
     * This methods checks whether the current device is connected to the internet.
     */
    private void checkInternetConnection() {
        connection = false;
        //Connectivity Manager to get system Service
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //to Check whether the device is connected to wifi or mobile data.
        //connected to a network
        NetworkInfo actNetworkInfo = connectivityManager.getActiveNetworkInfo();
        System.out.println(actNetworkInfo);
        if (actNetworkInfo != null) {
            connection = true;
        }
    }

    /**
     * Syncs data from server and writes it to the cache
     */
    private void syncDataFromServer() {
        //progress dialog for the user.
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Syncing", "Obtaining latest data from World bank server");
        //access data from the internet.
        readerJson = new JsonReader();
        new Thread() {
            //This thread used to wait for the program to get the data.
            public void run() {
                //wait until data get is complete
                readerJson.waitUntilDone();
                //dismiss dialog
                progressDialog.dismiss();
                //sort the data
                readerJson.sortTopics();
                //cache data
                interestRate = readerJson.getInterestRateArray();
                moneyGrowth = readerJson.getMoneyGrowthArray();
                netMigran = readerJson.getNetMigrationAarray();
                dataCaching.saveDataToInternalStorage("interestRate", interestRate);
                dataCaching.saveDataToInternalStorage("moneyGrowth", moneyGrowth);
                dataCaching.saveDataToInternalStorage("netMigran", netMigran);
            }
        }.start();
    }

    /**
     * Checks if this launch is the first after installation
     * @return state of first launch
     */
    private boolean firstLaunch() {
        boolean firsTimeLaunch = getSharedPreferences("preferences", MODE_PRIVATE).getBoolean("firstLaunch", true);
        if (firsTimeLaunch) {
            getSharedPreferences("preferences", MODE_PRIVATE).edit().putBoolean("firstLaunch", false).commit();
            return true;
        }
        return false;
    }

    /**
     * Create dialog to warn user to connect to internet for data sync
     */
    private void createNetErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String unableToConnect = getResources().getString(R.string.unable_to_connect);
        builder.setMessage(unableToConnect)
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                startActivity(i);
                                finish();
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Shows popup with information about app
     */
    private void showInfoPopup() {
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        popup.startAnimation(popupShow);
                    }
                });
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        popup.startAnimation(popupHide);
                       try {
                           animatePin();
                       }catch (Exception e){
                           e.getMessage();
                       }
                    }
                });
            }
        }.start();
    }

    /**
     * Create view elements and lay them out
     */
    private void initViewElements() {
        //initialise map and pin
        imageMap = (ImageView) findViewById(R.id.mapView);
        imagePin = (ImageView) findViewById(R.id.pin);

        //initialise popup animations and view
        popupShow = AnimationUtils.loadAnimation(this, R.anim.show);
        popupShow.setAnimationListener(this);
        popupHide = AnimationUtils.loadAnimation(this, R.anim.hide);
        popupHide.setAnimationListener(this);

        popup = (LinearLayout) findViewById(R.id.popUp);
        popup.setVisibility(View.GONE);

        //create list panel and initialise contents
        initList();

        //initialise pin locations
        setLocationsAndScreenSize();

        //Create layout params for graphs
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        //Pie chart displaying migration data parameters
        migrationChart = new PieChart(getBaseContext());
        migrationChart.setHardwareAccelerationEnabled(true);
        migrationChart.setHoleColor(Color.parseColor("#1a8fcb"));
        migrationChart.setCenterText("Migration"); migrationChart.setCenterTextColor(Color.WHITE);
        migrationChart.setCenterTextSize(20f); migrationChart.setDescription("");
        migrationChart.setTransparentCircleAlpha(0);
        migrationChart.getLegend().setTextColor(Color.WHITE);
        FrameLayout panelOne = (FrameLayout) findViewById(R.id.panelOne);
        migrationChart.setLayoutParams(params);
        panelOne.addView(migrationChart);

        //Bar graph displaying foreign direct investment data parameters
        investmentChart = new BarChart(getBaseContext());
        investmentChart.setHardwareAccelerationEnabled(true);
        investmentChart.setTouchEnabled(true);
        investmentChart.setDescription("");
        investmentChart.setGridBackgroundColor(Color.parseColor("#1a8fcb"));
        investmentChart.getXAxis().setTextColor(Color.WHITE);
        investmentChart.getAxisLeft().setTextColor(Color.WHITE);
        investmentChart.getAxisRight().setTextColor(Color.WHITE);
        investmentChart.getAxisRight().setEnabled(false);
        investmentChart.getLegend().setTextColor(Color.WHITE);
        FrameLayout panelTwo = (FrameLayout) findViewById(R.id.panelTwo);
        investmentChart.setLayoutParams(params);
        panelTwo.addView(investmentChart);

        //Line graph displaying interest data parameters
        interestChart = new LineChart(getBaseContext());
        interestChart.setHardwareAccelerationEnabled(true);
        interestChart.setTouchEnabled(true);
        interestChart.setDescription("");
        interestChart.setGridBackgroundColor(Color.parseColor("#1a8fcb"));
        interestChart.getXAxis().setTextColor(Color.WHITE);
        interestChart.getAxisLeft().setTextColor(Color.WHITE);
        interestChart.getAxisRight().setTextColor(Color.WHITE);
        interestChart.getAxisRight().setEnabled(false);
        interestChart.getLegend().setTextColor(Color.WHITE);
        FrameLayout panelThree = (FrameLayout) findViewById(R.id.panelThree);
        interestChart.setLayoutParams(params);
        panelThree.addView(interestChart);

        //Date slider
        dateBar = (SeekBar) findViewById(R.id.dateBar);
        final TextView dateText = (TextView) findViewById(R.id.dateText);
        dates = getDates();
        int difference = dates[1] - dates[0];
        dateBar.setMax(difference);
        dateText.setText(getResources().getString(R.string.str_year) + " " + (dates[0] + dateBar.getProgress()));

        dateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int date = dates[0] + seekBar.getProgress();
                dateText.setText(getResources().getString(R.string.str_year) + " " + date);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Update graph data when date is changed
                displayData(false);
            }
        });

        //Get a random country, highlight it in the list, and display it's data, with a random date
        //currentCountry = countryListVisual.get(new Random().nextInt(countryListVisual.size()));
        //currentCountry.setHighlighted(true);
        //countryAdapter.notifyDataSetChanged();
        int position = new Random().nextInt(countryListVisual.size());
        list.performItemClick(list.getAdapter().getView(position, null, null),
                position,
                list.getAdapter().getItemId(position));
        //displayData(true);
    }

    /**
     * Gets the range of dates in the data
     * @return date range
     */
    private int[] getDates() {
        //Set min and max as extreme high and low cases
        int[] dates = new int[] {2100, 0};
        //Get data for dates
        List<Country> dataList = dataCaching.readFromInternalStorage("moneyGrowth");
        Country dummy = dataList.get(0);
        Set<String> yearsSet =  dummy.getDataMap().keySet();
        List<String> years = new ArrayList<>(yearsSet);
        //For each year, if it is less than the min date, set it as the min date
        //If it is greater than the max date, set it as the max date
        for(int i = 0; i < years.size(); i++) {
            if(Integer.parseInt(years.get(i)) < dates[0]) {
                dates[0] = Integer.parseInt(years.get(i));
            }
            if(Integer.parseInt(years.get(i)) > dates[1]) {
                dates[1] = Integer.parseInt(years.get(i));
            }
        }
        return dates;
    }

    /**
     * Creates the list view with all the countries
     */
    private void initList() {
        list = (ListView) findViewById(R.id.listCountries);
        TextView emptyText = (TextView) findViewById(android.R.id.empty);
        list.setEmptyView(emptyText);

        //Create the contents from a data set
        initListContents();

        //Create custom adapter to handle view of each element and filtering
        countryAdapter = new CountryAdapter(countryListVisual, this);
        list.setAdapter(countryAdapter);

        //Add click listener for the elements
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long id) {
                //Get selected country
                currentCountry = countryAdapter.getItem(position);

                //Set date to min date
                dateBar.setProgress(0);
                //Display data graphs
                displayData(false);

                //Set country as highlighted, adn unhighlight all other countries
                currentCountry.setHighlighted(true);
                for (int i = 0; i < countryAdapter.getList().size(); i++) {
                    CountryListElement element = countryAdapter.getItem(i);
                    if (!element.equals(currentCountry)) {
                        element.setHighlighted(false);
                    }
                }
                //Update list view
                countryAdapter.notifyDataSetChanged();
                //Update pin on map
                animatePin();
            }
        });

        registerForContextMenu(list);

        //Add filtering for names of coutnry
        list.setTextFilterEnabled(true);
        EditText editText = (EditText) findViewById(R.id.txtCountrySearch);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count < before) {
                    countryAdapter.resetData();
                }
                countryAdapter.getFilter().filter(s.toString());
                //Reset highlighting
                unhighlightAll();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Removes highlighting from all list elements and updates list view
     */
    private void unhighlightAll() {
        for (int i = 0; i < countryAdapter.getList().size(); i++) {
            CountryListElement element = countryAdapter.getItem(i);
            element.setHighlighted(false);
        }
        countryAdapter.notifyDataSetChanged();
    }

    /**
     * Initialise list contents from a data set
     */
    private void initListContents() {
        countryListVisual = new ArrayList<>();
        List<Country> dataList = dataCaching.readFromInternalStorage("interestRate");
        for (int i = 0; i < dataList.size(); i++) {
            countryListVisual.add(new CountryListElement(dataList.get(i).getName(), JsonReader.isoCodes[i]));
        }

        //Alphabetically sort list by country name
        Collections.sort(countryListVisual, new Comparator<CountryListElement>() {
            @Override
            public int compare(CountryListElement lhs, CountryListElement rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        countryAdapter.notifyDataSetChanged();
        return true;
    }

    /**
     * Updates graphs
     * @param randomDate - true = random date
     */
    private void displayData(final boolean randomDate) {
        Thread task = new Thread() {
            public void run() {
                String countryName = currentCountry.getName();
                int date = dates[0] + dateBar.getProgress();
                if(randomDate) { date = rand.nextInt((dates[1] - dates[0]) + 1) + dates[0]; dateBar.setProgress(date - dates[0]); }
                migration(countryName, date);
                investment(countryName, date);
                interest(countryName, date);

                //Animate application of data for 0.5s
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        migrationChart.animateXY(1000, 1000);
                        investmentChart.animateXY(1000, 1000);
                        interestChart.animateXY(1000, 1000);

                        migrationChart.notifyDataSetChanged();
                        investmentChart.notifyDataSetChanged();
                        interestChart.notifyDataSetChanged();

                        migrationChart.invalidate();
                        investmentChart.invalidate();
                        interestChart.invalidate();
                    }
                });
            }
        };
        task.start();
    }

    /**
     * Update migration pie chart
     * @param countryName - country to display data for
     * @param date - date to display data from
     */
    private void migration(String countryName, int date) {
        //Get country data from cache, and get data map from country
        ArrayList<Country> countriesData = dataCaching.readFromInternalStorage("netMigran");
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < countriesData.size(); i++) {
            if (countriesData.get(i).getName().equals(countryName)) {
                mapData = countriesData.get(i).getDataMap();
                break;
            }
        }
        //If no data found, and exit method
        if(mapData == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(migrationChart.getData() != null) {
                        migrationChart.clearValues();
                    }
                }
            });
            return;
        }

        //Set first year as date given
        int year = date;
        ArrayList<Entry> yVals = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        final ArrayList<Integer> colours = new ArrayList<>();
        int index = 0;
        //Loop until the first date has data
        while(true) {
            if(year > dates[1]) {
                return;
            }
            if(mapData.get(String.valueOf(year)) == null) {
                year++;
            } else {
                break;
            }
        }
        //For every 5 years, add the data to the dataset and add a random colour
        for(int i = year; i <= dates[1]; i += 5) {
            String data = mapData.get(String.valueOf(i));
            if(data != null && !data.equals("")) {
                yVals.add(new Entry(Float.valueOf(data), index));
            } else {
                yVals.add(new Entry(0, index));
            }
            xVals.add(String.valueOf(i));
            float[] hsv = new float[] {rand.nextInt(255), 255 - rand.nextInt(75), 255 - rand.nextInt(75)};
            colours.add(Color.HSVToColor(hsv));
            index++;
        }

        //Set the data to display to the data set
        final PieDataSet migrationDataSet = new PieDataSet(yVals, "Flow of migrants in " + countryName + ".");
        final PieData migrationData = new PieData(xVals, migrationDataSet);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                migrationDataSet.setColors(colours);
                migrationDataSet.setSliceSpace(0f);
                migrationChart.setData(migrationData);
                migrationChart.getLegend().setWordWrapEnabled(true);
            }
        };
        executor.submit(task);
    }

    /**
     * Update mFDI bar graph
     * @param countryName - country to display data for
     * @param date - date to display data from
     */
    private void investment(String countryName, int date) {
        //Get country data from cache, and get data map from country
        ArrayList<Country> countriesData = dataCaching.readFromInternalStorage("moneyGrowth");
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < countriesData.size(); i++) {
            if (countriesData.get(i).getName().contains(countryName)) {
                mapData = countriesData.get(i).getDataMap();
            }
        }
        //If no data found, and exit method
        if(mapData == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(investmentChart.getData() != null) {
                        investmentChart.clearValues();
                    }
                }
            });
            return;
        }

        ArrayList<BarEntry> yVals = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        int index = 0;
        //For each date, get the data nd add it to the dataset. If there is no data, an a zero value
        for(int i = date; i <= dates[1]; i++) {
            String data = mapData.get(String.valueOf(i));
            if(data != null && !data.equals("")) {
                yVals.add(new BarEntry(Float.valueOf(data), index));
            } else {
                yVals.add(new BarEntry(0, index));
            }
            xVals.add(String.valueOf(i));
            index++;
        }

        //Set the data to display to the data set
        final BarDataSet investmentDataSet = new BarDataSet(yVals, "Average annual growth rate in money statistics in " + countryName + ".");
        final BarData investmentData = new BarData(xVals, investmentDataSet);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                float[] hsv = new float[]{rand.nextInt(255), 255 - rand.nextInt(75), 255 - rand.nextInt(75)};
                investmentDataSet.setColor(Color.HSVToColor(hsv));
                investmentDataSet.setValueTextColor(Color.WHITE);
                investmentChart.setData(investmentData);
                investmentChart.fitScreen();
                investmentChart.getLegend().setWordWrapEnabled(true);
            }
        };
        executor.submit(task);
    }

    /**
     * Update interest line graph
     * @param countryName - country to display data for
     * @param date - date to display data from
     */
    private void interest(String countryName, int date) {
        //Get country data from cache, and get data map from country
        ArrayList<Country> countriesData = dataCaching.readFromInternalStorage("interestRate");
        LinkedHashMap<String, String> mapData = null;
        for (int i = 0; i < countriesData.size(); i++) {
            if (countriesData.get(i).getName().equals(countryName)) {
                mapData = countriesData.get(i).getDataMap();
            }
        }
        //If no data found, and exit method
        if(mapData == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(interestChart.getData() != null) {
                        interestChart.clearValues();
                    }
                }
            });
            return;
        }

        ArrayList<Entry> yVals = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        int index = 0;
        //For each date, get the data nd add it to the dataset. If there is no data, an a zero value
        for(int i = date; i <= dates[1]; i++) {
            String data = mapData.get(String.valueOf(i));
            if(data != null && !data.equals("")) {
                yVals.add(new Entry(Float.valueOf(data), index));
            }
            xVals.add(String.valueOf(i));
            index++;
        }

        //Set the data to display to the data set
        final LineDataSet interestDataSet = new LineDataSet(yVals, "Interest rate for" + countryName + ".");
        final LineData interestData = new LineData(xVals, interestDataSet);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                float[] hsv = new float[]{rand.nextInt(255), 255 - rand.nextInt(75), 255 - rand.nextInt(75)};
                interestDataSet.setColor(Color.HSVToColor(hsv));
                interestDataSet.setValueTextColor(Color.WHITE);
                interestChart.setData(interestData);
                interestChart.fitScreen();
                interestChart.getLegend().setWordWrapEnabled(true);
            }
        };
        executor.submit(task);
    }

    /**
     * Display message that no connection was found, and data is being laoded from cache
     */
    private void customToast() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.custom_toast_cache, null);
        TextView text = (TextView) layout.findViewById(R.id.textViewToast);
        String noConnection = getResources().getString(R.string.no_connection_loading_cache);
        text.setText(noConnection);
        Toast toast = new Toast(this.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    @Override
    public void onAnimationStart(Animation animation) {
        if (animation.equals(popupShow)) {
            popup.setVisibility(View.VISIBLE);
        } else if (animation.equals(popupHide)) {
            popup.setVisibility(View.GONE);
        }
    }
    @Override
    public void onAnimationEnd(Animation animation) { }
    @Override
    public void onAnimationRepeat(Animation animation) { }

    /**
     * Set new position of pin on map
     */
    private void animatePin() {
        imagePin.setVisibility(View.VISIBLE);
        int[] countryLocation = countryLocations.get(currentCountry.getName());

        float[] mapSize = new float[2];

        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageMap.getImageMatrix().getValues(f);
        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];
        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageMap.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        mapSize[0] = Math.round(origW * scaleX);
        mapSize[1] = Math.round(origH * scaleY);

        final float newX = ((screenX - imageMap.getWidth() / 2 - mapSize[0] / 2) + ((mapSize[0] / 99) * countryLocation[0])) - 32;
        final float newY = ((imageMap.getHeight()/2 - mapSize[1]/2) + ((mapSize[1] / 99) * countryLocation[1])) - 64;
        System.out.println(newX + ", " + newY);

        TranslateAnimation transAnim = new TranslateAnimation(pinX, newX, pinY, newY);
        transAnim.setDuration(600);
        transAnim.restrictDuration(600);
        transAnim.setFillEnabled(true);
        transAnim.setFillAfter(true);
        imagePin.setVisibility(View.VISIBLE);
        imagePin.startAnimation(transAnim);
        transAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                pinX = newX;
                pinY = newY;
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void setLocationsAndScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenX = point.x;

        countryLocations = new LinkedHashMap<>();
        countryLocations.put("Algeria", new int[]{48, 48});
        countryLocations.put("Argentina", new int[]{27, 85});
        countryLocations.put("Australia", new int[]{85, 78});
        countryLocations.put("Brazil", new int[]{33, 70});
        countryLocations.put("Cameroon", new int[]{50, 61});
        countryLocations.put("Canada", new int[]{17, 28});
        countryLocations.put("Central African Republic", new int[]{52, 60});
        countryLocations.put("China", new int[]{75, 48});
        countryLocations.put("Colombia", new int[]{26, 63});
        countryLocations.put("Egypt, Arab Rep.", new int[]{54, 49});
        countryLocations.put("France", new int[]{47, 37});
        countryLocations.put("Germany", new int[]{48, 34});
        countryLocations.put("Greece", new int[]{53, 43});
        countryLocations.put("India", new int[]{68, 54});
        countryLocations.put("Italy", new int[]{51, 40});
        countryLocations.put("Jamaica", new int[]{27, 55});
        countryLocations.put("Japan", new int[]{85, 43});
        countryLocations.put("Mexico", new int[]{19, 54});
        countryLocations.put("Nigeria", new int[]{48, 60});
        countryLocations.put("Peru", new int[]{25, 67});
        countryLocations.put("Russian Federation", new int[]{72, 25});
        countryLocations.put("South Africa", new int[]{53, 80});
        countryLocations.put("Swaziland", new int[]{55, 79});
        countryLocations.put("Sweden", new int[]{51, 24});
        countryLocations.put("Turkey", new int[]{56, 41});
        countryLocations.put("United Arab Emirates", new int[]{62, 51});
        countryLocations.put("United Kingdom", new int[]{46, 31});
        countryLocations.put("United States", new int[]{18, 41});
        countryLocations.put("Uruguay", new int[]{31, 84});
    }
}
