package com.blockvote.registrarapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class RegistrationActivity extends AppCompatActivity
        implements VotingApplicantListFragment.OnListEntryClickedListener,
        ApplicantAcceptanceFragment.OnFragmentButtonPressedListener{

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

            // Create a new Fragment to be placed in the activity layout
            VotingApplicantListFragment voterAppFrag = new VotingApplicantListFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            voterAppFrag.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_registration, voterAppFrag ).commit();
        }
        /*TextView TV = (TextView)findViewById(R.id.RegTextView);
        TV.setText(message);*/
    }

    @Override
    public void onFragmentButtonPushed(String buttonPressed) {
        Log.d("Reg", buttonPressed);
    }

    @Override
    public void OnListEntryClicked(VoterApplicant voterSelected) {
        ApplicantAcceptanceFragment applicantAcceptFrag =
                ApplicantAcceptanceFragment.newInstance(voterSelected.getFirstName(),
                        voterSelected.getLastName());
        FragmentTransaction transaction =  getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_registration, applicantAcceptFrag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
