package com.bottlerocketstudios.groundcontrolsample.product.model;

import java.util.List;

public class ProductResponse {
    private static final String TAG = ProductResponse.class.getSimpleName();
        
    private List<ProductItem> mProduct;    
    
    public List<ProductItem> getProductList() {
        return mProduct;
    }

    public void setProduct(List<ProductItem> product) {
        mProduct = product;
    }

             
}

