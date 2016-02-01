package com.bottlerocketstudios.groundcontrolsample.config.serialization;

import com.bottlerocketstudios.groundcontrolsample.config.model.Region;


import org.json.JSONException;
import org.json.JSONObject;

public class RegionSerializer {
    private static final String TAG = RegionSerializer.class.getSimpleName();
    
    private static final String OBJECT_NAME = "regionListItem";
        
    private static final String FIELD_PATH = "path";
    private static final String FIELD_REGION = "region";
    
    public static String getObjectName() {
        return OBJECT_NAME;
    }
    
    public static Region parseJsonObject(JSONObject json) throws JSONException {
        
        Region region = new Region();

        region.setPath(json.getString(FIELD_PATH));
        region.setRegion(json.getString(FIELD_REGION));

        return region;
    }

    public static JSONObject toJsonObject(Region serializableObject) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(FIELD_PATH, serializableObject.getPath());
        obj.put(FIELD_REGION, serializableObject.getRegion());
        return obj;
    }
   
}

