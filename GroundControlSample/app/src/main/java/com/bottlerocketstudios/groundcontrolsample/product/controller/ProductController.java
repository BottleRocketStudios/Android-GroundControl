/*
 * Copyright (c) 2016 Bottle Rocket LLC.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bottlerocketstudios.groundcontrolsample.product.controller;

import android.location.Address;
import android.util.Log;

import com.bottlerocketstudios.groundcontrolsample.config.model.Configuration;
import com.bottlerocketstudios.groundcontrolsample.config.model.Region;
import com.bottlerocketstudios.groundcontrolsample.config.model.RegionConfiguration;
import com.bottlerocketstudios.groundcontrolsample.core.construction.Injectable;
import com.bottlerocketstudios.groundcontrolsample.core.construction.ServiceInjector;
import com.bottlerocketstudios.groundcontrolsample.core.controller.ProductApi;
import com.bottlerocketstudios.groundcontrolsample.product.model.ProductResponse;
import com.bottlerocketstudios.groundcontrolsample.product.serialization.ProductResponseSerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Encapsulate business rules and operations for Products
 */
public class ProductController {
    private static final String TAG = ProductController.class.getSimpleName();

    private OkHttpClient mHttpClient;

    public ProductController() {
        ServiceInjector.injectWithType(OkHttpClient.class, new Injectable<OkHttpClient>() {
            @Override
            public void receiveInjection(OkHttpClient injection) {
                mHttpClient = injection;
            }
        });
    }

    public Region getBestRegion(Address address, RegionConfiguration regionConfiguration) {
        Region defaultRegion = null;
        for (Region region: regionConfiguration.getRegionList()) {
            if (address.getCountryCode().equalsIgnoreCase(region.getRegion())) {
                return region;
            }
            if (RegionConfiguration.DEFAULT_REGION.equalsIgnoreCase(region.getRegion())) {
                defaultRegion = region;
            }
        }
        return defaultRegion;
    }

    public ProductResponse downloadProductList(Configuration configuration, Region region) {
        ProductResponse productResponse = null;
        Request request = ProductApi.createProductRequest(configuration, region);
        try {
            JSONObject jsonObject = ProductApi.downloadJson(mHttpClient, request);
            productResponse = ProductResponseSerializer.parseJsonObject(jsonObject);
        } catch (IOException e) {
            Log.e(TAG, "Caught java.io.IOException", e);
        } catch (JSONException e) {
            Log.e(TAG, "Caught org.json.JSONException", e);
        }
        return productResponse;
    }
}
