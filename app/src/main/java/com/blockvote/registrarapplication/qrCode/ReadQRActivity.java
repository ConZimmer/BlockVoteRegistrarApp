package com.blockvote.registrarapplication.qrCode;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blockvote.registrarapplication.MainActivity;
import com.blockvote.registrarapplication.R;
import com.blockvote.registrarapplication.SavedQRCode;
import com.blockvote.registrarapplication.crypto.Registrar;
import com.blockvote.registrarapplication.model.VoterRegRecordModel;
import com.blockvote.registrarapplication.network.BlockVoteServerAPI;
import com.blockvote.registrarapplication.network.BlockVoteServerInstance;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;
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
    private String serverResponse;
    Button continueButton;
    Button backButton;
    ProgressBar verifyVoterProgressBar;
    EditText voterIDEditText;
    private Registrar registrar;
    private String savedRegisteredVoters;

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
        continueButton = (Button)progressMenu.findViewById(R.id.button_Continue);
        backButton = (Button)progressMenu.findViewById(R.id.button_Back);
        verifyVoterProgressBar = (ProgressBar)findViewById(R.id.progressBarVerifyVoter);
        voterIDEditText = (EditText)findViewById(R.id.editTextVoterID);


        final Activity activity = this;
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                continueButton.setVisibility(View.GONE);
                backButton.setVisibility(View.GONE);
                voterIDEditText.setVisibility(View.GONE);
                verifyVoterProgressBar.setVisibility(View.VISIBLE);

                integrator = new IntentIntegrator(activity);

                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);

                registerVoter();
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
                            continueButton.setVisibility(View.VISIBLE);
                            backButton.setVisibility(View.VISIBLE);
                            voterIDEditText.setVisibility(View.VISIBLE);
                            verifyVoterProgressBar.setVisibility(View.GONE);
                        }
                    })
                    .show();
            return false;
        }

        dataStore = getSharedPreferences("RegistrarData", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = dataStore.getString("registrar", "");
        if (json == null) {
            Log.e("ERROR", "ERROR json is null");
        }
        registrar = gson.fromJson(json, Registrar.class);
        savedRegisteredVoters = registrar.name + "_SavedQRCodeList";

        List<SavedQRCode> savedQRcodeList;
        dataStore = getSharedPreferences("SavedData", MODE_PRIVATE);

        gson = new Gson();

        json = dataStore.getString(savedRegisteredVoters, "");
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
                                    continueButton.setVisibility(View.VISIBLE);
                                    backButton.setVisibility(View.VISIBLE);
                                    voterIDEditText.setVisibility(View.VISIBLE);
                                    verifyVoterProgressBar.setVisibility(View.GONE);
                                }
                            })
                            .show();
                    return false;
                }
            }
        }


        BlockVoteServerInstance blockVoteServerInstance = new BlockVoteServerInstance(registrar.serverURL);
        BlockVoteServerAPI apiService = blockVoteServerInstance.getAPI();

        Call<VoterRegRecordModel> call = apiService.voterRegRecord("US", govID);


        call.enqueue(new Callback<VoterRegRecordModel>() {
            @Override
            public void onResponse(Call<VoterRegRecordModel> call, Response<VoterRegRecordModel> response) {
                int statusCode = response.code();

                continueButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                voterIDEditText.setVisibility(View.VISIBLE);
                verifyVoterProgressBar.setVisibility(View.GONE);

                //Unable to get a response from the server
                if(response.body() == null){
                    new AlertDialog.Builder(ReadQRActivity.this)
                            .setMessage("Error communicating with Server. Voter registration not completed.\nPlease contact BlockVote administrators.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            })
                            .show();

                    return;
                }

                if(response.body().getResponse() != null) {

                    serverResponse = response.body().getResponse();

                    new AlertDialog.Builder(ReadQRActivity.this)
                            .setMessage("WARNING:\nVoter has already registered.\nPreventing Registration.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            })
                            .show();

                    return;
                }
                else
                {
                    serverResponse = response.body().getError().getMessage();

                    //TODO: very poor way of checking, just a temp quick solution,
                    if (serverResponse.toLowerCase().contains("voter with govID".toLowerCase()))
                    {
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                        integrator.setPrompt("Scan");
                        integrator.setCameraId(0);
                        integrator.setBeepEnabled(false);
                        integrator.setBarcodeImageEnabled(false);
                        integrator.initiateScan();
                    }
                    else
                    {

                        new AlertDialog.Builder(ReadQRActivity.this)
                                .setMessage("Please try again...")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                })
                                .show();
                    }

                    return;
                }



            }

            @Override
            public void onFailure(Call<VoterRegRecordModel> call, Throwable t) {
                Log.e("ERROR", "ERROR on VoterRegRecordModel");

                //Toast.makeText(getApplicationContext(), "Error on VoterRegRecordModel", Toast.LENGTH_SHORT).show();

                new AlertDialog.Builder(ReadQRActivity.this)
                        .setMessage("Voter registration not complete.\n Please try again...")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                continueButton.setVisibility(View.VISIBLE);
                                backButton.setVisibility(View.VISIBLE);
                                voterIDEditText.setVisibility(View.VISIBLE);
                                verifyVoterProgressBar.setVisibility(View.GONE);
                                startActivity(mainMenu);
                            }
                        })
                        .show();
            }
        });


        i_voterID = Integer.parseInt(govID);
        Log.d("LOG INFO", "GOV ID: " + govID );
        return true;
    }

}
