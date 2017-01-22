package com.blockvote.registrarapplication;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Connor on 1/18/2017.
 */

public interface NetworkInterface {
    @GET("/elecList/")
    public Call<List<String>> getElectionList();

    @FormUrlEncoded
    @POST("/applicants/")
    public Call<List<VoterApplicant>> getApplicants(@Field("election") String election);

    //https://blockvotenode2.mybluemix.net/
    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://registartestserver.mybluemix.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
