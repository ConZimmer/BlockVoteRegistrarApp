package com.blockvote.registrarapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.blockvote.registrarapplication.qrCode.ReadQRActivity;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences dataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataStore = getPreferences(MODE_PRIVATE);
        if (!dataStore.contains("access_token")){
            //Need to login
            Intent lockIntent = new Intent(this, LoginActivity.class);
            startActivity(lockIntent);
        }

        Intent lockIntent = new Intent(this, ReadQRActivity.class);
        startActivity(lockIntent);


    }
}
