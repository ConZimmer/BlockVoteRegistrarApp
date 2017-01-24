package com.blockvote.registrarapplication.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dave on 1/23/2017.
 */

public class AuthorizeRequest {
    private String username;

    private String voter;

    private String req;

    public AuthorizeRequest(String inputUsername, String inputVoter, String inputReq){
        username = inputUsername;
        voter = inputVoter;
        req = inputReq;
    }

}
