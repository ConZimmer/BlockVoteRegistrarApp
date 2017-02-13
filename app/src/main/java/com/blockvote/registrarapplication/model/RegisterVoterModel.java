package com.blockvote.registrarapplication.model;

import java.util.HashMap;
import java.util.Map;


public class RegisterVoterModel {

    private registerVoter_response response;
    //private ErrorResponseModel error;
    private Object error;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public registerVoter_response getResponse() {
        return response;
    }

    public void setResponse(registerVoter_response response) {
        this.response = response;
    }
/*
    public ErrorResponseModel getError() {
        return error;
    }

    public void setError(ErrorResponseModel error) {
        this.error = error;
    }
*/
    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}

