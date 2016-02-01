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
import com.bottlerocketstudios.groundcontrol.convenience.GroundControl;
import com.bottlerocketstudios.groundcontrol.listener.FunctionalAgentListener;
import com.bottlerocketstudios.groundcontrolsample.location.model.AddressContainer;

/**
 * Meta-Agent that uses the LocationAgent and ReverseGeocodeAgent to obtain a country code from the current location.
 */
public class CountryCodeAgent extends AbstractAgent<AddressContainer, Void> {

    Context mContext;

    public CountryCodeAgent(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public String getUniqueIdentifier() {
        return CountryCodeAgent.class.getCanonicalName();
    }

    @Override
    public void cancel() {}

    @Override
    public void onProgressUpdateRequested() {}

    @Override
    public void run() {
        GroundControl.bgAgent(getAgentExecutor(), new LocationAgent())
                .bgParallelCallback(new FunctionalAgentListener<Location, Void>() {
                    @Override
                    public void onCompletion(String agentIdentifier, Location result) {
                        startGeocoding(result);
                    }
                })
                .execute();
    }

    private void startGeocoding(Location location) {
        GroundControl.bgAgent(getAgentExecutor(), new ReverseGeocodeAgent(mContext, location))
                .bgParallelCallback(new FunctionalAgentListener<AddressContainer, Void>() {
                    @Override
                    public void onCompletion(String agentIdentifier, AddressContainer result) {
                        getAgentListener().onCompletion(getUniqueIdentifier(), result);
                    }
                })
                .execute();
    }
}
