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

import com.bottlerocketstudios.groundcontrol.convenience.GroundControl;
import com.bottlerocketstudios.groundcontrol.dependency.DependencyHandlingAgent;
import com.bottlerocketstudios.groundcontrol.listener.FunctionalAgentListener;
import com.bottlerocketstudios.groundcontrolsample.config.controller.ConfigurationController;
import com.bottlerocketstudios.groundcontrolsample.config.model.Configuration;
import com.bottlerocketstudios.groundcontrolsample.config.model.CurrentVersion;
import com.bottlerocketstudios.groundcontrolsample.config.model.RegionConfiguration;

/**
 * Fetch the RegionConfiguration from server or cache.
 */
public class RegionConfigurationAgent extends DependencyHandlingAgent<RegionConfiguration, Void> {

    private final Context mContext;
    private Configuration mConfiguration;
    private CurrentVersion mCurrentVersion;

    public RegionConfigurationAgent(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public String getUniqueIdentifier() {
        return RegionConfigurationAgent.class.getCanonicalName();
    }

    @Override
    public void cancel() {}

    @Override
    public void onProgressUpdateRequested() {}

    @Override
    public void run() {
        addParallelDependency(GroundControl.bgAgent(getAgentExecutor(), new ConfigurationAgent(mContext)),
                new FunctionalAgentListener<Configuration, Void>() {
                    @Override
                    public void onCompletion(String agentIdentifier, Configuration result) {
                        mConfiguration = result;
                    }
                });
        addParallelDependency(GroundControl.bgAgent(getAgentExecutor(), new VersionAgent(mContext)),
                new FunctionalAgentListener<CurrentVersion, Void>() {
                    @Override
                    public void onCompletion(String agentIdentifier, CurrentVersion result) {
                        mCurrentVersion = result;
                    }
                });

        executeDependencies();
    }

    @Override
    public void onDependenciesCompleted() {
        RegionConfiguration regionConfiguration = null;
        if (mConfiguration != null && mCurrentVersion != null) {
            ConfigurationController configurationController = new ConfigurationController();
            regionConfiguration = configurationController.downloadRegionConfiguration(mConfiguration, mCurrentVersion);
        }
        getAgentListener().onCompletion(getUniqueIdentifier(), regionConfiguration);
    }

}
