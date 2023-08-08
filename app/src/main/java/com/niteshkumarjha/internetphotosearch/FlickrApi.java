package com.niteshkumarjha.internetphotosearch;

import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface FlickrApi {

    @GET("?method=flickr.photos.")
    Call<JsonObject> getPhotos(@QueryMap Map<String, String> parameters);
}
