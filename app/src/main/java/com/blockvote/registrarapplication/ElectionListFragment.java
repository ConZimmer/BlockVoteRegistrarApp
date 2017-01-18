package com.blockvote.registrarapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ElectionListFragment extends Fragment {
    public final static String EXTRA_MESSAGE = "com.blockvote.registrarapp.MESSAGE";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_election_list, container, false);
        //final ListView listview = (ListView) R.findViewById(R.id.Elections_Listview);

        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 20; ++i) {
            list.add("Election " + i);
        }
        final ArrayAdapter adap = new ArrayAdapter<String>(
                this.getActivity(),
                R.layout.electionentry,
                R.id.textview_electionentry,
                list
        );
        final ListView listview = (ListView) v.findViewById(R.id.Elections_Listview);
        listview.setAdapter(adap);

        listview.setOnItemClickListener( new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                String text= (String)adap.getItem(i);

                //Use toast for debugging
                    //Toast creates a temporary notification when an item in the ListView is clicked.
                   /* Context context = adapterView.getContext();
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(context, text, duration).show();
                    return;*/

                Intent intent = new Intent(getActivity(), RegistrationActivity.class);
                intent.putExtra(EXTRA_MESSAGE, text);
                startActivity(intent);

                //Starting the Election Activity
                /*Intent intent = new Intent(adapterView.getContext(), MainElectionActivity.class);
                //Give the name of the election Selected
                //TODO: Pass an election object instead of a String
                intent.putExtra(getString(R.string.selectedElectionKey), text);
                startActivity(intent);*/

            }
        });

        return v;
    }


}
