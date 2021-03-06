package com.blockvote.registrarapplication.qrCode;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import com.blockvote.registrarapplication.LoginActivity;
import com.blockvote.registrarapplication.MainActivity;
import com.blockvote.registrarapplication.SavedQRCode;
import com.blockvote.registrarapplication.model.RegisterVoterModel;
import com.blockvote.registrarapplication.network.BlockVoteServerAPI;
import com.blockvote.registrarapplication.network.BlockVoteServerInstance;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import org.spongycastle.util.encoders.Base64;
import com.google.gson.Gson;
import android.content.SharedPreferences;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blockvote.registrarapplication.R;

import com.blockvote.registrarapplication.crypto.*;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateQRActivity extends AppCompatActivity {

    private final String LOG_TAG = GenerateQRActivity.class.getSimpleName();
    private ImageView imageView;
    private static int QRcodeWidth = 500 ;
    private Bitmap bitmap ;
    private String blindedTokenString;
    private String signedToken;
    private TokenRequest tokenRequest;
    private byte[] signature;
    private SharedPreferences dataStore;
    private ValueAnimator animator;
    private View progressBarView;
    private int ScreenWidth;
    private ProgressBar registrationProgress;
    private Intent mainMenu;
    private Intent loginActivity;
    private Button buttonContinue, buttonSaveQR;
    private boolean savedQRCode;
    private boolean registrationCompleted;
    private int voterID;
    List<SavedQRCode> savedQRcodeList;
    private String authorization;
    private boolean generatingQRcode;
    ProgressBar progressRegisterVoter;
    private Registrar registrar;
    private String savedRegisteredVoters;

    GenerateQRActivity generateQRActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        ScreenWidth = width;

        setContentView(R.layout.activity_generate_qr);
        generateQRActivity = this;
        generatingQRcode = false;
        processExtraData();

        progressRegisterVoter = (ProgressBar) findViewById(R.id.progressBarRegisterVoter);

        mainMenu = new Intent(this, MainActivity.class);
        mainMenu.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        loginActivity = new Intent(this, LoginActivity.class);
        loginActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        imageView = (ImageView)findViewById(R.id.image_QRCode);

        progressBarView = (View) findViewById(R.id.progressBarShowQR);
        registrationProgress = (ProgressBar) progressBarView.findViewById(R.id.progressBar);
        registrationProgress.setScaleY(2f);

        buttonContinue = (Button)progressBarView.findViewById(R.id.button_Continue);
        buttonSaveQR = (Button)progressBarView.findViewById(R.id.button_Back);
        buttonContinue.setText("Complete");
        buttonSaveQR.setText("Save QR");

        final ImageView backgroundOne = (ImageView) findViewById(R.id.background_one);
        final ImageView backgroundTwo = (ImageView) findViewById(R.id.background_two);

        animator = ValueAnimator.ofFloat(0.1f, 0.9f);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(3000L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();
                final float width = backgroundOne.getWidth();
                final float translationX = width * progress;
                backgroundOne.setTranslationX(translationX);
                backgroundTwo.setTranslationX(translationX - width);
            }
        });

        buttonSaveQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(GenerateQRActivity.this)
                        .setMessage("If you save, voter registration will NOT be completed!\nOnly save if voter has NOT scanned QR code.")
                        .setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                //TODO save QR code
                                saveQRCode(false);

                                startActivity(mainMenu);
                            }
                        })
                        .setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Hide the status bar.
                                View decorView = getWindow().getDecorView();
                                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                                decorView.setSystemUiVisibility(uiOptions);
                            }
                        })
                        .show();
            }
        });

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!registrationCompleted) {

                    buttonSaveQR.setVisibility(View.GONE);
                    buttonContinue.setVisibility(View.GONE);
                    progressRegisterVoter.setVisibility(View.VISIBLE);

                    BlockVoteServerInstance blockVoteServerInstance = new BlockVoteServerInstance(registrar.serverURL);
                    BlockVoteServerAPI apiService = blockVoteServerInstance.getAPI();

                    Call<RegisterVoterModel> call = apiService.registerVoter("US", Integer.toString(voterID), registrar.name, authorization);


                    call.enqueue(new Callback<RegisterVoterModel>() {
                        @Override
                        public void onResponse(Call<RegisterVoterModel> call, Response<RegisterVoterModel> response) {
                            int statusCode = response.code();

                            buttonSaveQR.setVisibility(View.VISIBLE);
                            buttonContinue.setVisibility(View.VISIBLE);
                            progressRegisterVoter.setVisibility(View.GONE);

                            //TODO: If we get code 200 the registration worked
                            //400 or 500 the error
                            //401 login is incorrect
                            //when you get 401 you need to relogin
                            String serverResponse;

                            //Unable to get a response from the server
                            if(response.body() == null){
                                new AlertDialog.Builder(GenerateQRActivity.this)
                                        .setMessage("Error communicating with Server. Voter registration not completed.\nPlease contact BlockVote administrators.")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                saveQRCode(false);
                                                startActivity(mainMenu);
                                            }
                                        })
                                        .show();

                                return;
                            }

                            if (statusCode == 401)
                            {
                                //TODO
                                //idToken has expired, the registrar needs to login again to get new idToken

                                registrationProgress.setProgress(100);
                                new AlertDialog.Builder(GenerateQRActivity.this)
                                        .setMessage("You secure idToken has expired. Voter registration not completed.\nPlease re-login.")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                saveQRCode(false);

                                                //TODO start login activity not main activity
                                                startActivity(loginActivity);
                                            }
                                        })
                                        .show();

                            }

                            if(response.body().getResponse() != null) {
                                serverResponse = response.body().getResponse().getResult();


                                if (statusCode == 200)
                                {
                                    Log.d("LOG INFO", "Registered voter... Result: " + serverResponse);
                                    //Toast.makeText(getApplicationContext(), "Registered Voter", Toast.LENGTH_SHORT).show();

                                    registrationProgress.setProgress(100);
                                    new AlertDialog.Builder(GenerateQRActivity.this)
                                            .setMessage("Voter registration completed")
                                            .setCancelable(false)
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    saveQRCode(true);
                                                    startActivity(mainMenu);
                                                }
                                            })
                                            .show();
                                }
                            }
                            else
                            {
                                Log.e("ERROR", "registering voter failed...");
                                Log.e("========DEBUG=======", "========================= Status code is: " + statusCode + " ========================");


                                serverResponse = response.body().getError().getMessage();
                                Toast.makeText(getApplicationContext(), serverResponse, Toast.LENGTH_SHORT).show();


                                //TODO: fail because already registered
                                //Error code is 200 when already registered

                                new AlertDialog.Builder(GenerateQRActivity.this)
                                        .setMessage("Voter registration not complete.\n" + serverResponse)
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                saveQRCode(false);
                                                startActivity(mainMenu);
                                            }
                                        })
                                        .show();

