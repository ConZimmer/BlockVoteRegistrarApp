package com.blockvote.registrarapplication.qrCode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import org.spongycastle.util.encoders.Base64;
import com.google.gson.Gson;
import android.content.SharedPreferences;

import com.blockvote.registrarapplication.R;

import com.blockvote.registrarapplication.crypto.*;

import java.math.BigInteger;

public class GenerateQRActivity extends AppCompatActivity {

    private final String LOG_TAG = GenerateQRActivity.class.getSimpleName();
    private ImageView imageView;
    private final static int QRcodeWidth = 1000 ;
    private Bitmap bitmap ;
    private String blindedTokenString;
    private String signedToken;
    private TokenRequest tokenRequest;
    private byte[] signature;
    private SharedPreferences dataStore;

    GenerateQRActivity generateQRActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);
        generateQRActivity = this;

        imageView = (ImageView)findViewById(R.id.image_QRCode);

        blindedTokenString = getIntent().getStringExtra("ScannedQRCodeBlindedTokenString");

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

        try {
            //TODO: Do this processing using AsyncTask
            bitmap = TextToImageEncode(signedToken);

            imageView.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }

    }

    Bitmap TextToImageEncode(String Value) throws WriterException {
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
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        Log.v(LOG_TAG, "QR Width: " + bitMatrixWidth + ", Height: " + bitMatrixHeight);
        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

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
