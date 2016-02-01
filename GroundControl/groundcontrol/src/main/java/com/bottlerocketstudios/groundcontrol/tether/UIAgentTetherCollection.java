/*
 * Copyright (c) 2016. Bottle Rocket LLC
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

package com.bottlerocketstudios.groundcontrol.tether;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handle UI lifecycle event to enforce release of AgentTethers onDestroy. Will throw an
 * IllegalStateException if it is finalized without having destroy() called.
 */
public class UIAgentTetherCollection {

    private static final String TAG = UIAgentTetherCollection.class.getSimpleName();

    private boolean mDestroyCalled;

    private final List<AgentTether> mAgentTetherList;

    public UIAgentTetherCollection() {
        mAgentTetherList = new ArrayList<>();
    }

    public boolean isDestroyed() {
        return mDestroyCalled;
    }

    public void addTether(AgentTether agentTether) {
        if (agentTether != null) mAgentTetherList.add(agentTether);
    }

    public void addAllTethers(Collection<AgentTether> agentTetherCollection) {
        mAgentTetherList.addAll(agentTetherCollection);
    }

    public void removeTether(AgentTether agentTether) {
        mAgentTetherList.remove(agentTether);
    }

    /**
     * Release all held tethers.
     */
    public void destroy() {
        for (AgentTether agentTether : mAgentTetherList) {
            agentTether.release();
        }
        mAgentTetherList.clear();
        mDestroyCalled = true;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!isDestroyed()) {
            destroy();
            Log.e(TAG, "Object was finalized without a call to destroy()");
        }
    }
}
