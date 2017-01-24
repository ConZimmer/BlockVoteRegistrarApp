package com.blockvote.registrarapplication.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dave on 1/23/2017.
 */

public class AuthorizeRequest {

    @SerializedName("username")
    @Expose
    String username;

    @SerializedName("voter")
    @Expose
    String voter;

    @SerializedName("reg")
    @Expose
    private String reg;

    public AuthorizeRequest(String inputUsername, String inputVoter, String inputReg){
        username = inputUsername;
        voter = inputVoter;
        reg = inputReg;
    }

}
