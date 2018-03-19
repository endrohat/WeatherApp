package com.example.indra.weather;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import butterknife.ButterKnife;

import static junit.framework.Assert.assertEquals;

/**
 * Created by indra on 19/03/18.
 */

@RunWith(RobolectricTestRunner.class)
public class WeatherDisplayActivityTest {

    @Test
    public void shouldUpdateUICorrectlyWithTestData() throws IOException {
        WeatherDisplayActivity activity = Robolectric.setupActivity(WeatherDisplayActivity.class);
        ButterKnife.bind(this, activity);

        File f = new File("app/src/test/testdata/testResponse.json");
        BufferedReader b = new BufferedReader(new FileReader(f));
        String fileContent = "";
        String readLine = "";
        while ((readLine = b.readLine()) != null) {
            fileContent+=readLine;
        }

        activity.updateUI(fileContent);
        assertEquals("10 c", activity.temperature.getText().toString() );
        assertEquals("Rain", activity.weatherType.getText().toString() );
        assertEquals("Shuzenji", activity.cityName.getText().toString() );

        int drawableResId = Shadows.shadowOf(activity.weatherIcon.getDrawable()).getCreatedFromResId();
        assertEquals(R.drawable.rainy_night, drawableResId);

    }
}
