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

import android.location.Location;
import android.os.Handler;
import android.os.Looper;


/**
 * Artificially take some time to obtain a location and return it to the listener asynchronously.
 */
public class FakeLocationController {

    public void requestLocation(final LocationListener locationListener) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Location location = new Location("gps");
                location.setLatitude(32.953980);
                location.setLongitude(-96.821973);
                locationListener.onLocationFound(location);
            }
        }, 1000);
    }

    public interface LocationListener {
        public void onLocationFound(Location location);
    }
}
