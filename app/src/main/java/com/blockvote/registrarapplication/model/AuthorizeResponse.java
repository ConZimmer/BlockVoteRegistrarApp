package com.blockvote.registrarapplication.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beast Mode on 1/21/2017.
 */

public class AuthorizeResponse {

    private String response;
    private ErrorResponseModel error;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public ErrorResponseModel getError() {
        return error;
    }

    public void setError(ErrorResponseModel error) {
        this.error = error;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}