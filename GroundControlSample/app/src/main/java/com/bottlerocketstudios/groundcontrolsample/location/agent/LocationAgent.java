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

import android.location.Location;

import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;
import com.bottlerocketstudios.groundcontrolsample.location.controller.FakeLocationController;

/**
 * Get a location from the FakeLocationController
 */
public class LocationAgent extends AbstractAgent<Location, Void> implements FakeLocationController.LocationListener {
    @Override
    public String getUniqueIdentifier() {
        return LocationAgent.class.getCanonicalName();
    }

    @Override
    public void cancel() {}

    @Override
    public void onProgressUpdateRequested() {}

    @Override
    public void run() {
        (new FakeLocationController()).requestLocation(this);
    }

    @Override
    public void onLocationFound(Location location) {
        getAgentListener().onCompletion(getUniqueIdentifier(), location);
    }
}
