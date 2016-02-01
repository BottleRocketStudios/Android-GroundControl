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

package com.bottlerocketstudios.groundcontrol.test.integration;

import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.policy.StandardAgentPolicyBuilder;

public class CancellationWrapperAgent extends AbstractAgent<Boolean, Void> {
    @Override
    public String getUniqueIdentifier() {
        return CancellationWrapperAgent.class.getCanonicalName();
    }

    @Override
    public void cancel() {
        getAgentListener().onCompletion(getUniqueIdentifier(), false);
    }

    @Override
    public void onProgressUpdateRequested() {}

    @Override
    public void run() {
        AgentPolicy agentPolicy = (new StandardAgentPolicyBuilder()).setParallelBackgroundCallback(true).build();
        getAgentExecutor().runAgent(new CancellationAgent(), agentPolicy, new AgentListener<Boolean, Void>() {
            @Override
            public void onCompletion(String agentIdentifier, Boolean result) {
                getAgentListener().onCompletion(getUniqueIdentifier(), result);
            }

            @Override
            public void onProgress(String agentIdentifier, Void progress) {}
        });
    }
}
