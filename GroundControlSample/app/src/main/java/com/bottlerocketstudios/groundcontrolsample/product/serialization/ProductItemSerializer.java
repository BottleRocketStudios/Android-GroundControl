package com.bottlerocketstudios.groundcontrolsample.product.serialization;

import com.bottlerocketstudios.groundcontrolsample.product.model.ProductItem;


import org.json.JSONException;
import org.json.JSONObject;

public class ProductItemSerializer {
    private static final String TAG = ProductItemSerializer.class.getSimpleName();
    
    private static final String OBJECT_NAME = "productItem";
        
    private static final String FIELD_COLOR = "color";
    private static final String FIELD_PRICE = "price";
    private static final String FIELD_MANUFACTURECOUNTRY = "manufactureCountry";
    private static final String FIELD_PARTID = "partId";
    private static final String FIELD_MANUFACTURER = "manufacturer";
    
    public static String getObjectName() {
        return OBJECT_NAME;
    }
    
    public static ProductItem parseJsonObject(JSONObject json) throws JSONException {
        
        ProductItem productItem = new ProductItem();

        productItem.setColor(json.getString(FIELD_COLOR));
        productItem.setPrice(json.getString(FIELD_PRICE));
        productItem.setManufactureCountry(json.getString(FIELD_MANUFACTURECOUNTRY));
        productItem.setPartId(json.getLong(FIELD_PARTID));
        productItem.setManufacturer(json.getString(FIELD_MANUFACTURER));

        return productItem;
    }

    public static JSONObject toJsonObject(ProductItem serializableObject) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(FIELD_COLOR, serializableObject.getColor());
        obj.put(FIELD_PRICE, serializableObject.getPrice());
        obj.put(FIELD_MANUFACTURECOUNTRY, serializableObject.getManufactureCountry());
        obj.put(FIELD_PARTID, serializableObject.getPartId());
        obj.put(FIELD_MANUFACTURER, serializableObject.getManufacturer());
        return obj;
    }
   
}

