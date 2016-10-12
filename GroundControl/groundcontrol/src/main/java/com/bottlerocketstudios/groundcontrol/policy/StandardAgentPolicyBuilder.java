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

package com.bottlerocketstudios.groundcontrol.policy;

import android.text.TextUtils;

import com.bottlerocketstudios.groundcontrol.executor.JobPriority;
import com.bottlerocketstudios.groundcontrol.looper.LooperController;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of AgentPolicyBuilder that allows overriding default values and enforces business
 * rules.
 */
public class StandardAgentPolicyBuilder implements AgentPolicyBuilder {

    private static final String DEFAULT_CALLBACK_LOOPER_ID = LooperController.UI_LOOPER_ID;
    private static final long DEFAULT_POLICY_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(5);
    private static final long DEFAULT_CACHE_AGE_MS = TimeUnit.MINUTES.toMillis(2);
    private static final long DEFAULT_PARALLEL_CALLBACK_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(6);
    private static final JobPriority DEFAULT_JOB_PRIORITY = JobPriority.NORMAL;
    private static final boolean DEFAULT_PARALLEL_BACKGROUND_CALLBACK = false;

    private String mDefaultCallbackLooperId;
    private long mDefaultPolicyTimeoutMs;
    private long mDefaultCacheAgeMs;
    private long mDefaultParallelCallbackTimeoutMs;
    private JobPriority mDefaultJobPriority;
    private boolean mDefaultParallelBackgroundCallback;

    private String mCallbackLooperId;
    private long mPolicyTimeoutMs;
    private long mMaxCacheAgeMs;
    private long mParallelCallbackTimeoutMs;
    private JobPriority mJobPriority;
    private Boolean mParallelBackgroundCallback;
    private boolean mBypassCache;
    private boolean mClearCache;

    private boolean mCallbackSet;

    public StandardAgentPolicyBuilder() {
        mDefaultCallbackLooperId = DEFAULT_CALLBACK_LOOPER_ID;
        mDefaultPolicyTimeoutMs = DEFAULT_POLICY_TIMEOUT_MS;
        mDefaultCacheAgeMs = DEFAULT_CACHE_AGE_MS;
        mDefaultParallelCallbackTimeoutMs = DEFAULT_PARALLEL_CALLBACK_TIMEOUT_MS;
        mDefaultJobPriority = DEFAULT_JOB_PRIORITY;
        mDefaultParallelBackgroundCallback = DEFAULT_PARALLEL_BACKGROUND_CALLBACK;
        clear();
    }

    public void setDefaultCallbackLooperId(String defaultCallbackLooperId) {
        if (mDefaultParallelBackgroundCallback && defaultCallbackLooperId != null) {
            throw new IllegalArgumentException("You must setDefaultParallelBackgroundCallback(false) first");
        }
        mDefaultCallbackLooperId = defaultCallbackLooperId;
    }

    public void setDefaultPolicyTimeoutMs(long defaultPolicyTimeoutMs) {
        mDefaultPolicyTimeoutMs = defaultPolicyTimeoutMs;
    }

    public void setDefaultCacheAgeMs(long defaultCacheAgeMs) {
        mDefaultCacheAgeMs = defaultCacheAgeMs;
    }

    public void setDefaultParallelCallbackTimeoutMs(long defaultParallelCallbackTimeoutMs) {
        mDefaultParallelCallbackTimeoutMs = defaultParallelCallbackTimeoutMs;
    }

    public void setDefaultJobPriority(JobPriority defaultJobPriority) {
        mDefaultJobPriority = defaultJobPriority;
    }

    public void setDefaultParallelBackgroundCallback(boolean defaultParallelBackgroundCallback) {
        if (mDefaultCallbackLooperId != null && defaultParallelBackgroundCallback) {
            throw new IllegalArgumentException("You must setDefaultCallbackLooperId(null) first");
        }
        mDefaultParallelBackgroundCallback = defaultParallelBackgroundCallback;
    }

    @Override
    public AgentPolicyBuilder setCallbackLooperId(String callbackLooperId) {
        if (mCallbackSet) {
            throw new IllegalArgumentException("Cannot call either setParallelBackgroundCallback or setCallbackLooperId again without clearing");
        } else if (TextUtils.isEmpty(callbackLooperId)) {
            throw new IllegalArgumentException("callbackLooperId cannot be empty or null. Maybe you were trying to setParallelBackgroundCallback(true)?");
        }
        mCallbackLooperId = callbackLooperId;
        mCallbackSet = true;
        if (isBackgroundLooper(callbackLooperId)) {
            disableCache();
        }
        return this;
    }

    @Override
    public String getCallbackLooperId() {
        return mCallbackLooperId;
    }

    @Override
    public AgentPolicyBuilder setParallelBackgroundCallback(boolean parallelBackgroundCallback) {
        if (mCallbackSet) {
            throw new IllegalArgumentException("Cannot call either setParallelBackgroundCallback or setCallbackLooperId again without clearing");
        } else if (!parallelBackgroundCallback) {
            throw new IllegalArgumentException("Do not setParallelBackgroundCallback(false) instead call setCallbackLooperId");
        }
        mParallelBackgroundCallback = true;
        mCallbackSet = true;
        disableCache();
        return this;
    }

