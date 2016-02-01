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

import android.os.Bundle;
import android.text.TextUtils;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.agent.Agent;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicyBuilder;

/**
 * Designed to help with one time agent executions and reattaching once a UI configuration change
 * has occurred.
 */
public class UIOneTimeAgentHelper {

    private final String mSavedStateKey;
    private String mAgentIdentifier;

    /*
     * Create a UIOneTimeAgentHelper that will use the provided savedStateKey.
     * This must be unique to the savedInstanceState Bundle for this UI element.
     */
    public UIOneTimeAgentHelper(String savedStateKey) {
        mSavedStateKey = savedStateKey;
    }

    /**
     * Removes the bypassCache specification if present in the provided policy and returns a new instance.
     */
    public AgentPolicy createValidAgentPolicyFromPolicy(AgentPolicyBuilder agentPolicyBuilder, AgentPolicy agentPolicy) {
        return agentPolicyBuilder.buildUpon(agentPolicy).setBypassCache(false).build();
    }

    /**
     * Reattach to a running agent matching the agentIdentifier in savedInstanceState using the default policy.
     */
    public <ResultType, ProgressType> AgentTether onActivityCreated(Bundle savedInstanceState, AgentExecutor agentExecutor, AgentListener<ResultType, ProgressType> agentListener) {
        return onActivityCreated(savedInstanceState, agentExecutor, null, agentListener);
    }

    /**
     * Reattach to a running agent matching the agentIdentifier in savedInstanceState using the supplied policy.
     */
    public <ResultType, ProgressType> AgentTether onActivityCreated(Bundle savedInstanceState, AgentExecutor agentExecutor, AgentPolicy agentPolicy, AgentListener<ResultType, ProgressType> agentListener) {
        AgentTether agentTether = null;
        if (savedInstanceState != null) {
            mAgentIdentifier = savedInstanceState.getString(mSavedStateKey);
            if (!TextUtils.isEmpty(mAgentIdentifier)) {
                if (agentPolicy == null) {
                    agentTether = agentExecutor.reattachToOneTimeAgent(mAgentIdentifier, agentListener);
                } else {
                    agentTether = agentExecutor.reattachToOneTimeAgent(mAgentIdentifier, agentPolicy, agentListener);
                }
            }
        }
        return agentTether;
    }

    /**
     * Saves the agent identifier in the saved instance state.
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(mSavedStateKey, mAgentIdentifier);
    }

    /**
     * Call when the agent has completed and you are no longer interested in tracking it.
     */
    public void onAgentCompletion() {
        mAgentIdentifier = null;
    }

    /**
     * Call when the agent is about to be executed.
     */
    public void onAgentCreated(String agentIdentifier) {
        mAgentIdentifier = agentIdentifier;
    }

    /**
     * Call when the agent is about to be executed.
     */
    public void onAgentCreated(Agent agent) {
        mAgentIdentifier = agent.getUniqueIdentifier();
    }

}
