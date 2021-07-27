package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    final String API_ID = "b43e6908cd380f9eb818d39de93d5598";
    String city = "Cairo";
    ProgressBar progressBar;
    TextView tvAddress, tvDate, tvStatus, tvTemp, tvMinTemp, tvMaxTemp, tvSunrise, tvSunset, tvWind, tvPressure, tvHumidity;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        tvAddress = findViewById(R.id.address);
        tvDate = findViewById(R.id.updated_at);
        tvStatus = findViewById(R.id.status);
        tvTemp = findViewById(R.id.temp);
        tvMinTemp = findViewById(R.id.temp_min);
        tvMaxTemp = findViewById(R.id.temp_max);
        tvSunrise = findViewById(R.id.sunrise);
        tvSunset = findViewById(R.id.sunset);
        tvPressure = findViewById(R.id.pressure);
        tvWind = findViewById(R.id.wind);
        tvHumidity = findViewById(R.id.humidity);
        progressBar = findViewById(R.id.loader);


        getPermission();
        checkLocationIsEnableOrNot();
//        getLocation();


        progressBar.setVisibility(View.VISIBLE);
        Ion.with(getBaseContext())
                .load("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_ID)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if (e != null) {
                            progressBar.setVisibility(View.GONE);

                        }


                        JsonObject sys = result.get("sys").getAsJsonObject();
                        String country = sys.get("country").getAsString();
                        String address = city + "," + country;

                        tvAddress.setText(address);


                        long sunrise = sys.get("sunrise").getAsLong() * 1000L;
                        Date sunriseDate = new Date(sunrise);
                        long sunset = sys.get("sunset").getAsLong();
                        Date sunsetDate = new Date(sunset);
                        tvSunrise.setText(sunriseDate.toString().subSequence(11, 16));
                        tvSunset.setText(sunsetDate.toString().subSequence(11, 16));


                        //tvDate


                        JsonArray weather = result.get("weather").getAsJsonArray();
                        String status = weather.get(0).getAsJsonObject().get("main").getAsString();
                        tvStatus.setText(status);


                        JsonObject main = result.get("main").getAsJsonObject();
                        int temp = main.get("temp").getAsInt();

                        tvTemp.setText(temp + "°C");


                        String temp_min = main.get("temp_min").getAsString();
                        tvMinTemp.setText("Min Temp: " + temp_min + "°C");


                        String temp_max = main.get("temp_max").getAsString();
                        tvMaxTemp.setText("Max Temp: " + temp_max + "°C");


                        String pressure = main.get("pressure").getAsString();
                        tvPressure.setText(pressure);


                        String humidity = main.get("humidity").getAsString();
                        tvHumidity.setText(humidity);

                        JsonObject wind = result.get("wind").getAsJsonObject();
                        String windSpeed = wind.get("speed").getAsString();
                        tvWind.setText(windSpeed);


                        tvDate.setText(new Date().toString().subSequence(0, 20));
                        progressBar.setVisibility(View.GONE);

                    }

                });

    }

    private void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, (android.location.LocationListener) this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void checkLocationIsEnableOrNot() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false, networkEnabled = false;
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gpsEnabled && !networkEnabled) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Enable Gps Service")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).show();

        }
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 150);

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            city = addresses.get(0).getCountryName();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}