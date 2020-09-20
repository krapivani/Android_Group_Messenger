package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class GroupMessengerProvider extends ContentProvider {

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {



          /* Used StackOverflow to understand how to iterate ContentValues and get Key and Value
        https://stackoverflow.com/questions/2390244/how-to-get-the-keys-from-contentvalues
        */


        Set<Map.Entry<String, Object>> val1 = values.valueSet();
        Iterator it = val1.iterator();
        String key ="";
        String value = "";

        String filename="";

        while(it.hasNext()) {

            Map.Entry me = (Map.Entry) it.next();
            key = me.getKey().toString();
            if (key.equals("key")) {
                filename = me.getValue().toString();
                //Log.v("insert",filename + " here");
            }
            if (key.equals("value")) {
                value = me.getValue().toString();
            }
        }


        FileOutputStream outputStream;


        try {
            Log.v("insert", "Key: " + filename + " Value: " + value);
            outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(value.getBytes());
            outputStream.close();
            //Log.v("insert", "Key: " + filename + " Value: " + value.toString());
        } catch (Exception e) {
            //Log.v("insert", "File write failed");
            e.printStackTrace();
        }


        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {


        FileInputStream fis = null;
        String text ="";
        try {
            fis = getContext().openFileInput(selection);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                text = text + line;
            }
        } catch (FileNotFoundException e) {
            //showToast("Exception e!");
            e.printStackTrace();
        } catch (IOException o) {
            //showToast("IOException");
            o.printStackTrace();
        }


        String[] column = new String[2];
        column[0] = "key";
        column[1] = "value";
        MatrixCursor cursor = new MatrixCursor(column);
        String[] add = new String[2];
        add[0] = selection;
        add[1] = text;
        cursor.addRow(add);

        Log.v("query", selection + " " + text);
        return cursor;
    }
}
