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

package com.bottlerocketstudios.groundcontrolsample.config.agent;

import android.content.Context;

import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;
import com.bottlerocketstudios.groundcontrolsample.config.controller.ConfigurationController;
import com.bottlerocketstudios.groundcontrolsample.config.model.Configuration;

/**
 * Fetch the configuration from server or cache.
 */
public class ConfigurationAgent extends AbstractAgent<Configuration, Void> {

    private final Context mContext;

    public ConfigurationAgent(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public String getUniqueIdentifier() {
        return ConfigurationAgent.class.getCanonicalName();
    }

    @Override
    public void cancel() {}

    @Override
    public void onProgressUpdateRequested() {}

    @Override
    public void run() {
        ConfigurationController configurationController = new ConfigurationController();
        Configuration configuration = configurationController.downloadConfiguration(mContext);
        getAgentListener().onCompletion(getUniqueIdentifier(), configuration);
    }

}
