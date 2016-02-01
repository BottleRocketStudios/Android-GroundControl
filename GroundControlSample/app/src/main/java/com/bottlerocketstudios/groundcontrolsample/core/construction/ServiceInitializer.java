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

package com.bottlerocketstudios.groundcontrolsample.core.construction;

import android.content.Context;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;


public class ServiceInitializer {

    private static final long CACHE_SIZE = 3 * 1024 * 1024;

    public static void initializeServices(Context context) {
        initializeOkHttp(context);
    }

    private static void initializeOkHttp(Context context) {
        OkHttpClient okHttpClient = (new OkHttpClient.Builder())
                .cache(createCache(context))
                .build();
        SampleAppServiceLocator.put(OkHttpClient.class, okHttpClient);
    }

    private static Cache createCache(Context context) {
        File cacheFile = new File(context.getCacheDir(), "http_cache.bin");
        return new Cache(cacheFile, CACHE_SIZE);
    }

    public static <T> void replaceService(Class<T> serviceClass, T service) {
        SampleAppServiceLocator.put(serviceClass, service);
    }

}
