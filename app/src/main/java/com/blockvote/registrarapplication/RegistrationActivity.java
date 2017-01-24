package com.blockvote.registrarapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.CheckBox;

import com.blockvote.registrarapplication.model.AuthorizeRequest;
import com.blockvote.registrarapplication.model.AuthorizeResponse;
import com.blockvote.registrarapplication.model.ElectionListModel;
import com.blockvote.registrarapplication.network.BlockVoteServerAPI;
import com.blockvote.registrarapplication.network.BlockVoteServerInstance;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegistrationActivity extends AppCompatActivity {

    private VoterApplicant voterSelected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Intent intent = getIntent();
        String ElectionSelected =
                intent.getStringExtra(ElectionListFragment.ELECTION_SELECTED_KEY);

        if (findViewById(R.id.activity_registration) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
        }
    }

        public void registerVoter(View view) {

            EditText editUsername = (EditText) findViewById(R.id.editUsername);
            String username = editUsername.getText().toString();

            EditText editVoter = (EditText) findViewById(R.id.editVoter);
            String voter = editUsername.getText().toString();

            CheckBox checkboxReg = (CheckBox) findViewById(R.id.checkboxReg);
            String reg;
            if(checkboxReg.isChecked()) {
                reg="yes";
            }else {
                reg="no";
            }

            BlockVoteServerInstance blockVoteServerInstance = new BlockVoteServerInstance();
            BlockVoteServerAPI apiService = blockVoteServerInstance.getAPI();

            AuthorizeRequest authorizeRequest= new AuthorizeRequest(username, voter, reg);
            Call<AuthorizeResponse> call = apiService.authorizeUser(authorizeRequest);

            call.enqueue(new Callback<AuthorizeResponse>() {

                @Override
                public void onResponse(Call<AuthorizeResponse> call, Response<AuthorizeResponse> response){
                    if(400 <= response.code()  && response.code() < 600)
                    {
                        Toast toast=Toast.makeText(getApplicationContext(),"Problem connecting to server, code: "
                                + response.code(),Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }
                    Toast toast=Toast.makeText(getApplicationContext(),"Voter approved"
                            ,Toast.LENGTH_LONG);
                    toast.show();

                }

                @Override
                public void onFailure(Call<AuthorizeResponse> call, Throwable t) {
                    Toast toast=Toast.makeText(getApplicationContext(),"Failure when registering voter"
                            ,Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }

        }
