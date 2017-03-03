package com.blockvote.registrarapplication;

/**
 * Created by Michael Heiber on 3/2/2017.
 */

import android.os.Bundle;
        import android.support.v4.app.Fragment;
        import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MyProgressBar extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.progress_bar, container, false);
    }

    public void setProgress(int value)
    {

    }
}