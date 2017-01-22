package com.blockvote.registrarapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.lock.AuthenticationCallback;
import com.auth0.android.lock.Lock;
import com.auth0.android.lock.LockCallback;
import com.auth0.android.lock.utils.LockException;
import com.auth0.android.result.Credentials;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Lock lock;

    private LockCallback callback = new AuthenticationCallback() {
        @Override
        public void onAuthentication(Credentials credentials) {
            // Login Success response
        }

        @Override
        public void onCanceled() {
            // Login Cancelled response
        }

        @Override
        public void onError(LockException error){
            // Login Error response
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Auth0 auth0 = new Auth0("f2pQL6jMgGQLDsNlHfhQgsmMVGzMcgmg", "enel500blockvote.auth0.com");
        lock = Lock.newBuilder(auth0, callback)
                // Add parameters to the Lock Builder
                .build(this);

        startActivity(lock.newIntent(this));
        if (findViewById(R.id.activity_main) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            NetworkInterface networkingInterface = NetworkInterface.retrofit.create(NetworkInterface.class);
            Call<List<String>> call = networkingInterface.getElectionList();
            call.enqueue(new Callback<List<String>>() {

                @Override
                public void onResponse(Call<List<String>> call, Response<List<String>> response){
                    if(400 <= response.code()  && response.code() < 600)
                    {
                        Toast toast=Toast.makeText(getApplicationContext(),"Problem connecting to server, code: "
                                + response.code(),Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }
                    ElectionListFragment electionListFrag =
                            ElectionListFragment.newInstance(response.body());
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.activity_main, electionListFrag).commit();
                }

                @Override
                public void onFailure(Call<List<String>> call, Throwable t) {
                    Toast toast=Toast.makeText(getApplicationContext(),"Failure when getting election list"
                        ,Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Your own Activity code
        lock.onDestroy(this);
        lock = null;
    }
}
