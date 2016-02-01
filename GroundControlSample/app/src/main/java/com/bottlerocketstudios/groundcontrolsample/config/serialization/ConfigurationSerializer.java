package com.bottlerocketstudios.groundcontrolsample.config.serialization;

import com.bottlerocketstudios.groundcontrolsample.config.model.Configuration;

import org.json.JSONException;
import org.json.JSONObject;

public class ConfigurationSerializer {
    private static final String TAG = ConfigurationSerializer.class.getSimpleName();
    
    private static final String OBJECT_NAME = "Configuration";
        
    private static final String FIELD_VERSIONPATH = "versionPath";
    private static final String FIELD_BASEURL = "baseUrl";
    
    public static String getObjectName() {
        return OBJECT_NAME;
    }
    
    public static Configuration parseJsonObject(JSONObject json) throws JSONException {
        
        Configuration configuration = new Configuration();

        configuration.setVersionPath(json.getString(FIELD_VERSIONPATH));
        configuration.setBaseUrl(json.getString(FIELD_BASEURL));

        return configuration;
    }

    public static JSONObject toJsonObject(Configuration serializableObject) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(FIELD_VERSIONPATH, serializableObject.getVersionPath());
        obj.put(FIELD_BASEURL, serializableObject.getBaseUrl().toString());
        return obj;
    }
   
}

