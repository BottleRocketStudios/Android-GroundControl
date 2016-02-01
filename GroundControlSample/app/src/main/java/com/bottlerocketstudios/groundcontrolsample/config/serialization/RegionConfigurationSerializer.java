package com.bottlerocketstudios.groundcontrolsample.config.serialization;

import com.bottlerocketstudios.groundcontrolsample.config.model.RegionConfiguration;
import com.bottlerocketstudios.groundcontrolsample.config.model.Region;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegionConfigurationSerializer {
    private static final String TAG = RegionConfigurationSerializer.class.getSimpleName();
    
    private static final String OBJECT_NAME = "RegionConfiguration";
        
    private static final String FIELD_REGIONLIST = "regionList";
    
    public static String getObjectName() {
        return OBJECT_NAME;
    }
    
    public static RegionConfiguration parseJsonObject(JSONObject json) throws JSONException {
        
        RegionConfiguration regionConfiguration = new RegionConfiguration();

        JSONArray regionListJsonArray = json.getJSONArray(FIELD_REGIONLIST);
        List<Region> regionList = new ArrayList<>(regionListJsonArray.length());
        for (int i = 0; i < regionListJsonArray.length(); i++) {
            regionList.add(RegionSerializer.parseJsonObject(regionListJsonArray.getJSONObject(i)));
        }
        regionConfiguration.setRegionList(regionList);


        return regionConfiguration;
    }

    public static JSONObject toJsonObject(RegionConfiguration serializableObject) throws JSONException {
        JSONObject obj = new JSONObject();

        JSONArray regionListJsonArray = new JSONArray();
        List<Region> regionList = serializableObject.getRegionList();
        for (Region item : regionList) {
            regionListJsonArray.put(RegionSerializer.toJsonObject(item));
        }
        obj.put(FIELD_REGIONLIST, regionListJsonArray);

        return obj;
    }
   
}

