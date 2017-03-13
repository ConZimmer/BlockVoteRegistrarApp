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

import com.blockvote.registrarapplication.MainActivity;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import org.spongycastle.util.encoders.Base64;
import com.google.gson.Gson;
import android.content.SharedPreferences;
import android.widget.ProgressBar;

import com.blockvote.registrarapplication.R;

import com.blockvote.registrarapplication.crypto.*;

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
    private Button buttonContinue, buttonSaveQR;
    private boolean savedQRCode;
    private int voterID;

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
        processExtraData();

        mainMenu = new Intent(this, MainActivity.class);
        mainMenu.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

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
                        .setMessage("If you save, voter registration will not be complete!\nOnly save if voter has NOT scanned QR code.")
                        .setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Complete
                                //TODO save QR code
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
                //TODO wrtie to block chain
                //TODO save QR code
                startActivity(mainMenu);
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

        View rootView = this.findViewById(android.R.id.content);

        if (!savedQRCode)
        {
            //new voter
            imageView.setVisibility(View.GONE);
            findViewById(R.id.textView_show_QR_code).setVisibility(View.GONE);
            buttonSaveQR.setVisibility(View.GONE);
            buttonContinue.setVisibility(View.GONE);

            rootView.findViewById(R.id.qrCode_progress).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.background_one).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.background_two).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.textView_generate_QR_code).setVisibility(View.VISIBLE);

            registrationProgress.setProgress(50);


            //blindedTokenString = getIntent().getStringExtra("ScannedQRCodeBlindedTokenString");

            try
            {
                tokenRequest = new TokenRequest(Base64.decode(blindedTokenString));

                dataStore = getSharedPreferences("RegistrarData", MODE_PRIVATE);

                Gson gson = new Gson();

                String json = dataStore.getString("registrar", "");
                if(json==null){
                    Log.e("ERROR", "ERROR json is null");
                }
                Registrar registrar = gson.fromJson(json, Registrar.class);

                byte[] temp = registrar.sign(tokenRequest);

                signedToken = Base64.toBase64String(temp);
            }
            catch(Exception ex)
            {
                Log.e("ERROR", "Error reading QR code or signing token");
            }


            //Generate QR code using AsyncTask
            new QRGenerator(rootView).execute(signedToken);

            animator.start();

        }
        else
        {

            rootView.findViewById(R.id.qrCode_progress).setVisibility(View.GONE);
            rootView.findViewById(R.id.background_one).setVisibility(View.GONE);
            rootView.findViewById(R.id.background_two).setVisibility(View.GONE);
            rootView.findViewById(R.id.textView_generate_QR_code).setVisibility(View.GONE);

            rootView.findViewById(R.id.image_QRCode).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.textView_show_QR_code).setVisibility(View.VISIBLE);
            progressBarView.findViewById(R.id.button_Continue).setVisibility(View.VISIBLE);
            progressBarView.findViewById(R.id.button_Back).setVisibility(View.VISIBLE);
            registrationProgress.setProgress(75);

        }


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processExtraData();
    }

    private void processExtraData(){
        Intent intent = getIntent();
        savedQRCode = getIntent().getBooleanExtra("savedQRCode",false);
        voterID = getIntent().getIntExtra("voterID",0);
        blindedTokenString = getIntent().getStringExtra("ScannedQRCodeBlindedTokenString");
    }

    @Override
    public void onBackPressed() {

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
