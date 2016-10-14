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

import android.text.TextUtils;
import android.util.Log;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.agent.Agent;
import com.bottlerocketstudios.groundcontrol.executor.JobPriority;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.looper.LooperController;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicyBuilder;
import com.bottlerocketstudios.groundcontrol.policy.StandardAgentPolicyBuilder;
import com.bottlerocketstudios.groundcontrol.tether.AgentTether;

import java.util.concurrent.TimeUnit;

/**
 * Standard implementation of ExecutionBuilder. New instances should be created by the StandardExecutionBuilderFactory.
 */
public class StandardExecutionBuilder<ResultType, ProgressType> implements ExecutionBuilder<ResultType, ProgressType> {

    private static final long DEFAULT_ONE_TIME_CACHE_AGE_MS = TimeUnit.MINUTES.toMillis(2);

    private static final String TAG = StandardExecutionBuilder.class.getSimpleName();

    private final Agent<ResultType, ProgressType> mAgent;
    private final String mAgentExecutorId;
    private final Class<? extends AgentPolicyBuilder> mAgentPolicyBuilderClass;
    private final AgentPolicyCache mAgentPolicyCache;
    private final boolean mReattach;
    private final String mReattachAgentIdentifier;

    private AgentListener<ResultType, ProgressType> mAgentListener;
    private AgentPolicy mAgentPolicy;
    private AgentPolicyBuilder mAgentPolicyBuilder;
    private Object mUiObject;
    private String mOneTimeId;

    /**
     * Constructor for use with normal Agent creation.
     */
    public StandardExecutionBuilder(String agentExecutorId, Agent<ResultType, ProgressType> agent, Class<? extends AgentPolicyBuilder> agentPolicyBuilderClass, AgentPolicyCache agentPolicyCache) {
        mAgentExecutorId = agentExecutorId;
        mAgent = agent;
        mAgentPolicyBuilderClass = agentPolicyBuilderClass;
        mAgentPolicyCache = agentPolicyCache;
        mReattach = false;
        mReattachAgentIdentifier = null;
    }

    /**
     * Constructor for use with UI reattaching to an Agent.
     */
    public StandardExecutionBuilder(String agentExecutorId, Class<? extends AgentPolicyBuilder> agentPolicyBuilderClass, AgentPolicyCache agentPolicyCache, Object uiObject, String oneTimeId, String reattachAgentIdentifier, AgentListener<ResultType, ProgressType> agentListener, AgentPolicy agentPolicy) {
        mAgentExecutorId = agentExecutorId;
        mAgent = null;
        mAgentPolicyBuilderClass = agentPolicyBuilderClass;
        mAgentPolicyCache = agentPolicyCache;
        mReattach = true;
        mReattachAgentIdentifier = reattachAgentIdentifier;
        mAgentListener = agentListener;
        mAgentPolicy = agentPolicy;
        ui(uiObject);
        oneTime(oneTimeId);
    }

    /**
     * Creates an AgentPolicyBuilder using the AgentPolicyBuilder class supplied.
     */
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

    /**
     * Gets the AgentPolicyBuilder instance or starts to build upon the provided AgentPolicy.
     */
    private AgentPolicyBuilder getAgentPolicyBuilder() {
        if (mAgentPolicyBuilder == null) {
            mAgentPolicyBuilder = createAgentPolicyBuilder();
            if (hasAgentPolicy()) {
                mAgentPolicyBuilder.buildUpon(getAgentPolicy());
            }
        }
        return mAgentPolicyBuilder;
    }

    private boolean hasAgentPolicyBuilder() {
        return mAgentPolicyBuilder != null;
    }

    private AgentPolicy getAgentPolicy() {
        return mAgentPolicy;
    }

