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

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;

/**
 * Default implementation of AgentTether
 */
public class StandardAgentTether implements AgentTether {

    private final AgentExecutor mAgentExecutor;
    private final String mAgentIdentifier;
    private final AgentListener mAgentListener;

    public StandardAgentTether(AgentTetherBuilder agentTetherBuilder) {
        mAgentExecutor = agentTetherBuilder.getAgentExecutor();
        mAgentIdentifier = agentTetherBuilder.getAgentIdentifier();
        mAgentListener = agentTetherBuilder.getAgentListener();
    }

    @Override
    public void cancel() {
        mAgentExecutor.tetherCancel(this, mAgentIdentifier, mAgentListener);
    }

    @Override
    public void release() {
        mAgentExecutor.tetherRelease(this, mAgentIdentifier, mAgentListener);
    }

    @Override
    public String getAgentIdentifier() {
        return mAgentIdentifier;
    }
}
