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

package com.bottlerocketstudios.groundcontrolsample.location.controller;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.bottlerocketstudios.groundcontrolsample.location.model.AddressContainer;

import java.io.IOException;
import java.util.Locale;

/**
 * Interaction with Geocoder
 */
public class GeocodingController {
    private static final String TAG = GeocodingController.class.getSimpleName();


    public static AddressContainer reverseGeocodeLocation(Context context, Location location) {

        Geocoder geocoder = new Geocoder(context, Locale.US);
        AddressContainer result = null;

        try {
            result = new AddressContainer(geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1));
        } catch (IOException e) {
            Log.e(TAG, "Geocoder exception", e);
        }

        return result;
    }
}
