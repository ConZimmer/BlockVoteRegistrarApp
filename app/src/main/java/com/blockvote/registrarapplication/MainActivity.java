package com.blockvote.registrarapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

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

        View theView = (View) findViewById(R.id.progressBarMain);
        ProgressBar pb = (ProgressBar) theView.findViewById(R.id.progressBar);

        pb.setProgress(80);



        dataStore = getPreferences(MODE_PRIVATE);
        if (!dataStore.contains("access_token")){
            //Need to login
            //Intent lockIntent = new Intent(this, LoginActivity.class);
            //startActivity(lockIntent);
        }
        
    }
}
