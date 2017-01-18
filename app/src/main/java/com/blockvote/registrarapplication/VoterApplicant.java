package com.blockvote.registrarapplication;

import java.io.Serializable;

public class VoterApplicant{
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
