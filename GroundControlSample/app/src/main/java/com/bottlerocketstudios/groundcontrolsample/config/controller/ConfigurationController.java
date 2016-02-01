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

package com.bottlerocketstudios.groundcontrolsample.config.controller;

import android.content.Context;
import android.util.Log;

import com.bottlerocketstudios.groundcontrolsample.config.model.Configuration;
import com.bottlerocketstudios.groundcontrolsample.config.model.CurrentVersion;
import com.bottlerocketstudios.groundcontrolsample.config.model.RegionConfiguration;
import com.bottlerocketstudios.groundcontrolsample.config.serialization.ConfigurationSerializer;
import com.bottlerocketstudios.groundcontrolsample.config.serialization.CurrentVersionSerializer;
import com.bottlerocketstudios.groundcontrolsample.config.serialization.RegionConfigurationSerializer;
import com.bottlerocketstudios.groundcontrolsample.core.construction.Injectable;
import com.bottlerocketstudios.groundcontrolsample.core.construction.ServiceInjector;
import com.bottlerocketstudios.groundcontrolsample.core.controller.ProductApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ConfigurationController {
    private static final String TAG = ConfigurationController.class.getSimpleName();

    private static final String PREF_CACHE_TIME = "ConfigurationController.cacheTime";
    private static final String PREF_CONFIGURATION = "ConfigurationController.configuration";

    private static final long CACHE_LIFETIME_MS = TimeUnit.MINUTES.toMillis(10);

    private OkHttpClient mHttpClient;

    public ConfigurationController() {
        ServiceInjector.injectWithType(OkHttpClient.class, new Injectable<OkHttpClient>() {
            @Override
            public void receiveInjection(OkHttpClient injection) {
                mHttpClient = injection;
            }
        });
    }

    public Configuration downloadConfiguration(Context context) {
        Configuration configuration = null;
        Request request = ProductApi.createConfigurationRequest(context);
        try {
            JSONObject jsonObject = ProductApi.downloadJson(mHttpClient, request);
            configuration = ConfigurationSerializer.parseJsonObject(jsonObject);
        } catch (IOException e) {
            Log.e(TAG, "Caught java.io.IOException", e);
        } catch (JSONException e) {
            Log.e(TAG, "Caught org.json.JSONException", e);
        }
        return configuration;
    }

    public CurrentVersion downloadCurrentVersion(Configuration configuration) {
        CurrentVersion currentVersion = null;
        Request request = ProductApi.createVersionRequest(configuration);
        try {
            JSONObject jsonObject = ProductApi.downloadJson(mHttpClient, request);
            currentVersion = CurrentVersionSerializer.parseJsonObject(jsonObject);
        } catch (IOException e) {
            Log.e(TAG, "Caught java.io.IOException", e);
        } catch (JSONException e) {
            Log.e(TAG, "Caught org.json.JSONException", e);
        }
        return currentVersion;
    }

    public RegionConfiguration downloadRegionConfiguration(Configuration configuration, CurrentVersion currentVersion) {
        RegionConfiguration regionConfiguration = null;
        Request request = ProductApi.createRegionRequest(configuration, currentVersion);
        try {
            JSONObject jsonObject = ProductApi.downloadJson(mHttpClient, request);
            regionConfiguration = RegionConfigurationSerializer.parseJsonObject(jsonObject);
        } catch (IOException e) {
            Log.e(TAG, "Caught java.io.IOException", e);
        } catch (JSONException e) {
            Log.e(TAG, "Caught org.json.JSONException", e);
        }
        return regionConfiguration;
    }
}
