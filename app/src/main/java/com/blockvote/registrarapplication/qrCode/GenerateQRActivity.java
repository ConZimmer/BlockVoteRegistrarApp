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
    private String blindedToken;
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

        blindedToken = getIntent().getStringExtra("Value");

        tokenRequest = new TokenRequest(Base64.decode(blindedToken));

        dataStore = getPreferences(MODE_PRIVATE);

        Gson gson = new Gson();
        //TODO: Move to login fragment
        SharedPreferences.Editor prefsEditor = dataStore.edit();

        String publicKeyModulusS="AKBDi814o+/Ujo8bF1qjMnyotruZHLv5FWl/xYYFpKLgU4jmeXxhJKY36kmJFK+Kxt6anqmmVBKVZityfp+2lUzojYEJJ9Jzv4qQQQ4BcHijlrrBSvvWZ6KOVB30n2Lgxj99g6B1Eopyq4h+6TC3Sr/DBIkZ0tAH5a3+RG3Q8OEcYGpCQu7v4MIOgF+bFikeu6gk0Mob71TlPGauAIFpc4q3UVBjhbEIyc6vv76Z+RtNd3FZZzsLphzrJB4s6b6TwKpUsIWJ7dXBpkCSVv/sDtB4PeOrzHTH5UHGYkTLbF4o1ie23mbjhIWcSJryJrS+3VMaNuB+waImz/nlJEh/qy0=";
        String publicKeyExponentS="AQAB";
        String privateKeyModulusS="AKBDi814o+/Ujo8bF1qjMnyotruZHLv5FWl/xYYFpKLgU4jmeXxhJKY36kmJFK+Kxt6anqmmVBKVZityfp+2lUzojYEJJ9Jzv4qQQQ4BcHijlrrBSvvWZ6KOVB30n2Lgxj99g6B1Eopyq4h+6TC3Sr/DBIkZ0tAH5a3+RG3Q8OEcYGpCQu7v4MIOgF+bFikeu6gk0Mob71TlPGauAIFpc4q3UVBjhbEIyc6vv76Z+RtNd3FZZzsLphzrJB4s6b6TwKpUsIWJ7dXBpkCSVv/sDtB4PeOrzHTH5UHGYkTLbF4o1ie23mbjhIWcSJryJrS+3VMaNuB+waImz/nlJEh/qy0=";
        String privateKeyExponentS="GsCDuPoDJZDd9invyU+2KQRxslmB6CfRSPHM600MWSrojtDoJRjDKSLqzzkcdJwOC9EUHJ4Y6Rw6uJRtaiQsgnDMVCaO2Oy847imP1wCpgSqr8R9y5GT7ZjkFjcEFxmNxkHho7p/LJCtLQUAUIM8LUv0uR0QKW00DAoGaq1m1DCog+PzxRhsJvx6zx6yw7+xwyKd1udXErACRVyvplJ7nFvcfBSC0fp4V3kf1OxUbLAxPVYxiD2LXq6ND3GQY0kT+WueHd5AKSBXv3a21KRXLrhUn22EYsm3oY6pAD/6WiwXdmMpdqgA8NeLOD0O8qbpZogM8VRwuKV4fZZ1fpLeCQ==";

        BigInteger publicKeyModulus = new BigInteger(Base64.decode(publicKeyModulusS));
        BigInteger publicKeyExponent = new BigInteger(Base64.decode(publicKeyExponentS));
        BigInteger privateKeyModulus = new BigInteger(Base64.decode(privateKeyModulusS));
        BigInteger privateKeyExponent = new BigInteger(Base64.decode(privateKeyExponentS));


        Registrar registrarInput = new Registrar(publicKeyModulus, publicKeyExponent, privateKeyModulus, privateKeyExponent);
        String json = gson.toJson(registrarInput);
        prefsEditor.putString("registrar", json);
        prefsEditor.commit();

        json = dataStore.getString("registrar", "");
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
