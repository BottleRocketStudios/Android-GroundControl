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

package com.bottlerocketstudios.groundcontrolsample.home.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bottlerocketstudios.groundcontrol.convenience.GroundControl;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrolsample.R;
import com.bottlerocketstudios.groundcontrolsample.core.ui.BaseActivity;
import com.bottlerocketstudios.groundcontrolsample.product.agent.GetLatestLocalProductsAgent;
import com.bottlerocketstudios.groundcontrolsample.product.model.ProductResponse;
import com.bottlerocketstudios.groundcontrolsample.product.model.ProductUpdateStatus;
import com.bottlerocketstudios.groundcontrolsample.product.ui.ProductRecyclerAdapter;


public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mStatusText;
    private ProductRecyclerAdapter mProductRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        RecyclerView productRecycler = (RecyclerView) findViewById(R.id.main_recycler);
        mProductRecyclerAdapter = new ProductRecyclerAdapter();
        productRecycler.setAdapter(mProductRecyclerAdapter);
        productRecycler.setLayoutManager(new LinearLayoutManager(this));

        mStatusText = (TextView) findViewById(R.id.main_status);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GroundControl.uiAgent(this, new GetLatestLocalProductsAgent(this))
                .cacheAgeMs(0)
                .uiCallback(mProductAgentListener)
                .execute();

    }

    private AgentListener<ProductResponse, ProductUpdateStatus> mProductAgentListener = new AgentListener<ProductResponse, ProductUpdateStatus>() {
        @Override
        public void onCompletion(String agentIdentifier, ProductResponse result) {
            if (result != null) {
                updateRecyclerView(result);
            } else {
                Toast.makeText(MainActivity.this, R.string.main_server_failure, Toast.LENGTH_LONG).show();
            }
            mStatusText.setVisibility(View.GONE);
        }

        @Override
        public void onProgress(String agentIdentifier, ProductUpdateStatus progress) {
            Log.d(TAG, "Status updated to " + progress);
            updateProgress(progress);
        }
    };

    private void updateProgress(ProductUpdateStatus progress) {
        String statusText;
        switch (progress) {
            case LOCATING:
                statusText = getString(R.string.main_status_locating);
                break;
            case DOWNLOADING:
                statusText = getString(R.string.main_status_downloading);
                break;
            case INITIALIZING:
            default:
                statusText = getString(R.string.main_status_initializing);
                break;
        }
        mStatusText.setText(statusText);
    }

    private void updateRecyclerView(ProductResponse productResponse) {
        mProductRecyclerAdapter.swapItems(productResponse.getProductList());
    }
}
