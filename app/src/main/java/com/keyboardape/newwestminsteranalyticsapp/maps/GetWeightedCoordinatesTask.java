package com.keyboardape.newwestminsteranalyticsapp.maps;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GetWeightedCoordinatesTask extends AsyncTask<Void, Void, List<WeightedLatLng>> {

    /**
     * Interface for callback function when task completes.
     */
    public interface OnComplete {
        /**
         * Task to do once list of weighted coordinates are retrieved.
         * @param weightedCoordinates list of WeightedLatLng retrieved, or null
         */
        void doTask(List<WeightedLatLng> weightedCoordinates);
    }


    /** Constants of key names inside JSON files. */
    private class JsonKeynames {
        public static final String NUMBER_OF_RESIDENTS = "NUM_RES";
        public static final String GEOJSON             = "json_geometry";
        public static final String COORDINATES         = "coordinates";
    }

    /** Tag used for logging purposes. */
    private static final String LOG_TAG = GetWeightedCoordinatesTask.class.getSimpleName();

    /** Activity that called this task. */
    private Activity mActivity;

    /** URL to JSON file that will be parsed. */
    private String mJsonUrl;

    /** Callback function for when this task completes, regardless of fail or success. */
    private OnComplete mCallback;

    /**
     * Constructor.
     * @param activity that is calling the task
     * @param jsonUrl  that will be parsed for weighted coordinates
     * @param callback function that will be called once task completes
     */
    public GetWeightedCoordinatesTask(Activity activity, String jsonUrl, OnComplete callback) {
        mActivity = activity;
        mJsonUrl  = jsonUrl;
        mCallback = callback;
    }

    /**
     * Show "loading data" message before task begins.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showToastMessage(mActivity.getString(R.string.toast_msg_loading_data));
    }

    /**
     * Retrieve list of weighted coordinates in the background.
     * @param params not used
     * @return list of WeightedLatLng
     */
    @Override
    protected List<WeightedLatLng> doInBackground(Void... params) {
        try {
            InputStream in = openConnectionAndGetInputStream(mJsonUrl);
            return readJsonStream(in);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }

        return null;
    }

    /**
     * Displays data loading success/fail message and calls callback function.
     * @param weightedCoordinates a list of WeightedLatLng or null
     */
    @Override
    protected void onPostExecute(List<WeightedLatLng> weightedCoordinates) {
        super.onPostExecute(weightedCoordinates);

        int message = (weightedCoordinates == null)
            ? R.string.toast_msg_load_data_failed
            : R.string.toast_msg_load_data_success;

        showToastMessage(mActivity.getString(message));
        mCallback.doTask(weightedCoordinates);
    }

    /**
     * Shows a Toast message on UI thread.
     * @param message to show
     */
    private void showToastMessage(final String message) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Reads specified JSON stream for weighted coordinates.
     * @param in InputStream of JSON file
     * @return list of WeightedLatLng or null
     * @throws IOException
     */
    private List<WeightedLatLng> readJsonStream(InputStream in) throws IOException {
        List<WeightedLatLng> weightedCoordinates = new ArrayList<>();
        JsonReader           reader              = new JsonReader(new InputStreamReader(in, "UTF-8"));

        try {
            reader.beginArray();
            while (reader.hasNext()) {
                WeightedLatLng weightedCoordinate = readWeightedCoordinate(reader);
                if (weightedCoordinate != null) {
                    weightedCoordinates.add(weightedCoordinate);
                }
            }
            reader.endArray();

            return weightedCoordinates;
        } finally {
            reader.close();
            in.close();
        }
    }

    /**
     * Reads a WeightedLatLng from JsonReader.
     * @param reader active JsonReader
     * @return WeightedLatLng or null
     * @throws IOException
     */
    private WeightedLatLng readWeightedCoordinate(JsonReader reader) throws IOException {
        Integer numResidents = null;
        LatLng  coordinate   = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();

            switch(name) {
                case JsonKeynames.NUMBER_OF_RESIDENTS:
                    if (reader.peek().equals(JsonToken.NULL)) {
                        reader.skipValue();
                    } else {
                        numResidents = reader.nextInt();
                    }
                    break;
                case JsonKeynames.GEOJSON:
                    coordinate = readCoordinate(reader);
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();

        if (numResidents == null || numResidents == 0 || coordinate == null) {
            return null;
        }

        return new WeightedLatLng(coordinate, numResidents);
    }

    /**
     * Reads a LatLng off JsonReader.
     * @param reader active JsonReader
     * @return LatLng or null
     * @throws IOException
     */
    private LatLng readCoordinate(JsonReader reader) throws IOException {
        LatLng coordinate = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(JsonKeynames.COORDINATES)) {
                reader.beginArray();
                while (reader.hasNext()) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginArray();
                        coordinate = (reader.peek().equals(JsonToken.BEGIN_ARRAY))
                            ? readMultiPolygonCoordinate(reader)
                            : readPolygonCoordinate(reader);
                        reader.endArray();
                    }
                    reader.endArray();
                }
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return coordinate;
    }

    /**
     * Reads the first Polygon coordinate off a JsonReader.
     * @param reader active JsonReader
     * @return LatLng or null
     * @throws IOException
     */
    private LatLng readPolygonCoordinate(JsonReader reader) throws IOException {
        Double latitude  = null;
        Double longitude = null;

        while (reader.hasNext()) {
            if (longitude == null) {
                longitude = reader.nextDouble();
            } else if (latitude == null) {
                latitude = reader.nextDouble();
            } else {
                reader.skipValue();
            }
        }

        if (latitude == null || longitude == null) {
            return null;
        }

        return new LatLng(latitude, longitude);
    }

    /**
     * Reads first MultiPolygon coordinate off a JsonReader.
     * @param reader active JsonReader
     * @return LatLng or null
     * @throws IOException
     */
    private LatLng readMultiPolygonCoordinate(JsonReader reader) throws IOException {
        Double latitude  = null;
        Double longitude = null;

        while (reader.hasNext()) {
            reader.beginArray();
            while (reader.hasNext()) {
                if (longitude == null) {
                    longitude = reader.nextDouble();
                } else if (latitude == null) {
                    latitude = reader.nextDouble();
                } else {
                    reader.skipValue();
                }
            }
            reader.endArray();
        }

        if (latitude == null || longitude == null) {
            return null;
        }

        return new LatLng(latitude, longitude);
    }

    /**
     * Opens a connection to json file's URL and returns an InputStream.
     * @param urlString to json file
     * @return InputStream
     * @throws IOException
     */
    private InputStream openConnectionAndGetInputStream(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        return connection.getInputStream();
    }
}
