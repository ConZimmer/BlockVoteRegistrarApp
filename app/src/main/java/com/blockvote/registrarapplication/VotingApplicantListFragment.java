package com.blockvote.registrarapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class VotingApplicantListFragment extends Fragment {

    public final static String ARG_applicant_list = "applicant_list";

    private ArrayList<VoterApplicant> applicantList = new ArrayList<VoterApplicant>();

    private OnVoterApplicantClickedListener listEntryListener;

    public static VotingApplicantListFragment newInstance(List<VoterApplicant> applicantList) {
        VotingApplicantListFragment fragment = new VotingApplicantListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_applicant_list, new ArrayList<VoterApplicant>(applicantList));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            applicantList = (ArrayList<VoterApplicant>)getArguments()
                    .getSerializable(ARG_applicant_list);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_applicant_list, container, false);

        /*
        final ArrayList<VoterApplicant> list = new ArrayList<VoterApplicant>();
        for (int i = 0; i < 20; ++i) {
            list.add(new VoterApplicant("firstName " + i, "lastName " + i));
        }*/
        final ArrayAdapter adap = new ArrayAdapter<VoterApplicant>(
                this.getActivity(),
                R.layout.voter_applicant_entry,
                R.id.vote_applicant_textView,
                applicantList
        );
        final ListView listview = (ListView) rootView.findViewById(R.id.applicant_list);
        listview.setAdapter(adap);

        listview.setOnItemClickListener( new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                VoterApplicant voterSelected  = (VoterApplicant)adap.getItem(i);
                listEntryListener.OnVoterApplicantClicked(voterSelected);
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VotingApplicantListFragment.OnVoterApplicantClickedListener) {
            listEntryListener = (VotingApplicantListFragment.OnVoterApplicantClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement VotingApplicantListFragment.OnListEntryClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listEntryListener = null;
    }

    public interface OnVoterApplicantClickedListener{
        public void OnVoterApplicantClicked(VoterApplicant voterSelected);
    }

}
