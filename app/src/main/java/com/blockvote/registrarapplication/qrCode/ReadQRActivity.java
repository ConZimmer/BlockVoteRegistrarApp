package com.blockvote.registrarapplication.qrCode;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blockvote.registrarapplication.R;
import com.blockvote.registrarapplication.model.RegisterVoterModel;
import com.blockvote.registrarapplication.network.BlockVoteServerAPI;
import com.blockvote.registrarapplication.network.BlockVoteServerInstance;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadQRActivity extends AppCompatActivity {

    private EditText editText;
    private String EditTextValue;
    private IntentIntegrator integrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_qr);

        editText = (EditText)findViewById(R.id.editText);
        TextView textView = (TextView) this.findViewById(R.id.readQR_blurb);
        textView.setText("Please Verify Persons Identity and enter their 'gov ID' number");

        View progressMenu = findViewById(R.id.progressBarEnterID);
        Button readQRButton = (Button)progressMenu.findViewById(R.id.button_Continue);

        final Activity activity = this;
        readQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                integrator = new IntentIntegrator(activity);

                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);

                //TODO uncomment: if (registerVoter()) {
                if(true){
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                    integrator.setPrompt("Scan");
                    integrator.setCameraId(0);
                    integrator.setBeepEnabled(false);
                    integrator.setBarcodeImageEnabled(false);
                    integrator.initiateScan();
                }
                else
                {

                }
            }
        });

        /* TODO move to main menu action bar
        Button clearButton = (Button)findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //clear the SharedPreferences data
                SharedPreferences dataStore = getSharedPreferences("RegistrarData", MODE_PRIVATE);
                SharedPreferences.Editor editor = dataStore.edit();
                editor.remove("access_token");
                editor.commit();

                Log.d("MainActivity", "Cleared");
            }
        });
        */
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                Intent lockIntent = new Intent(this, GenerateQRActivity.class);
                lockIntent.putExtra("ScannedQRCodeBlindedTokenString", result.getContents());
                startActivity(lockIntent);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean registerVoter(){

        String govID = "";
        if(editText != null) {
            govID = editText.getText().toString();
        }
        EditTextValue = govID;

        Log.d("LOG INFO", "GOV ID: " + govID );

        BlockVoteServerInstance blockVoteServerInstance = new BlockVoteServerInstance();
        BlockVoteServerAPI apiService = blockVoteServerInstance.getAPI();
        Call<RegisterVoterModel> call = apiService.registerVoter("US", govID, "david");

        call.enqueue(new Callback<RegisterVoterModel>() {
            @Override
            public void onResponse(Call<RegisterVoterModel> call, Response<RegisterVoterModel> response) {
                int statusCode = response.code();

                if(response.body().getResponse() != null) {
                    String serverResponse = response.body().getResponse().getResult();

                    Log.d("LOG INFO", "Registered voter... Result: " + serverResponse);
                    Toast.makeText(getApplicationContext(), "Registered Voter", Toast.LENGTH_SHORT).show();
                    integrator.initiateScan();
                }
                else
                {
                    Log.e("ERROR", "registering voter failed...");
                    Toast.makeText(getApplicationContext(), "Already registered", Toast.LENGTH_SHORT).show();
                    //TODO: fail because already registered
                }

            }

            @Override
            public void onFailure(Call<RegisterVoterModel> call, Throwable t) {
                Log.e("ERROR", "ERROR");
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                //throw new RuntimeException("Could not register voter");

            }
        });


        return false;
    }

    private void startQRCodeRead()
    {
        integrator.initiateScan();
    }

}
