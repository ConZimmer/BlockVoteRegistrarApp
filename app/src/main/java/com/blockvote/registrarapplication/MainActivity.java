package com.blockvote.registrarapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                }
            });
        }
    }
}
