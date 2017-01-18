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

public class VotingApplicantListFragment extends Fragment {

    private OnListEntryClickedListener listEntryListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_applicant_list, container, false);


        final ArrayList<VoterApplicant> list = new ArrayList<VoterApplicant>();
        for (int i = 0; i < 20; ++i) {
            list.add(new VoterApplicant("firstName " + i, "lastName " + i));
        }
        final ArrayAdapter adap = new ArrayAdapter<VoterApplicant>(
                this.getActivity(),
                R.layout.voter_applicant_entry,
                R.id.vote_applicant_textView,
                list
        );
        final ListView listview = (ListView) rootView.findViewById(R.id.applicant_list);
        listview.setAdapter(adap);

        listview.setOnItemClickListener( new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                VoterApplicant voterSelected  = (VoterApplicant)adap.getItem(i);
                listEntryListener.OnListEntryClicked(voterSelected);
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VotingApplicantListFragment.OnListEntryClickedListener) {
            listEntryListener = (VotingApplicantListFragment.OnListEntryClickedListener) context;
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

    public interface OnListEntryClickedListener{
        public void OnListEntryClicked(VoterApplicant voterSelected);
    }
}
