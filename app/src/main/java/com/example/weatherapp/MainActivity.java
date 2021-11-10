package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private String url = "https://api.openweathermap.org/data/2.5/onecall?lat=30.267153&lon=-97.743057&exclude=minutely&appid=0256881a8401e8d56197a5cb1089b0b0&units=imperial";
    private RequestQueue rq;
    private TextView desc, temp, feelsLike, wind, pressure, humidity;
    private TextView[] days, times, temps;
    private final String[] dayOfWeek = {"SUN","MON","TUE","WED","THU","FRI","SAT"};
    private ImageView icon;
    private ImageView[] icons;
    private boolean imperial = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rq = Volley.newRequestQueue(this);
        temp = findViewById(R.id.temp);
        feelsLike = findViewById(R.id.feelsLike);
        wind = findViewById(R.id.windSpeed);
        pressure = findViewById(R.id.pressure);
        humidity = findViewById(R.id.humidity);
        desc = findViewById(R.id.desc);
        icon = findViewById(R.id.icon);
        LinearLayout l = findViewById(R.id.horizontalLayout);
        days = new TextView[l.getChildCount()];
        for (int i = 0; i < l.getChildCount(); i++) {
            days[i] = (TextView)(l.getChildAt(i));
        }
        TableLayout t = findViewById(R.id.table);
        times = new TextView[t.getChildCount()-1];
        temps = new TextView[t.getChildCount()-1];
        icons = new ImageView[t.getChildCount()-1];
        for (int i = 1; i < t.getChildCount(); i++) {
            times[i-1] = (TextView)((TableRow)t.getChildAt(i)).getChildAt(0);
            icons[i-1] = (ImageView)((TableRow)t.getChildAt(i)).getChildAt(1);
            temps[i-1] = (TextView)((TableRow)t.getChildAt(i)).getChildAt(2);
        }
        Button units = findViewById(R.id.units);
        units.setOnClickListener(v -> {
            Log.i("App", "units button has been pressed");
            if (imperial) {
                units.setText("C / F");
                url = url.substring(0, url.length() - 8) + "metric";
                imperial = false;
            } else {
                units.setText("F / C");
                url = url.substring(0, url.length() - 6) + "imperial";
                imperial = true;
            }
            Log.i("App", url);
            updateFields();
        });
        updateFields();
    }

    public void updateFields() {
        JsonObjectRequest obreq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject current = response.getJSONObject("current");
                    desc.setText(current.getJSONArray("weather").getJSONObject(0).getString("main"));
                    System.out.println(desc.getText());
                    temp.setText(Math.round(current.getDouble("temp"))+"°");
                    feelsLike.setText("Feels Like: " + Math.round(current.getDouble("feels_like"))+ "°");
                    wind.setText("Wind: " + Math.round(current.getDouble("wind_speed"))+(imperial ? "mph" : "m/s"));
                    pressure.setText("Pressure: " + Math.round(current.getDouble("pressure"))+"hPa");
                    humidity.setText("Humidity: " + Math.round(current.getDouble("humidity"))+"%");
                    Glide.with(icon).load("https://openweathermap.org/img/wn/" + current.getJSONArray("weather").getJSONObject(0).getString("icon") + "@2x.png").into(icon);
                    JSONArray daily = response.getJSONArray("daily");
                    Date d = new Date();
                    for (int i = 0; i < days.length; i++) {
                        days[i].setText(dayOfWeek[(d.getDay() + i + 1) % 7] + "\nH: " + Math.round(daily.getJSONObject(i+1).getJSONObject("temp").getDouble("max")) + "°" +
                                                                          "\nL: " + Math.round(daily.getJSONObject(i+1).getJSONObject("temp").getDouble("min")) + "°");
                    }
                    JSONArray hourly = response.getJSONArray("hourly");
                    times[0].setText("Now");
                    temps[0].setText(temp.getText());
                    Glide.with(icons[0]).load("https://openweathermap.org/img/wn/" + current.getJSONArray("weather").getJSONObject(0).getString("icon") + "@2x.png").into(icons[0]);
                    for (int i = 1; i < temps.length; i++) {
                        times[i].setText(((d.getHours() + i - 1) % 12) + 1 + ":00" + ((d.getHours() + i) % 24 > 11 ? "PM" : "AM"));
                        temps[i].setText(Math.round(hourly.getJSONObject(i).getDouble("temp")) + "°");
                        Glide.with(icons[i]).load("https://openweathermap.org/img/wn/" + hourly.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon") + "@2x.png").into(icons[i]);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        rq.add(obreq);
    }
}