    @Override
    public boolean isParallelBackgroundCallback() {
        return mParallelBackgroundCallback;
    }

    @Override
    public AgentPolicyBuilder setPolicyTimeoutMs(long policyTimeoutMs) {
        mPolicyTimeoutMs = policyTimeoutMs;
        return this;
    }

    @Override
    public long getPolicyTimeoutMs() {
        return mPolicyTimeoutMs;
    }

    @Override
    public AgentPolicyBuilder setMaxCacheAgeMs(long maxCacheAgeMs) {
        mMaxCacheAgeMs = maxCacheAgeMs;
        return this;
    }

    @Override
    public long getMaxCacheAgeMs() {
        return mMaxCacheAgeMs;
    }

    @Override
    public long getParallelCallbackTimeoutMs() {
        return mParallelCallbackTimeoutMs;
    }

    @Override
    public AgentPolicyBuilder setClearCache(boolean clearCache) {
        mClearCache = clearCache;
        return this;
    }

    @Override
    public boolean shouldClearCache() {
        return mClearCache;
    }

    @Override
    public AgentPolicyBuilder setParallelCallbackTimeoutMs(long parallelCallbackTimeoutMs) {
        mParallelCallbackTimeoutMs = parallelCallbackTimeoutMs;
        return this;
    }

    @Override
    public AgentPolicyBuilder setJobPriority(JobPriority jobPriority) {
        mJobPriority = jobPriority;
        return this;
    }

    @Override
    public JobPriority getJobPriority() {
        return mJobPriority;
    }

    @Override
    public AgentPolicyBuilder setBypassCache(boolean bypassCache) {
        mBypassCache = bypassCache;
        return this;
    }

    @Override
    public boolean shouldBypassCache() {
        return mBypassCache;
    }

    @Override
    public AgentPolicyBuilder clear() {
        mCallbackLooperId = null;
        mParallelBackgroundCallback = false;
        mCallbackSet = false;
        setPolicyTimeoutMs(0);
        setMaxCacheAgeMs(-1);
        setParallelCallbackTimeoutMs(0);
        setJobPriority(null);
        setBypassCache(false);
        setClearCache(false);
        return this;
    }

    @Override
    public AgentPolicyBuilder buildUpon(AgentPolicy agentPolicy) {
        clear();

        if (agentPolicy == null) {
            //When given a null policy to build upon, just clear to defaults.
            return this;
        }

        if (agentPolicy.isParallelBackgroundCallback()) {
            setParallelBackgroundCallback(true);
        } else {
            setCallbackLooperId(agentPolicy.getCallbackLooperId());
        }
        setPolicyTimeoutMs(agentPolicy.getPolicyTimeoutMs());
        setMaxCacheAgeMs(agentPolicy.getMaxCacheAgeMs());
        setParallelCallbackTimeoutMs(agentPolicy.getParallelCallbackTimeoutMs());
        setJobPriority(agentPolicy.getJobPriority());
        setBypassCache(agentPolicy.shouldBypassCache());
        setClearCache(agentPolicy.shouldClearCache());
        return this;
    }

    @Override
    public AgentPolicy build() {
        if (!mCallbackSet) {
            if (mDefaultParallelBackgroundCallback) {
                setParallelBackgroundCallback(true);
            } else {
                setCallbackLooperId(mDefaultCallbackLooperId);
            }
        }

        if (getPolicyTimeoutMs() <= 0) {
            setPolicyTimeoutMs(mDefaultPolicyTimeoutMs);
        }

        if (getMaxCacheAgeMs() < 0) {
            setMaxCacheAgeMs(mDefaultCacheAgeMs);
        }

        if (getParallelCallbackTimeoutMs() <= 0) {
            setParallelCallbackTimeoutMs(mDefaultParallelCallbackTimeoutMs);
        }

        if (getJobPriority() == null) {
            setJobPriority(mDefaultJobPriority);
        }

        validatePolicy();

        return new AgentPolicy(this);
    }

    @Override
    public AgentPolicyBuilder disableCache() {
        setBypassCache(true);
        setMaxCacheAgeMs(0);
        return this;
    }

    private void validatePolicy() {
        if (isUsingCacheOnBackgroundRequest()) {
            throw new IllegalStateException("Cannot build a policy that will use cache for a background delivery. Cache should only be used for UI Looper delivery. Call setBypassCache(true).");
        }

        if (shouldClearCache()) {
            //Cannot clear cache without also bypassing it.
            setBypassCache(true);
        }

    }

    private boolean isBackgroundLooper(String callbackLooperId) {
        return !TextUtils.equals(LooperController.UI_LOOPER_ID, callbackLooperId);
    }

    private boolean isUsingCacheOnBackgroundRequest() {
        return !shouldBypassCache() && (isParallelBackgroundCallback() || isBackgroundLooper(getCallbackLooperId()));
    }
}
