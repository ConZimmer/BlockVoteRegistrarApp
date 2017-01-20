package com.blockvote.registrarapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ElectionListFragment extends Fragment {
    public final static String ELECTION_SELECTED_KEY =
            "com.blockvote.registrarapp.ELECTION_SELECTED_KEY";

    public final static String ARG_election_list = "election_list";

    private ArrayList<String> electionList = new ArrayList<String>();

    public static ElectionListFragment newInstance(List<String> electionList) {
        ElectionListFragment fragment = new ElectionListFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_election_list, new ArrayList<String>(electionList));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            electionList = getArguments().getStringArrayList(ARG_election_list);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_election_list, container, false);

       /* final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 20; ++i) {
            list.add("Election " + i);
        }*/
        final ArrayAdapter adap = new ArrayAdapter<String>(
                this.getActivity(),
                R.layout.electionentry,
                R.id.textview_electionentry,
                electionList
        );
        final ListView listview = (ListView) v.findViewById(R.id.Elections_Listview);
        listview.setAdapter(adap);

        listview.setOnItemClickListener( new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                String text= (String)adap.getItem(i);

                Intent intent = new Intent(getActivity(), RegistrationActivity.class);
                intent.putExtra(ELECTION_SELECTED_KEY, text);
                startActivity(intent);

            }
        });

        return v;
    }


}
