package com.keyboardape.newwestminsteranalyticsapp.downloaders;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Parses a file containing one or more JSON objects.
 * Parses and processes one JSON object at a time.
 */
public abstract class JsonStreamParserAsync extends AsyncTask<Void, Void, Void> {

    /** URL containing JSON string. */
    private String mJsonUrl;

    /** Tracks whether the stream has been parsed. */
    private boolean mIsStreamParsed;

    // globalized to ease cleanUpInputStreamReader() method
    private HttpURLConnection mConnection;
    private InputStream mInputStream;
    private InputStreamReader mInputStreamReader;

    /**
     * Constructor.
     * @param jsonUrl containing JSON string
     */
    public JsonStreamParserAsync(String jsonUrl) {
        mJsonUrl = jsonUrl;
        mIsStreamParsed = false;
        mConnection = null;
        mInputStream = null;
        mInputStreamReader = null;
    }

    /**
     * Called every time a JSON object is parsed from URL.
     * RUNS ON BACKGROUND THREAD.
     * @param o JSONObject to be processed
     */
    abstract protected void processJsonObject(JSONObject o);

    /**
     * Called if stream can not be loaded.
     * RUNS ON MAIN/UI THREAD.
     */
    abstract protected void onParseFailed();

    /**
     * Called if stream has been parsed and processed successfully.
     * RUNS ON MAIN/UI THREAD.
     */
    abstract protected void onParseSuccess();

    /**
     * Called on main thread before parsing JSON URL.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * Work done in background thread.
     * @param voids not used
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        if (loadInputStreamReader()) {
            JSONObject jsonObject;
            while ((jsonObject = getNextJsonObjectOrNull()) != null) {
                processJsonObject(jsonObject);
            }
            mIsStreamParsed = true;
        }
        cleanUpInputStreamReader();
        return null;
    }

    /**
     * Called on main thread after execution of main task.
     * @param aVoid not used
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mIsStreamParsed) {
            onParseSuccess();
        } else {
            onParseFailed();
        }
    }

    /*
     * Parses and returns the next JSON object.
     * @return JSONObject or null
     */
    private JSONObject getNextJsonObjectOrNull() {
        final int OPEN_BRACKET = '{';
        final int CLOSE_BRACKET = '}';
        final int END_OF_STREAM = -1;

        StringBuilder str = new StringBuilder();

        try {
            int openBracketCount = 0;
            int c;

            // skip till END_OF_STREAM or OPEN_BRACKET found
            while ((c = mInputStreamReader.read()) != END_OF_STREAM && c != OPEN_BRACKET) {}

            if (c == END_OF_STREAM) {
                return null;
            }

            // increment openBracketCount and append to string
            str.append((char) OPEN_BRACKET);
            ++openBracketCount;

            // append all to string till openBracketCount == 0
            while (true) {
                c = mInputStreamReader.read();
                str.append((char) c);
                if (c == OPEN_BRACKET) {
                    ++openBracketCount;
                } else if (c == CLOSE_BRACKET) {
                    --openBracketCount;
                }

                // found the end of the block
                if (openBracketCount == 0) {
                    break;
                }
            }
        } catch (Exception e) {}

        // return JSONObject or null
        if (str.length() > 0) {
            try {
                return new JSONObject(str.toString());
            } catch (Exception e) {}
        }
        return null;
    }

    /*
     * Load InputStreamReader for parsing.
     * @return true if successful
     */
    private boolean loadInputStreamReader() {
        try {
            String requestMethod = "GET";
            URL url = new URL(mJsonUrl);
            mConnection = (HttpURLConnection) url.openConnection();
            mConnection.setRequestMethod(requestMethod);
            mInputStream = mConnection.getInputStream();
            mInputStreamReader = new InputStreamReader(mInputStream);
            return true;
        } catch (Exception e) {
            Log.e(JsonStreamParserAsync.class.getSimpleName(), e.getMessage());
        }
        return false;
    }

    /*
     * Releases all resources used to load InputStreamReader.
     */
    private void cleanUpInputStreamReader() {
        try {
            if (mInputStreamReader != null) {
                mInputStreamReader.close();
            }
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mConnection != null) {
                mConnection.disconnect();
            }
        } catch (Exception e) {}
    }
}