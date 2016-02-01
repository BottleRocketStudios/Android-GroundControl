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

import java.util.ArrayList;
import java.util.List;

/**
 * Designed to help with one time agent executions and reattaching once a UI configuration change
 * has occurred. The Collection version will also allow homogeneous Agents which share the same policy
 * and listener instance to be reattached in bulk.
 */
public class UIOneTimeAgentHelperCollection {

    private static final String SAVED_STATE_COUNT = "count";

    private final String mBaseSavedStateKey;
    private final List<String> mAgentIdentifierList;

    /*
     * Create a UIOneTimeAgentHelper that will use the provided savedStateKey.
     * This must be unique to the savedInstanceState Bundle for this UI element.
     */
    public UIOneTimeAgentHelperCollection(String baseSavedStateKey) {
        mBaseSavedStateKey = baseSavedStateKey;
        mAgentIdentifierList = new ArrayList<>();
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
    public <ResultType, ProgressType> List<AgentTether> onActivityCreated(Bundle savedInstanceState, AgentExecutor agentExecutor, AgentListener<ResultType, ProgressType> agentListener) {
        return onActivityCreated(savedInstanceState, agentExecutor, null, agentListener);
    }

    /**
     * Reattach to a running agent matching the agentIdentifier in savedInstanceState using the supplied policy.
     */
    public <ResultType, ProgressType> List<AgentTether> onActivityCreated(Bundle savedInstanceState, AgentExecutor agentExecutor, AgentPolicy agentPolicy, AgentListener<ResultType, ProgressType> agentListener) {
        List<AgentTether> agentTetherList = new ArrayList<>();
        if (savedInstanceState != null) {
            restoreAgentIdentifierList(savedInstanceState);
            for (String agentIdentifier: mAgentIdentifierList) {
                if (agentPolicy == null) {
                    agentTetherList.add(agentExecutor.reattachToOneTimeAgent(agentIdentifier, agentListener));
                } else {
                    agentTetherList.add(agentExecutor.reattachToOneTimeAgent(agentIdentifier, agentPolicy, agentListener));
                }
            }
        }
        return agentTetherList;
    }

    private String getKeyForIndex(int index) {
        return mBaseSavedStateKey + String.valueOf(index);
    }

    private void restoreAgentIdentifierList(Bundle savedInstanceState) {
        mAgentIdentifierList.clear();
        int size = savedInstanceState.getInt(mBaseSavedStateKey + SAVED_STATE_COUNT, 0);
        for (int i = 0; i < size; i++) {
            String agentIdentifier = savedInstanceState.getString(getKeyForIndex(i));
            if (!TextUtils.isEmpty(agentIdentifier)) {
                mAgentIdentifierList.add(agentIdentifier);
            }
        }
    }

    /**
     * Saves the agent identifier in the saved instance state.
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(mBaseSavedStateKey + SAVED_STATE_COUNT, mAgentIdentifierList.size());
        int i = 0;
        for (String agentIdentifier: mAgentIdentifierList) {
            outState.putString(getKeyForIndex(i), agentIdentifier);
            i++;
        }
    }

    /**
     * Call when the agent has completed and you are no longer interested in tracking it.
     */
    public void onAgentCompletion(String agentIdentifier) {
        mAgentIdentifierList.remove(agentIdentifier);
    }

    /**
     * Call when the agent is about to be executed.
     */
    public void onAgentCreated(String agentIdentifier) {
        mAgentIdentifierList.add(agentIdentifier);
    }

    /**
     * Call when the agent is about to be executed.
     */
    public void onAgentCreated(Agent agent) {
        onAgentCreated(agent.getUniqueIdentifier());
    }

}
