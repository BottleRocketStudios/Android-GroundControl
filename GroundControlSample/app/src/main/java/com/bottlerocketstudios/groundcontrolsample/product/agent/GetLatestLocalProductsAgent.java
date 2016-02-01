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

package com.bottlerocketstudios.groundcontrolsample.product.agent;

import android.content.Context;
import android.location.Address;

import com.bottlerocketstudios.groundcontrol.dependency.DependencyHandlingAgent;
import com.bottlerocketstudios.groundcontrol.convenience.GroundControl;
import com.bottlerocketstudios.groundcontrol.listener.FunctionalAgentListener;
import com.bottlerocketstudios.groundcontrolsample.config.agent.ConfigurationAgent;
import com.bottlerocketstudios.groundcontrolsample.config.agent.RegionConfigurationAgent;
import com.bottlerocketstudios.groundcontrolsample.config.model.Configuration;
import com.bottlerocketstudios.groundcontrolsample.config.model.Region;
import com.bottlerocketstudios.groundcontrolsample.config.model.RegionConfiguration;
import com.bottlerocketstudios.groundcontrolsample.location.agent.CountryCodeAgent;
import com.bottlerocketstudios.groundcontrolsample.location.model.AddressContainer;
import com.bottlerocketstudios.groundcontrolsample.product.controller.ProductController;
import com.bottlerocketstudios.groundcontrolsample.product.model.ProductResponse;
import com.bottlerocketstudios.groundcontrolsample.product.model.ProductUpdateStatus;

/**
 * An agent that will obtain the latest list of products.
 */
public class GetLatestLocalProductsAgent extends DependencyHandlingAgent<ProductResponse, ProductUpdateStatus> {

    private final Context mContext;
    private ProductUpdateStatus mProductUpdateStatus;
    private Configuration mConfiguration;
    private RegionConfiguration mRegionConfiguration;
    private AddressContainer mAddressContainer;

    public GetLatestLocalProductsAgent(Context context) {
        mContext = context.getApplicationContext();
        mProductUpdateStatus = ProductUpdateStatus.INITIALIZING;
    }

    @Override
    public String getUniqueIdentifier() {
        return GetLatestLocalProductsAgent.class.getCanonicalName();
    }

    @Override
    public void onProgressUpdateRequested() {
        notifyProgress();
    }

    private void notifyProgress() {
        getAgentListener().onProgress(getUniqueIdentifier(), mProductUpdateStatus);
    }

    private void setProductUpdateStatus(ProductUpdateStatus productUpdateStatus) {
        mProductUpdateStatus = productUpdateStatus;
    }

    @Override
    public void run() {
        setProductUpdateStatus(ProductUpdateStatus.LOCATING);
        notifyProgress();

        addParallelDependency(GroundControl.bgAgent(getAgentExecutor(), new ConfigurationAgent(mContext)),
                new FunctionalAgentListener<Configuration, Void>() {
                    @Override
                    public void onCompletion(String agentIdentifier, Configuration result) {
                        mConfiguration = result;
                    }
                });

        addParallelDependency(GroundControl.bgAgent(getAgentExecutor(), new RegionConfigurationAgent(mContext)),
                new FunctionalAgentListener<RegionConfiguration, Void>() {
                    @Override
                    public void onCompletion(String agentIdentifier, RegionConfiguration result) {
                        mRegionConfiguration = result;
                    }
                });

        addParallelDependency(GroundControl.bgAgent(getAgentExecutor(), new CountryCodeAgent(mContext)),
                new FunctionalAgentListener<AddressContainer, Void>() {
                    @Override
                    public void onCompletion(String agentIdentifier, AddressContainer result) {
                        mAddressContainer = result;
                    }
                });

        executeDependencies();
    }

    @Override
    public void onDependenciesCompleted() {
        setProductUpdateStatus(ProductUpdateStatus.DOWNLOADING);
        notifyProgress();

        ProductResponse productResponse = null;
        if (mAddressContainer != null && mAddressContainer.getAddresses().size() > 0 && mRegionConfiguration != null && mConfiguration != null) {
            ProductController productController = new ProductController();
            //Pick the first address
            Address address = mAddressContainer.getAddresses().get(0);
            //Find the best region for this address
            Region region = productController.getBestRegion(address, mRegionConfiguration);
            productResponse = productController.downloadProductList(mConfiguration, region);
        }
        getAgentListener().onCompletion(getUniqueIdentifier(), productResponse);
    }
}
