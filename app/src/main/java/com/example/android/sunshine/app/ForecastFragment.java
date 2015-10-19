package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin on 9/27/2015.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mforecastAdapter;
    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String[] forecastArray={"today-sunny 88/33",
                "tomorrow-sunny 89/44",
                "tomorrow-sunny 89/44",
                "tomorrow-sunny 89/44"
        };

        List<String> weekForecast=new ArrayList<String>(Arrays.asList(forecastArray));
        mforecastAdapter =new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textView,
                weekForecast);

        final ListView listView =(ListView)rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mforecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Context context = getActivity().getApplicationContext();
//                CharSequence text = mforecastAdapter.getItem(position);
//                int duration = Toast.LENGTH_SHORT;
//
//                Toast toast = Toast.makeText(context, text, duration);
//                toast.show();
                Intent detailActivityIntent = new Intent(getActivity(), DetailActivity.class);
                detailActivityIntent.putExtra(Intent.EXTRA_TEXT, mforecastAdapter.getItem(position));
                startActivity(detailActivityIntent);
            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {

        inflater.inflate(R.menu.forecastfragment, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateWeatherForecast();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeatherForecast();
    }

    private void updateWeatherForecast() {
      FetchWeatherTask weatherTask=new FetchWeatherTask(getActivity(),mforecastAdapter);
      SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
      String location=sharedPreferences.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_defaultValue));
      weatherTask.execute(location);
    }

}