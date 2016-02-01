package com.bottlerocketstudios.groundcontrolsample.product.model;

public class ProductItem {
    private static final String TAG = ProductItem.class.getSimpleName();
        
    private String mColor;
    private String mPrice;
    private String mManufactureCountry;
    private long mPartId;
    private String mManufacturer;    
    
    public String getColor() {
        return mColor;
    }

    public void setColor(String color) {
        mColor = color;
    }

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String price) {
        mPrice = price;
    }

    public String getManufactureCountry() {
        return mManufactureCountry;
    }

    public void setManufactureCountry(String manufactureCountry) {
        mManufactureCountry = manufactureCountry;
    }

    public long getPartId() {
        return mPartId;
    }

    public void setPartId(long partId) {
        mPartId = partId;
    }

    public String getManufacturer() {
        return mManufacturer;
    }

    public void setManufacturer(String manufacturer) {
        mManufacturer = manufacturer;
    }

             
}

