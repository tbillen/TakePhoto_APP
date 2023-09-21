package com.takephoto.vendor.apirequest;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("/")
    Call<GetUser> getUser(@Field("action") String action, @Field("name") String name, @Field("pw") String pw);

    @FormUrlEncoded
    @POST("/")
    Call<SetImage> setImage(@Field("action") String action, @Field("userID") String userID, @Field("image") String image);
}