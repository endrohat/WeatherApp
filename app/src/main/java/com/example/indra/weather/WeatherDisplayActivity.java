package com.example.indra.weather;


import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.indra.models.WeatherData;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherDisplayActivity extends AppCompatActivity implements WeatherViewContract {

    @BindView(R.id.cityNameView) TextView cityName;
    @BindView(R.id.weatherTypeView) TextView weatherType;
    @BindView(R.id.weatherIconView) ImageView weatherIcon;
    @BindView(R.id.temperatureView) TextView temperature;
    private WeatherDisplayLogic weatherDisplayLogic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_display);
        ButterKnife.bind(this);
        weatherDisplayLogic = new WeatherDisplayLogic(this, this);
        weatherDisplayLogic.init();

    }

    @Override
    public void showErrorAndLastData(String weatherDataString) {
        Toast.makeText(WeatherDisplayActivity.this, R.string.fetch_error, Toast.LENGTH_SHORT).show();
        if(!"".equals(weatherDataString)) {
            updateUI(weatherDataString);
        }
    }

    @Override
    public void updateUI(String weatherDataString) {
        WeatherData weatherData = weatherDisplayLogic.getGson().fromJson(weatherDataString, WeatherData.class);

        cityName.setText(weatherData.getName());
        weatherType.setText(weatherData.getWeather().get(0).getMain() );
        temperature.setText(weatherData.getMain().getTemp().intValue() + " c");

        Resources res = getResources();
        String mDrawableName =  weatherDisplayLogic.getWeatherIconMap().get(weatherData.getWeather().get(0).getIcon());
        int resID = res.getIdentifier(mDrawableName , "drawable", getPackageName());
        weatherIcon.setImageResource(resID);
        Log.i(TAG, weatherData.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                weatherDisplayLogic.refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
