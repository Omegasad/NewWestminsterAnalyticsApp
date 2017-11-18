package com.keyboardape.newwestminsteranalyticsapp.utilities;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLToInputStreamAsync extends AsyncTask<Void, Void, Void> {

    private OnInputStreamOpenedCallback mCallback;
    private String                      mURL;
    private HttpURLConnection           mConnection;
    private InputStream                 mInputStream;
    private long                        mURLLastModified;

    public URLToInputStreamAsync(OnInputStreamOpenedCallback callback, String url) {
        mCallback        = callback;
        mURL             = url;
        mConnection      = null;
        mInputStream     = null;
        mURLLastModified = 0;
    }

    // ---------------------------------------------------------------------------------------------
    //                                         ASYNC TASK
    // ---------------------------------------------------------------------------------------------/

    @Override
    protected Void doInBackground(Void... voids) {
        loadInputStream();
        mCallback.onInputStreamOpened(mInputStream, mURLLastModified);
        cleanUpInputStream();
        return null;
    }

    // ---------------------------------------------------------------------------------------------
    //                              LOADING & CLEANING INPUT STREAM READER
    // ---------------------------------------------------------------------------------------------/

    private boolean loadInputStream() {
        try {
            String requestMethod = "GET";
            URL url = new URL(mURL);
            mConnection = (HttpURLConnection) url.openConnection();
            mConnection.setRequestMethod(requestMethod);
            mURLLastModified = mConnection.getLastModified();
            mInputStream = mConnection.getInputStream();
            return true;
        } catch (Exception e) {
            Log.e(URLToInputStreamAsync.class.getSimpleName(), e.getMessage());
        }
        return false;
    }

    private void cleanUpInputStream() {
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mConnection != null) {
                mConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e(URLToInputStreamAsync.class.getSimpleName(), e.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------
    //                        CALLBACK : ON INPUT STREAM OPENED CALLBACK
    // ---------------------------------------------------------------------------------------------/

    public interface OnInputStreamOpenedCallback {
        void onInputStreamOpened(InputStream inputStreamOrNull, long urlLastModified);
    }
}