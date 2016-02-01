package com.bottlerocketstudios.groundcontrolsample.config.serialization;

import com.bottlerocketstudios.groundcontrolsample.config.model.CurrentVersion;

import org.json.JSONException;
import org.json.JSONObject;

public class CurrentVersionSerializer {
    private static final String TAG = CurrentVersionSerializer.class.getSimpleName();
    
    private static final String OBJECT_NAME = "CurrentVersion";
        
    private static final String FIELD_VERSION = "version";
    
    public static String getObjectName() {
        return OBJECT_NAME;
    }
    
    public static CurrentVersion parseJsonObject(JSONObject json) throws JSONException {
        
        CurrentVersion currentVersion = new CurrentVersion();

        currentVersion.setVersion(json.getString(FIELD_VERSION));

        return currentVersion;
    }

    public static JSONObject toJsonObject(CurrentVersion serializableObject) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(FIELD_VERSION, serializableObject.getVersion());
        return obj;
    }
   
}

