package com.bottlerocketstudios.groundcontrolsample.product.serialization;

import com.bottlerocketstudios.groundcontrolsample.product.model.ProductResponse;
import com.bottlerocketstudios.groundcontrolsample.product.model.ProductItem;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductResponseSerializer {
    private static final String TAG = ProductResponseSerializer.class.getSimpleName();
    
    private static final String OBJECT_NAME = "ProductList";
        
    private static final String FIELD_PRODUCT = "product";
    
    public static String getObjectName() {
        return OBJECT_NAME;
    }
    
    public static ProductResponse parseJsonObject(JSONObject json) throws JSONException {
        
        ProductResponse productResponse = new ProductResponse();


        JSONArray productJsonArray = json.getJSONArray(FIELD_PRODUCT);
        List<ProductItem> product = new ArrayList<>(productJsonArray.length());
        for (int i = 0; i < productJsonArray.length(); i++) {
            product.add(ProductItemSerializer.parseJsonObject(productJsonArray.getJSONObject(i)));
        }
        productResponse.setProduct(product);


        return productResponse;
    }

    public static JSONObject toJsonObject(ProductResponse serializableObject) throws JSONException {
        JSONObject obj = new JSONObject();

        JSONArray productJsonArray = new JSONArray();
        List<ProductItem> product = serializableObject.getProductList();
        for (ProductItem item : product) {
            productJsonArray.put(ProductItemSerializer.toJsonObject(item));
        }
        obj.put(FIELD_PRODUCT, productJsonArray);

        return obj;
    }
   
}

