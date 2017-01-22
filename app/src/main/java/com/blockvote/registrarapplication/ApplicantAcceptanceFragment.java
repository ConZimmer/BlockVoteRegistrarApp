package com.blockvote.registrarapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnApplicantAcceptanctButtonPressedListener} interface
 * to handle interaction events.
 * Use the {@link ApplicantAcceptanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ApplicantAcceptanceFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    /*private static final String ARG_applicant_first_name = "applicant_first_name";
    private static final String ARG_applicant_last_name = "applicant_last_name";

    private String applicantFirstName = "";
    private String applicantLastName = "";*/

    private static final String ARG_applicant = "applicant";

    private VoterApplicant applicant;

    private OnApplicantAcceptanctButtonPressedListener mListener;

    public ApplicantAcceptanceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param  applicant the voter applicant that has been selected.
     * @return A new instance of fragment ApplicantAcceptanceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ApplicantAcceptanceFragment newInstance(VoterApplicant applicant) {
        ApplicantAcceptanceFragment fragment = new ApplicantAcceptanceFragment();
        Bundle args = new Bundle();
        /*args.putString(ARG_applicant_first_name, firstName);
        args.putString(ARG_applicant_last_name, lastName);*/
        args.putSerializable(ARG_applicant,applicant);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            /*applicantFirstName = getArguments().getString(ARG_applicant_first_name);
            applicantLastName = getArguments().getString(ARG_applicant_last_name);*/
            applicant = (VoterApplicant)getArguments().getSerializable(ARG_applicant);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_applicant_acceptance, container, false);
        view.findViewById(R.id.accept_button).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                acceptButtonPushed();
            }
        });

        view.findViewById(R.id.reject_button).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                rejectButtonPushed();
            }
        });
        TextView firstNameTV = (TextView)view.findViewById(R.id.applicant_first_name);
        firstNameTV.setText(applicant.getFirstName());
        TextView lastNameTV = (TextView)view.findViewById(R.id.applicant_last_name);
        lastNameTV.setText(applicant.getLastName());
        return view;
    }

    public void buttonPressed(String buttonPressed) {
        if (mListener != null) {
            mListener.onApplicantAcceptanceButtonPushed(buttonPressed);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnApplicantAcceptanctButtonPressedListener) {
            mListener = (OnApplicantAcceptanctButtonPressedListener) context;
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

    public void acceptButtonPushed() {
        buttonPressed("accept");
    }

    public void rejectButtonPushed() {
        buttonPressed("reject");
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
    public interface OnApplicantAcceptanctButtonPressedListener {
        // TODO: Update argument type and name
        void onApplicantAcceptanceButtonPushed(String buttonPressed);
    }
}
