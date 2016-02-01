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
 * Default implementation of AgentTetherBuilder
 */
public class StandardAgentTetherBuilder implements AgentTetherBuilder {

    private AgentExecutor mAgentExecutor;
    private String mAgentIdentifier;
    private AgentListener mAgentListener;

    @Override
    public AgentTetherBuilder setAgentExecutor(AgentExecutor agentExecutor) {
        mAgentExecutor = agentExecutor;
        return this;
    }

    @Override
    public AgentExecutor getAgentExecutor() {
        return mAgentExecutor;
    }

    @Override
    public AgentTetherBuilder setAgentIdentifier(String agentIdentifier) {
        mAgentIdentifier = agentIdentifier;
        return this;
    }

    @Override
    public String getAgentIdentifier() {
        return mAgentIdentifier;
    }

    @Override
    public AgentTetherBuilder setAgentListener(AgentListener agentListener) {
        mAgentListener = agentListener;
        return this;
    }

    @Override
    public AgentListener getAgentListener() {
        return mAgentListener;
    }

    @Override
    public AgentTetherBuilder clear() {
        setAgentIdentifier(null);
        setAgentExecutor(null);
        setAgentListener(null);
        return this;
    }

    @Override
    public AgentTether build() {
        return new StandardAgentTether(this);
    }
}
