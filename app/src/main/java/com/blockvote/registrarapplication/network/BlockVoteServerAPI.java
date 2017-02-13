package com.blockvote.registrarapplication.network;

import com.blockvote.registrarapplication.model.AuthorizeRequest;
import com.blockvote.registrarapplication.model.AuthorizeResponse;
import com.blockvote.registrarapplication.model.ElectionListModel;
import com.blockvote.registrarapplication.model.FullElectionInfoModel;
import com.blockvote.registrarapplication.model.RegisterVoterModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Headers;

/**
 * Created by Beast Mode on 1/21/2017.
 */

public interface BlockVoteServerAPI {

    @Headers({"Accept: application/json"})
    @GET("elections/")
    Call<ElectionListModel> getElectionList();

    @Headers({"Accept: application/json"})
    @GET("getElectionInfo/")
    Call<FullElectionInfoModel> getElectionInfo();

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("authorizeUser/")
    Call<AuthorizeResponse> authorizeUser(@Body AuthorizeRequest authorizeRequest);

    @FormUrlEncoded
    @POST("registerVoter/")
    Call<RegisterVoterModel> registerVoter(@Field("region") String region, @Field("govID") String govID,
                                    @Field("registrarName") String registrarName);
}
