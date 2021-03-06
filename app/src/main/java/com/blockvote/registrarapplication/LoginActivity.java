package com.blockvote.registrarapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.lock.AuthenticationCallback;
import com.auth0.android.lock.Lock;
import com.auth0.android.lock.LockCallback;
import com.auth0.android.lock.utils.LockException;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.UserProfile;
import com.blockvote.registrarapplication.crypto.Registrar;
import com.blockvote.registrarapplication.qrCode.GenerateQRActivity;
import com.blockvote.registrarapplication.qrCode.ReadQRActivity;
import com.google.gson.Gson;

import android.content.SharedPreferences;

import org.spongycastle.util.encoders.Base64;

import java.math.BigInteger;


public class LoginActivity extends Activity {

    private Lock mLock;
    private Intent lockIntent;
    private Auth0 auth0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth0 = new Auth0(getString(R.string.auth0_client_id), getString(R.string.auth0_domain));
        mLock = Lock.newBuilder(auth0, mCallback)
                //Add parameters to the builder
                .build(this);
        startActivity(mLock.newIntent(this));

        lockIntent = new Intent(this, MainActivity.class);
        lockIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Your own Activity code
        mLock.onDestroy(this);
        mLock = null;
    }

    private final LockCallback mCallback = new AuthenticationCallback() {
        @Override
        public void onAuthentication(Credentials credentials) {
            Toast.makeText(getApplicationContext(), "Log In - Success", Toast.LENGTH_SHORT).show();

            AuthenticationAPIClient client = new AuthenticationAPIClient(auth0);

            SharedPreferences sharedPref = getSharedPreferences("RegistrarData", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPref.edit();

            client.tokenInfo(credentials.getIdToken())
                    .start(new BaseCallback<UserProfile, AuthenticationException>() {
                        @Override
                        public void onSuccess(UserProfile payload){

                            BigInteger publicKeyModulus = new BigInteger(Base64.decode(payload.getUserMetadata().get("publicKeyModulus").toString()));
                            BigInteger publicKeyExponent = new BigInteger(Base64.decode(payload.getUserMetadata().get("publicKeyExponent").toString()));
                            BigInteger privateKeyModulus = new BigInteger(Base64.decode(payload.getUserMetadata().get("privateKeyModulus").toString()));
                            BigInteger privateKeyExponent = new BigInteger(Base64.decode(payload.getUserMetadata().get("privateKeyExponent").toString()));
                            String registrarName = payload.getUserMetadata().get("name").toString();
                            String serverURL = payload.getUserMetadata().get("url").toString();

                            Gson gson = new Gson();
                            Registrar registrarInput = new Registrar(publicKeyModulus, publicKeyExponent, privateKeyModulus, privateKeyExponent, registrarName, serverURL);
                            String json = gson.toJson(registrarInput);
                            editor.putString("registrar", json);
                            editor.commit();

                            startActivity(lockIntent);
                        }

                        @Override
                        public void onFailure(AuthenticationException error){
                        }
                    });

            editor.putString("access_token", credentials.getAccessToken());
            editor.putString("token_type", credentials.getType());
            editor.putString("id_token", credentials.getIdToken());
            editor.putString("refresh_token", credentials.getRefreshToken());
            editor.apply();

            //startActivity(lockIntent);

        }

        @Override
        public void onCanceled() {
            Toast.makeText(getApplicationContext(), "Log In - Cancelled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(LockException error) {
            Toast.makeText(getApplicationContext(), "Log In - Error Occurred", Toast.LENGTH_SHORT).show();
        }
    };

}


