package com.blockvote.registrarapplication;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by Connor on 1/18/2017.
 */

public interface NetworkInterface {
    @GET("/elecList/")
    public Call<List<String>> getMessage();

    //https://api.github.com/
    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://registartestserver.mybluemix.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
