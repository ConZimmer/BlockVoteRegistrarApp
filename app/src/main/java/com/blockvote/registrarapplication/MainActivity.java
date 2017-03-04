package com.blockvote.registrarapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TabHost;

import com.blockvote.registrarapplication.qrCode.ReadQRActivity;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences dataStore;
    private int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ActionBar myActionBar = (ActionBar) getSupportActionBar();
        //myActionBar.setTitle("Testing");

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


        dataStore = getPreferences(MODE_PRIVATE);
        if (!dataStore.contains("access_token")){
            //Need to login
            //Intent lockIntent = new Intent(this, LoginActivity.class);
            //startActivity(lockIntent);
        }
        
    }
}
