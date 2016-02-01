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

package com.bottlerocketstudios.groundcontrol.convenience;

import com.bottlerocketstudios.groundcontrol.agent.Agent;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;

/**
 * Create new ExecutionBuilders for each Agent.
 */
public interface ExecutionBuilderFactory {
    /**
     * Create an ExecutionBuilder for use with an agent in UI or background operations.
     */
    <ResultType, ProgressType> ExecutionBuilder<ResultType, ProgressType> createForAgent(Agent<ResultType, ProgressType> agent);

    /**
     * Create an ExecutionBuilder for use with an agent in UI reattach scenarios.
     */
    <ResultType, ProgressType> ExecutionBuilder<ResultType, ProgressType> createForReattach(Object uiObject, String oneTimeIdentifier, String agentIdentifier, AgentListener<ResultType, ProgressType> listener, AgentPolicy agentPolicy);

    /**
     * Register a policy with the ExecutionBuilderFactory to be available to all ExecutionBuilders.
     */
    void registerPolicy(String policyIdentifier, AgentPolicy policy);

    /**
     * Get a policy in the ExecutionBuilderFactory.
     */
    AgentPolicy getPolicy(String policyIdentifier);
}
