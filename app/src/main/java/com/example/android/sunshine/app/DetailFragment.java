package com.example.android.sunshine.app;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG=DetailActivity.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    private static final String FORECAST_SHARE_HASHTAG="#Sunshine";
    private ShareActionProvider mShareActionProvider;
    private final static int DETAIL_LOADER =0;
    private String mForecast;
    private Uri mUri;

    private static final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "."+ WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguements=getArguments();
        if(arguements!=null) {
            mUri = arguements.getParcelable(DETAIL_URI);
        }

        View view=inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView)view.findViewById(R.id.detail_icon);
        mFriendlyDateView=(TextView)view.findViewById(R.id.detail_day_textview);
        mDateView=(TextView)view.findViewById(R.id.detail_date_textview);
        mDescriptionView=(TextView)view.findViewById(R.id.detail_forecast_textview);
        mHighTempView=(TextView)view.findViewById(R.id.detail_high_textview);
        mLowTempView=(TextView)view.findViewById(R.id.detail_low_textview);
        mHumidityView=(TextView)view.findViewById(R.id.detail_humidity_textview);
        mWindView=(TextView)view.findViewById(R.id.detail_wind_textview);
        mPressureView=(TextView)view.findViewById(R.id.detail_pressure_textview);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null,this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.detailfragment, menu);
        MenuItem menuItem=(MenuItem)menu.findItem(R.id.action_share);
        mShareActionProvider=(ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if(mForecast!=null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }

    }

    private Intent createShareForecastIntent(){
        Intent shareIntent =new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast +" "+FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        if (mUri==null) {
            return null;
        }
        return new CursorLoader(getActivity(),
                mUri,
                DETAIL_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }

        int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
        mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        long date = data.getLong(COL_WEATHER_DATE);
        String friendlyDateText = Utility.getDayName(getActivity(), date);
        String dateText = Utility.getFormattedMonthDay(getActivity(), date);
        mFriendlyDateView.setText(friendlyDateText);
        mDateView.setText(dateText);

        String description = data.getString(COL_WEATHER_DESC);
        mDescriptionView.setText(description);

        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(getActivity().getApplicationContext(), data.getDouble(COL_WEATHER_MAX_TEMP));
        mHighTempView.setText(high);
        String low = Utility.formatTemperature(getActivity().getApplicationContext(),data.getDouble(COL_WEATHER_MIN_TEMP));
        mLowTempView.setText(low);

        // Read humidity from cursor and update view
        float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));
        // Read wind speed and direction from cursor and update view
        float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
        float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
        mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));
        // Read pressure from cursor and update view
        float pressure = data.getFloat(COL_WEATHER_PRESSURE);
        mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

        mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);
        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
}
