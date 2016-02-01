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

package com.bottlerocketstudios.groundcontrolsample.core.controller;

import android.content.Context;
import android.net.Uri;

import com.bottlerocketstudios.groundcontrolsample.R;
import com.bottlerocketstudios.groundcontrolsample.config.model.Configuration;
import com.bottlerocketstudios.groundcontrolsample.config.model.CurrentVersion;
import com.bottlerocketstudios.groundcontrolsample.config.model.Region;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProductApi {

    public static JSONObject downloadJson(OkHttpClient okHttpClient, Request request) throws IOException, JSONException {
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new JSONObject(response.body().string());
        } else {
            throw new IOException("Server did not respond with success");
        }
    }

    public static Request createConfigurationRequest(Context context) {
        String configUrl = context.getString(R.string.config_url);
        return new Request.Builder()
                .url(configUrl)
                .get()
                .build();
    }

    public static Request createVersionRequest(Configuration configuration) {
        return new Request.Builder()
                .url(createFullUrl(configuration, configuration.getVersionPath()))
                .get()
                .build();
    }

    public static Request createRegionRequest(Configuration configuration, CurrentVersion currentVersion) {
        return new Request.Builder()
                .url(createFullUrl(configuration, currentVersion.getVersion()))
                .get()
                .build();
    }

    public static Request createProductRequest(Configuration configuration, Region region) {
        return new Request.Builder()
                .url(createFullUrl(configuration, region.getPath()))
                .get()
                .build();
    }

    private static String createFullUrl(Configuration configuration, String appendedPath) {
        return Uri.withAppendedPath(configuration.getBaseUrl(), appendedPath).toString();
    }

}
