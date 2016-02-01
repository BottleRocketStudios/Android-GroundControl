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

import android.util.Log;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.agent.Agent;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.looper.LooperController;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicyBuilder;
import com.bottlerocketstudios.groundcontrol.policy.StandardAgentPolicyBuilder;

/**
 * Creates StandardExecutionBuilder instances for each new agent.
 */
public class StandardExecutionBuilderFactory implements ExecutionBuilderFactory {
    private static final String TAG = StandardExecutionBuilderFactory.class.getSimpleName();

    private final Class<? extends AgentPolicyBuilder> mAgentPolicyBuilderClass;
    private final String mAgentExecutorId;
    private final AgentPolicyCache mAgentPolicyCache = new AgentPolicyCache();

    /**
     * Create a new StandardExecutionBuilderFactory with the supplied default AgentPolicies for the
     * specified AgentExecutor.
     *
     * @see GroundControl#setExecutionBuilderFactory(String, ExecutionBuilderFactory)
     */
    public StandardExecutionBuilderFactory(Class <? extends AgentPolicyBuilder> agentPolicyBuilderClass, String agentExecutorId, AgentPolicy uiPolicy, AgentPolicy backgroundSerialPolicy, AgentPolicy backgroundParallelPolicy) {
        mAgentPolicyBuilderClass = agentPolicyBuilderClass;
        mAgentExecutorId = agentExecutorId;
        mAgentPolicyCache.put(AgentPolicyCache.POLICY_IDENTIFIER_UI, uiPolicy);
        mAgentPolicyCache.put(AgentPolicyCache.POLICY_IDENTIFIER_BG_SERIAL, backgroundSerialPolicy);
        mAgentPolicyCache.put(AgentPolicyCache.POLICY_IDENTIFIER_BG_PARALLEL, backgroundParallelPolicy);
    }

    /**
     * Create a new StandardExecutionBuilderFactory for the specified AgentExecutor. Default AgentPolicies
     * will be generated using the StandardAgentPolicyBuilder.
     *
     * @see GroundControl#setExecutionBuilderFactory(String, ExecutionBuilderFactory)
     */
    public StandardExecutionBuilderFactory(String agentExecutorId) {
        this(StandardAgentPolicyBuilder.class, agentExecutorId);
    }

    /**
     * Create a new StandardExecutionBuilderFactory for the specified AgentExecutor. Default AgentPolicies
     * will be generated using the supplied AgentPolicyBuilderClass.
     *
     * @see GroundControl#setExecutionBuilderFactory(String, ExecutionBuilderFactory)
     */
    public StandardExecutionBuilderFactory(Class <? extends AgentPolicyBuilder> agentPolicyBuilderClass, String agentExecutorId) {
        mAgentPolicyBuilderClass = agentPolicyBuilderClass;
        mAgentExecutorId = agentExecutorId;

        //Create default policies.
        AgentPolicyBuilder agentPolicyBuilder = createAgentPolicyBuilder();
        mAgentPolicyCache.put(AgentPolicyCache.POLICY_IDENTIFIER_UI,
                agentPolicyBuilder
                        .clear()
                        .setCallbackLooperId(LooperController.UI_LOOPER_ID)
                        .build());
        mAgentPolicyCache.put(AgentPolicyCache.POLICY_IDENTIFIER_BG_SERIAL,
                agentPolicyBuilder
                        .clear()
                        .setCallbackLooperId(AgentExecutor.getInstance(mAgentExecutorId).getBackgroundLooperId())
                        .build());
        mAgentPolicyCache.put(AgentPolicyCache.POLICY_IDENTIFIER_BG_PARALLEL,
                agentPolicyBuilder
                        .clear()
                        .setParallelBackgroundCallback(true)
                        .build());
    }

    private AgentPolicyBuilder createAgentPolicyBuilder() {
        AgentPolicyBuilder agentPolicyBuilder = null;
        try {
            agentPolicyBuilder = mAgentPolicyBuilderClass.newInstance();
        } catch (InstantiationException e) {
            Log.e(TAG, "Caught java.lang.InstantiationException", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Caught java.lang.IllegalAccessException", e);
        }
        if (agentPolicyBuilder == null) agentPolicyBuilder = new StandardAgentPolicyBuilder();
        return agentPolicyBuilder;
    }

    @Override
    public <ResultType, ProgressType> ExecutionBuilder<ResultType, ProgressType> createForAgent(Agent<ResultType, ProgressType> agent) {
        return new StandardExecutionBuilder<>(mAgentExecutorId, agent, mAgentPolicyBuilderClass, mAgentPolicyCache);
    }

    @Override
    public <ResultType, ProgressType> ExecutionBuilder<ResultType, ProgressType> createForReattach(Object uiObject, String oneTimeIdentifier, String agentIdentifier, AgentListener<ResultType, ProgressType> listener, AgentPolicy agentPolicy) {
        return new StandardExecutionBuilder<>(mAgentExecutorId, mAgentPolicyBuilderClass, mAgentPolicyCache, uiObject, oneTimeIdentifier, agentIdentifier, listener, agentPolicy);
    }

    @Override
    public void registerPolicy(String policyIdentifier, AgentPolicy policy) {
        mAgentPolicyCache.put(policyIdentifier, policy);
    }

    @Override
    public AgentPolicy getPolicy(String policyIdentifier) {
        return mAgentPolicyCache.get(policyIdentifier);
    }
}