    private boolean hasAgentPolicy() {
        return mAgentPolicy != null;
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> policy(AgentPolicy agentPolicy) {
        if (hasAgentPolicyBuilder() || hasAgentPolicy()) {
            throw new IllegalStateException("You must supply a policy before other operations as they will potentially build upon the supplied policy.");
        }
        mAgentPolicy = agentPolicy;
        return this;
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> policy(String agentPolicyIdentifier) {
        return policy(mAgentPolicyCache.get(agentPolicyIdentifier));
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> uiPolicy() {
        return policy(AgentPolicyCache.POLICY_IDENTIFIER_UI);
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> bgParallelPolicy() {
        return policy(AgentPolicyCache.POLICY_IDENTIFIER_BG_PARALLEL);
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> bgSerialPolicy() {
        return policy(AgentPolicyCache.POLICY_IDENTIFIER_BG_SERIAL);
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> uiCallback(AgentListener<ResultType, ProgressType> agentListener) {
        if (mAgentListener != null) {
            throw new IllegalStateException("Cannot specify more than one listener");
        }

        if (!hasAgentPolicy()) {
            uiPolicy();
        }

        if (!LooperController.UI_LOOPER_ID.equals(getAgentPolicy().getCallbackLooperId())) {
            getAgentPolicyBuilder().setCallbackLooperId(LooperController.UI_LOOPER_ID);
        }

        mAgentListener = agentListener;
        return this;
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> bgParallelCallback(AgentListener<ResultType, ProgressType> agentListener) {
        if (mAgentListener != null) {
            throw new IllegalStateException("Cannot specify more than one listener");
        }

        if (!hasAgentPolicy()) {
            bgParallelPolicy();
        }

        if (!getAgentPolicy().isParallelBackgroundCallback()) {
            getAgentPolicyBuilder().setParallelBackgroundCallback(true);
        }

        mAgentListener = agentListener;
        return this;
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> bgSerialCallback(AgentListener<ResultType, ProgressType> agentListener) {
        if (mAgentListener != null) {
            throw new IllegalStateException("Cannot specify more than one listener");
        }

        if (!hasAgentPolicy()) {
            bgSerialPolicy();
        }

        String backgroundLooperId = AgentExecutor.getInstance(mAgentExecutorId).getBackgroundLooperId();
        if (!backgroundLooperId.equals(getAgentPolicy().getCallbackLooperId())) {
            getAgentPolicyBuilder().setCallbackLooperId(backgroundLooperId);
        }

        mAgentListener = agentListener;
        return this;
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> priority(JobPriority jobPriority) {
        if (!hasAgentPolicy()) {
            throw new IllegalStateException("You must establish a baseline policy first see policy method documentation.");
        }

        if (!getAgentPolicy().getJobPriority().equals(jobPriority)) {
            getAgentPolicyBuilder().setJobPriority(jobPriority);
        }
        return this;
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> bypassCache(boolean bypassCache) {
        if (!hasAgentPolicy()) {
            throw new IllegalStateException("You must establish a baseline policy first see policy method documentation.");
        }

        if (getAgentPolicy().shouldBypassCache() != bypassCache) {
            getAgentPolicyBuilder().setBypassCache(bypassCache);
        }
        return this;
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> clearCache(boolean clearCache) {
        if (!hasAgentPolicy()) {
            throw new IllegalStateException("You must establish a baseline policy first see policy method documentation.");
        }

        if (getAgentPolicy().shouldClearCache() != clearCache) {
            getAgentPolicyBuilder().setClearCache(clearCache);
        }
        return this;
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> cacheAgeMs(long cacheAgeMs) {
        if (!hasAgentPolicy()) {
            throw new IllegalStateException("You must establish a baseline policy first see policy method documentation.");
        }

        if (getAgentPolicy().getMaxCacheAgeMs() != cacheAgeMs) {
            getAgentPolicyBuilder().setMaxCacheAgeMs(cacheAgeMs);
        }
        return this;
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> disableCache() {
        bypassCache(true);
        cacheAgeMs(0);
        return this;
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> timeout(long timeoutMs) {
        if (!hasAgentPolicy()) {
            throw new IllegalStateException("You must establish a baseline policy first see policy method documentation.");
        }

        if (getAgentPolicy().getPolicyTimeoutMs() != timeoutMs) {
            getAgentPolicyBuilder().setPolicyTimeoutMs(timeoutMs);
        }
        return this;
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> parallelCallbackTimeout(long timeoutMs) {
        if (!hasAgentPolicy()) {
            throw new IllegalStateException("You must establish a baseline policy first see policy method documentation.");
        }

        if (getAgentPolicy().getParallelCallbackTimeoutMs() != timeoutMs) {
            getAgentPolicyBuilder().setParallelCallbackTimeoutMs(timeoutMs);
        }
        return this;
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> oneTime(String oneTimeIdentifier) {
        mOneTimeId = oneTimeIdentifier;
        return this;
    }

    @Override
    public ExecutionBuilder<ResultType, ProgressType> ui(Object object) {
        mUiObject = object;
        return this;
    }

    @Override
    public AgentTether execute() {
        build();
        AgentTether agentTether;
        AgentPolicy agentPolicy = getAgentPolicy();
        if (mReattach) {
            agentTether = AgentExecutor.getInstance(mAgentExecutorId).reattachToOneTimeAgent(mReattachAgentIdentifier, agentPolicy, mAgentListener);
        } else {
            agentTether = AgentExecutor.getInstance(mAgentExecutorId).runAgent(mAgent, agentPolicy, mAgentListener);
        }

        if (mUiObject != null) {
            GroundControl.updateUiInformationContainer(mAgentExecutorId, mUiObject, agentTether, agentPolicy, mOneTimeId);
        }
        return agentTether;
    }

    private void build() {

        if (mReattach) {
            //Enforce sensible policy for one-time reattach so that the cache is used if available.
            bypassCache(false);
            clearCache(false);
        }

        if (!TextUtils.isEmpty(mOneTimeId)) {
            //Enforce sensible policy for one-time executions so that caching will occur.
            if (getAgentPolicy().getMaxCacheAgeMs() <= 0) {
                cacheAgeMs(DEFAULT_ONE_TIME_CACHE_AGE_MS);
            }
        }

        //If a builder has been made due to policy changes, build the policy.
        if (hasAgentPolicyBuilder()) {
            mAgentPolicy = getAgentPolicyBuilder().build();
        }

        //No listener, no dice.
        if (mAgentListener == null) {
            throw new IllegalStateException("You must set a listener");
        }
    }

}
