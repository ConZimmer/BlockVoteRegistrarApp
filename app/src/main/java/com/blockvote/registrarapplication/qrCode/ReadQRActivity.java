package com.blockvote.registrarapplication.qrCode;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blockvote.registrarapplication.MainActivity;
import com.blockvote.registrarapplication.R;
import com.blockvote.registrarapplication.SavedQRCode;
import com.blockvote.registrarapplication.model.RegisterVoterModel;
import com.blockvote.registrarapplication.network.BlockVoteServerAPI;
import com.blockvote.registrarapplication.network.BlockVoteServerInstance;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadQRActivity extends AppCompatActivity {

    private EditText voterID;
    private int i_voterID;
    private IntentIntegrator integrator;
    private Intent mainMenu;
    private View progressBarView;
    private ProgressBar registrationProgress;
    private SharedPreferences dataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_qr);

        mainMenu = new Intent(this, MainActivity.class);
        mainMenu.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        progressBarView = (View) findViewById(R.id.progressBarEnterID);
        registrationProgress = (ProgressBar) progressBarView.findViewById(R.id.progressBar);
        registrationProgress.setScaleY(2f);

        voterID = (EditText)findViewById(R.id.editTextVoterID);
        TextView textView = (TextView) this.findViewById(R.id.readQR_blurb);
        textView.setText("Verify voters identity and enter their 'voter ID'");

        View progressMenu = findViewById(R.id.progressBarEnterID);
        Button readQRButton = (Button)progressMenu.findViewById(R.id.button_Continue);
        Button backButton = (Button)progressMenu.findViewById(R.id.button_Back);

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

                if (registerVoter()) {
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

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(mainMenu);
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

        registrationProgress.setProgress(25);
        voterID.setText("");

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
                lockIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                lockIntent.putExtra("ScannedQRCodeBlindedTokenString", result.getContents());
                lockIntent.putExtra("savedQRCode", false);
                lockIntent.putExtra("voterID", i_voterID);
                startActivity(lockIntent);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {

    }

    private boolean registerVoter(){

        String govID = null;
        if(voterID != null){
            govID = voterID.getText().toString();
        }
        if (govID == null || govID.equals("") )
        {
            new AlertDialog.Builder(this)
                    .setMessage("Please enter the voter ID")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .show();
            return false;
        }

        List<SavedQRCode> savedQRcodeList;
        dataStore = getSharedPreferences("SavedData", MODE_PRIVATE);

        Gson gson = new Gson();

        String json = dataStore.getString("SavedQRCodeList", "");
        if(json==null){
            Log.e("ERROR", "ERROR json is null");
        }

        Type type = new TypeToken<List<SavedQRCode>>(){}.getType();
        savedQRcodeList = gson.fromJson(json, type);

        if (savedQRcodeList != null) {
            for (SavedQRCode QRcode : savedQRcodeList) {
                if (QRcode.voterID.equals(govID)) {
                    new AlertDialog.Builder(this)
                            .setMessage("Duplicate voter ID found in registrar app.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            })
                            .show();
                    return false;
                }
            }
        }

        i_voterID = Integer.parseInt(govID);
        Log.d("LOG INFO", "GOV ID: " + govID );
        return true;
    }

}
