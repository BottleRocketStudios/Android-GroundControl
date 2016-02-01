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

package com.bottlerocketstudios.groundcontrolsample.location.agent;

import android.content.Context;
import android.location.Location;

import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;
import com.bottlerocketstudios.groundcontrolsample.location.controller.GeocodingController;
import com.bottlerocketstudios.groundcontrolsample.location.model.AddressContainer;

/**
 * Provided with a location, reverse-geocode the location to a collection of addresses.
 */
public class ReverseGeocodeAgent extends AbstractAgent<AddressContainer, Void> {

    private final Location mLocation;
    private final Context mContext;

    public ReverseGeocodeAgent(Context context, Location location) {
        mContext = context.getApplicationContext();
        mLocation = location;
    }

    @Override
    public String getUniqueIdentifier() {
        return ReverseGeocodeAgent.class.getCanonicalName();
    }

    @Override
    public void cancel() {}

    @Override
    public void onProgressUpdateRequested() {}

    @Override
    public void run() {
        AddressContainer addressContainer = GeocodingController.reverseGeocodeLocation(mContext, mLocation);
        getAgentListener().onCompletion(getUniqueIdentifier(), addressContainer);
    }
}
