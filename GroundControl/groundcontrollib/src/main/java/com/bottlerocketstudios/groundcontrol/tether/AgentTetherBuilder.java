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
 * Builder to work like an AbstractFactory to create new instances of an AgentLeash
 */
public interface AgentTetherBuilder {
    /**
     * The AgentExecutor this tether is connected to.
     */
    AgentTetherBuilder setAgentExecutor(AgentExecutor agentExecutor);

    AgentExecutor getAgentExecutor();

    /**
     * The agentIdentifier associated with this Tether.
     */
    AgentTetherBuilder setAgentIdentifier(String agentIdentifier);

    String getAgentIdentifier();

    /**
     * The AgentListener associated with this Tether.
     */
    AgentTetherBuilder setAgentListener(AgentListener agentListener);

    AgentListener getAgentListener();

    /**
     * Clear to build a new instance.
     */
    AgentTetherBuilder clear();

    /**
     * Create a new instance of the AgentTether using the supplied values.
     */
    AgentTether build();
}
