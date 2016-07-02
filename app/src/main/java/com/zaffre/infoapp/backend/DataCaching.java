package com.zaffre.infoapp.backend;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class DataCaching {
    private Context context;

    /**
     *
     * @param context context
     */
    public DataCaching(Context context){
        this.context = context;
    }

    /**
     * Save arrayList to a current device memory
     * @param fileName name to save data to
     * @param list date to save
     */
    public void saveDataToInternalStorage(String fileName, ArrayList<Country> list){
        try {
            //this setup a directory to save array in the memory.
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream of = new ObjectOutputStream(fos);
            //write the arraylist.
            of.writeObject(list);
            of.flush();
            //close the directory
            of.close();
            fos.close();
        }
        catch (Exception e) {
            Log.e("InternalStorage", e.getMessage());
        }
    }

    /**
     * Read from the internal storage.
     * @param fileName place to read from
     * @return date read
     */
    public ArrayList<Country> readFromInternalStorage(String fileName){
        //Arraylist to be returned from the memory.
        ArrayList<Country> listToReturn = null;
        FileInputStream inputStream;

        try{
            //get data with specified name
            inputStream = context.openFileInput(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            //return list as country data type object.
            listToReturn = (ArrayList<Country>) objectInputStream.readObject();
            objectInputStream.close();
        }catch (FileNotFoundException e){
            e.fillInStackTrace();
            Log.e("File not Found", e.getMessage());
        }catch (IOException e){
//            Log.e("InternalStorage", e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return listToReturn;
    }
}
