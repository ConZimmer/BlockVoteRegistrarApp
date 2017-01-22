package com.blockvote.registrarapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegistrationActivity extends AppCompatActivity
        implements VotingApplicantListFragment.OnVoterApplicantClickedListener,
        ApplicantAcceptanceFragment.OnApplicantAcceptanctButtonPressedListener {

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

            NetworkInterface networkingInterface = NetworkInterface.retrofit.create(NetworkInterface.class);
            Call<List<VoterApplicant>> call = networkingInterface.getApplicants(ElectionSelected);
            call.enqueue(new Callback<List<VoterApplicant>>() {

                 @Override
                 public void onResponse(Call<List<VoterApplicant>> call, Response<List<VoterApplicant>> response) {
                     if (400 <= response.code() && response.code() < 600) {
                         Toast toast = Toast.makeText(getApplicationContext(), "Problem connecting to server, code: "
                                 + response.code(), Toast.LENGTH_LONG);
                         toast.show();
                         return;
                     }
                     VotingApplicantListFragment voterAppFrag =
                             VotingApplicantListFragment.newInstance(response.body());
                     getSupportFragmentManager().beginTransaction()
                             .add(R.id.activity_registration, voterAppFrag).commit();
                 }

                 @Override
                 public void onFailure(Call<List<VoterApplicant>> call, Throwable t) {
                 }
             });

            // Create a new Fragment to be placed in the activity layout
           /* VotingApplicantListFragment voterAppFrag = new VotingApplicantListFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            voterAppFrag.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_registration, voterAppFrag ).commit();*/
        }
        /*TextView TV = (TextView)findViewById(R.id.RegTextView);
        TV.setText(message);*/
    }

    @Override
    public void onApplicantAcceptanceButtonPushed(String buttonPressed) {
        ConfirmChoiceFragment confirmChoiceFragment =
                ConfirmChoiceFragment.newInstance(voterSelected, buttonPressed);
        FragmentTransaction transaction =  getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_registration, confirmChoiceFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void OnVoterApplicantClicked(VoterApplicant voterSelected) {
        this.voterSelected = voterSelected;
        ApplicantAcceptanceFragment applicantAcceptFrag =
                ApplicantAcceptanceFragment.newInstance(voterSelected);
        FragmentTransaction transaction =  getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_registration, applicantAcceptFrag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
