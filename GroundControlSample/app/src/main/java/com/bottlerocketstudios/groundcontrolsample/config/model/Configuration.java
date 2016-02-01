package com.bottlerocketstudios.groundcontrolsample.config.model;

import android.net.Uri;

public class Configuration {
    private static final String TAG = Configuration.class.getSimpleName();
        
    private String mVersionPath;
    private Uri mBaseUrl;
    
    public String getVersionPath() {
        return mVersionPath;
    }

    public void setVersionPath(String versionPath) {
        mVersionPath = versionPath;
    }

    public Uri getBaseUrl() {
        return mBaseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        mBaseUrl = Uri.parse(baseUrl);
    }

             
}

