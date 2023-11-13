package com.rivten.chessopenings;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.util.Log;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

import java.util.Random;

import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

import android.content.res.AssetManager;

public class MainActivity extends Activity {

    private boolean toggled = false;
    public SQLiteDatabase db;

    private void openDatabase() {
        // Apparently, this is what it takes to "extract" an asset from the APK
        // Since the APK is just a zip, and since an android app can only access
        // and also since the SQLiteDatabase interface does not allow to open
        // a database with an InputStream (why take only a file path and not a
        // buffer to the content of the file directly ??), then all we can do
        // is copy the content of the asset file from the APK to the disk.
        // And you have to deal with new version yourself or take the hit to
        // do the copy each time yourself.
        // I was able to do all this thanks to this repo :
        // https://github.com/jgilfelt/android-sqlite-asset-helper
        // I mostly re-wrote a simplified version of this code.
        try {
            File dbFile = new File(getApplicationInfo().dataDir + "/small.db");
            if (dbFile.exists()) {
                db = SQLiteDatabase.openDatabase(getApplicationInfo().dataDir + "/small.db", null, SQLiteDatabase.OPEN_READONLY);
            } else {
                InputStream is = getAssets().open("small.db");
                OutputStream outs = new FileOutputStream(getApplicationInfo().dataDir + "/small.db");
                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = is.read(buffer)) > 0) {
                    outs.write(buffer, 0, length);
                }
                outs.flush();
                outs.close();
                is.close();
                db = SQLiteDatabase.openDatabase(getApplicationInfo().dataDir + "/small.db", null, SQLiteDatabase.OPEN_READONLY);
            }
        } catch (IOException e) {
            Log.e("exception", e.getMessage());
        }
        db.rawQuery("PRAGMA writable_schema=ON", null);

    }

    private class Opening {
        final public String eco;
        final public String name;
        final public String pgn;

        public Opening(String eco, String name, String pgn) {
            this.eco = eco;
            this.name = name;
            this.pgn = pgn;
        }
    }

    private List<Opening> openings;

    private List<Opening> openOpeningAssets() {
        List<Opening> result = new ArrayList<Opening>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("openings.tsv")));

            String line;
            int lineIndex = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (lineIndex == 0) {
                    lineIndex += 1;
                    continue;
                }
                String[] splits = line.split("\t");
                Opening opening = new Opening(splits[0], splits[1], splits[2]);
                result.add(opening);
                lineIndex += 1;
            }
        } catch (IOException e) {
            Log.e("exception", e.getMessage());
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LinearLayout openingsToPlayLayout = (LinearLayout) findViewById(R.id.openingsToPlay);

        MainActivity activity = this;

        openings = openOpeningAssets();

        for (Opening opening : openings) {
            Log.e(">>>", opening.eco + " - " + opening.name + " - " + opening.pgn);
        }
        Log.e(">>>", openings.get(0).eco + " - " + openings.get(0).name + " - " + openings.get(0).pgn);

        Random random = new Random();

        TextView[] openingViews = new TextView[10];
        openingViews[0] = (TextView) findViewById(R.id.opening0);
        openingViews[1] = (TextView) findViewById(R.id.opening1);
        openingViews[2] = (TextView) findViewById(R.id.opening2);
        openingViews[3] = (TextView) findViewById(R.id.opening3);
        openingViews[4] = (TextView) findViewById(R.id.opening4);
        openingViews[5] = (TextView) findViewById(R.id.opening5);
        openingViews[6] = (TextView) findViewById(R.id.opening6);
        openingViews[7] = (TextView) findViewById(R.id.opening7);
        openingViews[8] = (TextView) findViewById(R.id.opening8);
        openingViews[9] = (TextView) findViewById(R.id.opening9);

        final Button getRandomOpeningsButton = (Button) findViewById(R.id.getNewOpeningsButton);
        getRandomOpeningsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Cursor cursor = db.rawQuery("select sqlite_version();", null);
                //if (cursor.moveToNext())
                //{
                //    String version = cursor.getString(0);
                //    Log.e(">>>>", version);
                //}
                //SQLiteDatabase db = SQLiteDatabase.openDatabase("assets/small.db", null, SQLiteDatabase.OPEN_READONLY);
                //Cursor cursor = db.rawQuery("SELECT move_uci FROM graph WHERE fen_rowid = ? ORDER BY RANDOM() LIMIT 5", new String[]{"1"});
                //while (cursor.moveToNext())
                //{
                //    String moveUCI = cursor.getString(0);
                //    Log.e(">>>", moveUCI);
                //}
                for (TextView openingView : openingViews) {
                    Opening randomOpening = openings.get(random.nextInt(openings.size()));
                    openingView.setText(randomOpening.eco + " - " + randomOpening.name + " - " + randomOpening.pgn);
                }
            }
        });
    }
}
