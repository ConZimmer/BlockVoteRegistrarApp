package com.blockvote.registrarapplication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConfirmChoiceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConfirmChoiceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfirmChoiceFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_applicant_fName = "applicant_LName";
    private static final String ARG_applicant_lName = "applicant_fName";
    private static final String ARG_registrar_choice = "registrar_choice";

    // TODO: Rename and change types of parameters
    private String applicantFName;
    private String applicantLName;
    private String registrarChoice;

//    private OnFragmentInteractionListener mListener;

    public ConfirmChoiceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param appFirstName the first name of the applicant.
     * @param appLName the last name of the applicant.
     * @param registrarChoice the choice made by the registrar.
     * @return A new instance of fragment ConfirmChoiceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConfirmChoiceFragment newInstance(String appFirstName, String appLName, String registrarChoice) {
        ConfirmChoiceFragment fragment = new ConfirmChoiceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_applicant_fName, appFirstName);
        args.putString(ARG_applicant_lName, appLName);
        args.putString(ARG_registrar_choice, registrarChoice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            applicantFName = getArguments().getString(ARG_applicant_fName);
            applicantLName = getArguments().getString(ARG_applicant_lName);
            registrarChoice = getArguments().getString(ARG_registrar_choice);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_confirm_choice, container, false);
        TextView confirmChoiceTV = (TextView)rootView.findViewById(R.id.confirm_choice_TextView);
        confirmChoiceTV.setText("Are you sure you want to " + registrarChoice
                + " " + applicantFName + " " + applicantLName);
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
/*    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
   /* public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