/*
                                new AlertDialog.Builder(GenerateQRActivity.this)
                                        .setMessage("You are not registered as a valid registrar. Voter registration not complete.\nPlease contact BlockVote administrators to enroll you.")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                saveQRCode(false);
                                                startActivity(mainMenu);
                                            }
                                        })
                                        .show();
*/
                            }

                        }

                        @Override
                        public void onFailure(Call<RegisterVoterModel> call, Throwable t) {
                            Log.e("ERROR", "ERROR on RegisterVoterModel call");

                            buttonSaveQR.setVisibility(View.VISIBLE);
                            buttonContinue.setVisibility(View.VISIBLE);
                            progressRegisterVoter.setVisibility(View.GONE);

                            new AlertDialog.Builder(GenerateQRActivity.this)
                                    .setMessage("Voter registration not complete.\n Please try again...")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            saveQRCode(false);
                                            startActivity(mainMenu);
                                        }
                                    })
                                    .show();
                        }
                    });

                }
                else
                {
                    saveQRCode(true);
                    startActivity(mainMenu);
                }
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

        //Check to see if we need to create the QR code
        //savedQRCode = getIntent().getBooleanExtra("savedQRCode",false);
        //voterID = getIntent().getIntExtra("voterID",0);

        if(!generatingQRcode) {
            View rootView = this.findViewById(android.R.id.content);

            //new voter
            imageView.setVisibility(View.GONE);
            findViewById(R.id.textView_show_QR_code).setVisibility(View.GONE);
            buttonSaveQR.setVisibility(View.GONE);
            buttonContinue.setVisibility(View.GONE);

            rootView.findViewById(R.id.qrCode_progress).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.background_one).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.background_two).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.textView_generate_QR_code).setVisibility(View.VISIBLE);


            if (!savedQRCode) {
                registrationProgress.setProgress(50);

                try {
                    tokenRequest = new TokenRequest(Base64.decode(blindedTokenString));

                    dataStore = getSharedPreferences("RegistrarData", MODE_PRIVATE);

                    Gson gson = new Gson();

                    String json = dataStore.getString("registrar", "");
                    String idToken = dataStore.getString("id_token", "");

                    authorization = "Bearer " + idToken;

                    if (json == null) {
                        Log.e("ERROR", "ERROR json is null");
                    }
                    registrar = gson.fromJson(json, Registrar.class);

                    byte[] temp = registrar.sign(tokenRequest);

                    signedToken = Base64.toBase64String(temp);
                } catch (Exception ex) {
                    Log.e("ERROR", "Error reading QR code or signing token");

                    //TODO testing saving QR codes without server - delete after and replace with error handling (eg an alert dialog)
                    //signedToken = blindedTokenString;

                    new AlertDialog.Builder(GenerateQRActivity.this)
                            .setMessage("Invalid QR code, please retry voter registration")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(mainMenu);
                                }
                            })
                            .show();

                    return;

                }
            } else {

                signedToken = getVoterQRCode(voterID);

                if (registrationCompleted) {
                    registrationProgress.setProgress(100);
                } else {
                    registrationProgress.setProgress(50);
                }
            }


            //Generate QR code using AsyncTask
            generatingQRcode = true;
            new QRGenerator(rootView).execute(signedToken);

            animator.start();

        }

    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.e("====sDEBUG====", "=========================ON PAUSE==========================");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.e("====sDEBUG====", "=========================ON STOP==========================");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processExtraData();
    }

    private void processExtraData(){
        Intent intent = getIntent();
        registrationCompleted = getIntent().getBooleanExtra("completedRegistration", false);
        savedQRCode = getIntent().getBooleanExtra("savedQRCode",false);
        voterID = getIntent().getIntExtra("voterID",0);
        blindedTokenString = getIntent().getStringExtra("ScannedQRCodeBlindedTokenString");
    }

    @Override
    public void onBackPressed() {

        if (!registrationCompleted) {
            new AlertDialog.Builder(GenerateQRActivity.this)
                    .setMessage("If you save, voter registration will NOT be completed!\nOnly save if voter has NOT scanned QR code.")
                    .setCancelable(false)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            //TODO save QR code
                            saveQRCode(false);

                            startActivity(mainMenu);
                        }
                    })
                    .setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Hide the status bar.
                            View decorView = getWindow().getDecorView();
                            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                            decorView.setSystemUiVisibility(uiOptions);
                        }
                    })
                    .show();
        }
        else
        {
            startActivity(mainMenu);
        }

    }



    private void saveQRCode(boolean registrationCompleted){

        dataStore = getSharedPreferences("RegistrarData", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = dataStore.getString("registrar", "");
        if (json == null) {
            Log.e("ERROR", "ERROR json is null");
        }
        registrar = gson.fromJson(json, Registrar.class);
        savedRegisteredVoters = registrar.name + "_SavedQRCodeList";


        dataStore = getSharedPreferences("SavedData", MODE_PRIVATE);
        SharedPreferences.Editor editor = dataStore.edit();

        gson = new Gson();

        json = dataStore.getString(savedRegisteredVoters, "");
        if(json==null){
            Log.e("ERROR", "ERROR json is null");
        }

        Type type = new TypeToken<List<SavedQRCode>>(){}.getType();
        savedQRcodeList = gson.fromJson(json, type);

        if (savedQRcodeList == null)
        {
            savedQRcodeList = new LinkedList<SavedQRCode>();
        }

        SavedQRCode newCode = new SavedQRCode();
        newCode.completed = registrationCompleted;
        newCode.QRCode = signedToken;
        newCode.voterID = Integer.toString(voterID);


        /*
        for (SavedQRCode QRcode : savedQRcodeList) {
            if(QRcode.voterID.equals(newCode.voterID)){
                savedQRcodeList.remove(QRcode);
            }
        }
        */

        int index;
        boolean duplicate = false;

        for (index = 0; index < savedQRcodeList.size(); index++)
        {
            if(savedQRcodeList.get(index).voterID.equals(newCode.voterID)){
                duplicate = true;
                break;
            }
        }

        if (duplicate) {
            savedQRcodeList.remove(index);
        }



        savedQRcodeList.add(newCode);

        json = gson.toJson(savedQRcodeList);


        editor.putString(savedRegisteredVoters, json);
        editor.commit();
    }

    private String getVoterQRCode(int voter){

        dataStore = getSharedPreferences("RegistrarData", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = dataStore.getString("registrar", "");
        if (json == null) {
            Log.e("ERROR", "ERROR json is null");
        }
        registrar = gson.fromJson(json, Registrar.class);
        savedRegisteredVoters = registrar.name + "_SavedQRCodeList";

        dataStore = getSharedPreferences("SavedData", MODE_PRIVATE);
        SharedPreferences.Editor editor = dataStore.edit();

        gson = new Gson();

        json = dataStore.getString(savedRegisteredVoters, "");
        if(json==null){
            Log.e("ERROR", "ERROR json is null");
        }

        Type type = new TypeToken<List<SavedQRCode>>(){}.getType();
        savedQRcodeList = gson.fromJson(json, type);

        if (savedQRcodeList == null)
        {
            //TODO
            return "LIST EMPTY";
        }

        for (SavedQRCode QRcode : savedQRcodeList) {
            if(QRcode.voterID.equals(Integer.toString(voter))){
                return QRcode.QRCode;
            }
        }

        //TODO
        return "NOT FOUND";
    }


    public class QRGenerator extends AsyncTask<String, Integer, Bitmap> {
        private final String LOG_TAG= "QRGenerator";
        private View rootView;
        private ArcProgress pb;

        public QRGenerator(View rootView){
            this.rootView=rootView;
            Log.e(LOG_TAG, "QR generation started... " );
            pb = (ArcProgress) findViewById(R.id.qrCode_arc_progress);
        }

        @Override
        public Bitmap doInBackground (String... params) {
            try{
                Bitmap bitmap= TextToImageEncode(params[0]);
                return bitmap;
            }catch(WriterException writerException){
                Log.e(LOG_TAG, "QR generation failed... " + "\n" + writerException.getMessage() );
                return null;
            }

        }

        @Override
        public void onPostExecute(Bitmap bitmap){
            Log.v(LOG_TAG, "Displaying the QR now");
            ImageView imageView = (ImageView) rootView.findViewById(R.id.image_QRCode);

            imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, ScreenWidth, ScreenWidth, false));

            final Animation fadeOut = AnimationUtils.loadAnimation(GenerateQRActivity.this,R.anim.fadeout);
            final Animation fadeIn = AnimationUtils.loadAnimation(GenerateQRActivity.this,R.anim.fadein);

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
/* TODO remove
                    // Testing proper saving of QR code values
                    Toast.makeText(getApplicationContext(),
                            "QR code is :" + signedToken, Toast.LENGTH_LONG)
                            .show();
*/

                    if(!savedQRCode)
                    {
                        rootView.findViewById(R.id.image_QRCode).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.image_QRCode).startAnimation(fadeIn);

                        rootView.findViewById(R.id.textView_show_QR_code).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.textView_show_QR_code).startAnimation(fadeIn);

                        progressBarView.findViewById(R.id.button_Continue).setVisibility(View.VISIBLE);
                        progressBarView.findViewById(R.id.button_Continue).startAnimation(fadeIn);
                        progressBarView.findViewById(R.id.button_Back).setVisibility(View.VISIBLE);
                        progressBarView.findViewById(R.id.button_Back).startAnimation(fadeIn);

                        registrationProgress.setProgress(75);
                    }
                    else
                    {
                        rootView.findViewById(R.id.image_QRCode).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.image_QRCode).startAnimation(fadeIn);
                        rootView.findViewById(R.id.textView_show_QR_code).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.textView_show_QR_code).startAnimation(fadeIn);
                        progressBarView.findViewById(R.id.button_Continue).setVisibility(View.VISIBLE);
                        progressBarView.findViewById(R.id.button_Continue).startAnimation(fadeIn);

                        if(registrationCompleted)
                        {
                            progressBarView.findViewById(R.id.button_Back).setVisibility(View.GONE);
                            registrationProgress.setProgress(100);
                        }
                        else
                        {
                            progressBarView.findViewById(R.id.button_Back).setVisibility(View.VISIBLE);
                            progressBarView.findViewById(R.id.button_Back).startAnimation(fadeIn);
                            registrationProgress.setProgress(75);
                        }
                    }

                    generatingQRcode = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });


            animator.pause();

            rootView.findViewById(R.id.qrCode_progress).startAnimation(fadeOut);
            rootView.findViewById(R.id.background_one).startAnimation(fadeOut);
            rootView.findViewById(R.id.background_two).startAnimation(fadeOut);
            rootView.findViewById(R.id.textView_generate_QR_code).startAnimation(fadeOut);
            rootView.findViewById(R.id.qrCode_progress).setVisibility(View.GONE);
            rootView.findViewById(R.id.background_one).setVisibility(View.GONE);
            rootView.findViewById(R.id.background_two).setVisibility(View.GONE);
            rootView.findViewById(R.id.textView_generate_QR_code).setVisibility(View.GONE);


        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.v(LOG_TAG, "Progress Update" + values[0]);
            pb.setProgress(values[0]);
        }

        private Bitmap TextToImageEncode(String Value) throws WriterException {
            BitMatrix bitMatrix;
            try {
                bitMatrix = new MultiFormatWriter().encode(
                        Value,
                        BarcodeFormat.DATA_MATRIX.QR_CODE,
                        QRcodeWidth, QRcodeWidth, null
                );

            } catch (IllegalArgumentException Illegalargumentexception) {

                return null;
            }
            catch (Exception ex) {
                Log.e("ERROR", "Catching exception when attempting to create QR code");
                return null;
            }

            int bitMatrixWidth = bitMatrix.getWidth();

            int bitMatrixHeight = bitMatrix.getHeight();

            Log.v(LOG_TAG, "QR Width: " + bitMatrixWidth + ", Height: " + bitMatrixHeight);
            int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

            int percentage = 0;
            int increment = bitMatrixHeight/100;

            for (int y = 0; y < bitMatrixHeight; y++) {
                int offset = y * bitMatrixWidth;
                if (y%increment == 0 && percentage < 100) {
                    percentage++;
                    publishProgress(percentage);
                }
                for (int x = 0; x < bitMatrixWidth; x++) {
//                getResources().getColor(R.color.QR)
                    pixels[offset + x] = bitMatrix.get(x, y) ?
                            getResources().getColor(R.color.QRDarkColor):getResources().getColor(R.color.QRWhite);

                }
            }
            Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

            bitmap.setPixels(pixels, 0, bitMatrixWidth, 0, 0, bitMatrixWidth, bitMatrixHeight);
            return bitmap;
        }
    }

}
