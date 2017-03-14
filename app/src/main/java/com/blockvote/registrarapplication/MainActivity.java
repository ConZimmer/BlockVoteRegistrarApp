package com.blockvote.registrarapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;

import com.blockvote.registrarapplication.qrCode.ReadQRActivity;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.zxing.integration.android.IntentIntegrator;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences dataStore;
    private int progress = 0;
    private ListView incompleteList;
    private ListView completedList;

    private Intent readQRIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readQRIntent = new Intent(this, ReadQRActivity.class);
        readQRIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar myActionBar = (ActionBar) getSupportActionBar();
        myActionBar.setTitle("Welcome 'registar Name goes here' ");

        //Progress Bar
        /*
        View theView = (View) findViewById(R.id.progressBarMain);
        ProgressBar pb = (ProgressBar) theView.findViewById(R.id.progressBar);

        pb.setProgress(80);
        pb.setScaleY(2f);
        */


        TabHost host = (TabHost)findViewById(R.id.main_tab_menu);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Tab One");
        spec.setContent(R.id.tab1_register_voter);
        spec.setIndicator("Add Voter");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Tab Two");
        spec.setContent(R.id.tab2_incomplete_registrations);
        spec.setIndicator("Incomplete");
        host.addTab(spec);

        //Tab 3
        spec = host.newTabSpec("Tab Three");
        spec.setContent(R.id.tab3_completed_registrations);
        spec.setIndicator("Completed");
        host.addTab(spec);

        incompleteList = (ListView) findViewById(R.id.list_incomplete);


        //TODO testing delete after
        String[] values = new String[] { "Android List View",
                "Adapter implementation",
                "Simple List View In Android",
                "Create List View Android",
                "Android Example",
                "List View Source Code",
                "List View Array Adapter",
                "Android Example List View",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8"
        };

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        // Assign adapter to ListView
        incompleteList.setAdapter(adapter);

        // ListView Item Click Listener
        incompleteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) incompleteList.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                        .show();

            }

        });


        dataStore = getPreferences(MODE_PRIVATE);
        if (!dataStore.contains("access_token")){
            //Need to login
            //Intent lockIntent = new Intent(this, LoginActivity.class);
            //startActivity(lockIntent);
        }


        ImageButton addVoter = (ImageButton)findViewById(R.id.imageButtonAddVoter);
        addVoter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(readQRIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        //Incomplete list

    }


    @Override
    public void onBackPressed() {
        Log.d("MainActivity", "Back Button was pressed");

        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Hide the status bar.
                        View decorView = getWindow().getDecorView();
                        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                        decorView.setSystemUiVisibility(uiOptions);
                    }
                })
                .show();
    }

}
