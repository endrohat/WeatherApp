package com.example.indra.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;


/* Logic class for weather display */
public class WeatherDisplayLogic implements PermissionListener {

    private Activity activity;
    private WeatherService weatherService;
    private SharedPreferences sharedPref;

    private Map<String,String> weatherIconMap;

    private Gson gson;
    private ConnectivityManager cm;
    private static final String BASE_URL = "http://api.openweathermap.org/";
    private FusedLocationProviderClient fusedLocationClient;
    private WeatherViewContract weatherViewContract;

    WeatherDisplayLogic(Activity activity, WeatherViewContract weatherViewContract) {
        this.activity = activity;
        this.weatherViewContract = weatherViewContract;
    }

    void init() {
        sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        weatherIconMap = new HashMap<>();
        buildWeatherDataMap(weatherIconMap);

        cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();

        weatherService = retrofit.create(WeatherService.class);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);


        TedPermission.with(activity)
                .setPermissionListener(this)
                .setDeniedMessage(R.string.permission_denied)
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();

    }

    public Map<String, String> getWeatherIconMap() {
        return weatherIconMap;
    }

    public Gson getGson() {
        return gson;
    }

    private static void buildWeatherDataMap(Map<String,String> weatherIconMap) {
        weatherIconMap.put("01d","sunny");
        weatherIconMap.put("01n","clear_night");
        weatherIconMap.put("02d","cloudy_day");
        weatherIconMap.put("02d","cloudy_night");
        weatherIconMap.put("03d","clouds");
        weatherIconMap.put("03n","clouds");
        weatherIconMap.put("04d","clouds");
        weatherIconMap.put("04n","clouds");
        weatherIconMap.put("09d","shower_rain");
        weatherIconMap.put("09n","shower_rain");
        weatherIconMap.put("10d","rainy_day");
        weatherIconMap.put("10n","rainy_night");
        weatherIconMap.put("11d","thunderstorm");
        weatherIconMap.put("11n","thunderstorm");
        weatherIconMap.put("13d","snow");
        weatherIconMap.put("13n","snow");
        weatherIconMap.put("50d","mist");
        weatherIconMap.put("50n","mist");
    }

    @SuppressLint("MissingPermission")
    void refresh() {
        if(!isConnected()) {
            handleError();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            weatherService.getWeatherData(location.getLatitude(),location.getLongitude()).enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if(response.isSuccessful()) {
                                        String weatherDataString = response.body();

                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString("weatherData", weatherDataString);
                                        editor.commit();

                                        weatherViewContract.updateUI(weatherDataString);
                                    } else {
                                        Log.e(WeatherViewContract.TAG, response.message());
                                        handleError();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Log.e(WeatherViewContract.TAG, t.getMessage());
                                    handleError();
                                }
                            });
                        } else {
                            handleError();
                        }
                    }
                });
    }

    private void handleError() {
        String weatherDataString = sharedPref.getString("weatherData","");
        if(!"".equals(weatherDataString)) {
            weatherViewContract.showErrorAndLastData(weatherDataString);
        }
    }

    @Override
    public void onPermissionGranted() {
        refresh();
    }

    @Override
    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
        Log.e(WeatherViewContract.TAG, "permission denied");
    }

    private boolean isConnected() {
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
