package com.bottlerocketstudios.groundcontrolsample.config.model;

import java.util.List;

public class RegionConfiguration {
    public static final String DEFAULT_REGION = "DEFAULT";
        
    private List<Region> mRegionList;
    
    public List<Region> getRegionList() {
        return mRegionList;
    }

    public void setRegionList(List<Region> regionList) {
        mRegionList = regionList;
    }

             
}

