package com.example.indra.weather;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


/* Rest api contract */
public interface WeatherService {

    @GET("/data/2.5/weather?APPID=<YOUR_APPID>&units=metric")
    Call<String> getWeatherData(@Query("lat") double latitude, @Query("lon") double longitude);

}
