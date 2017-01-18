package com.blockvote.registrarapplication;

/**
 * Created by Connor on 1/16/2017.
 */

public class VoterApplicant {
    private String firstName;
    private String lastName;

    public VoterApplicant(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    @Override
    public String toString(){
        return firstName + " " + lastName;
    }


}
