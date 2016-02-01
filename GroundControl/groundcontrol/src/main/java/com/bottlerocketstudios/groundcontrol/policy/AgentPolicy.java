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

import com.bottlerocketstudios.groundcontrol.executor.JobPriority;

/**
 * Policy for a single AgentExecutor call. This policy applies to the individual request and not
 * other simultaneous requests, though an increase in priority or MaxCacheAge will result in increasing
 * those values for the related execution and cache entries.
 *
 * @see com.bottlerocketstudios.groundcontrol.policy.AgentPolicyBuilder
 */
public class AgentPolicy {
    private final String mCallbackLooperId;
    private final long mPolicyTimeoutMs;
    private final long mParallelCallbackTimeoutMs;
    private final long mMaxCacheAgeMs;
    private final JobPriority mJobPriority;
    private final boolean mParallelBackgroundCallback;
    private final boolean mBypassCache;
    private final boolean mClearCache;

    public AgentPolicy(AgentPolicyBuilder builder) {
        mCallbackLooperId = builder.getCallbackLooperId();
        mPolicyTimeoutMs = builder.getPolicyTimeoutMs();
        mMaxCacheAgeMs = builder.getMaxCacheAgeMs();
        mJobPriority = builder.getJobPriority();
        mParallelBackgroundCallback = builder.isParallelBackgroundCallback();
        mBypassCache = builder.shouldBypassCache();
        mParallelCallbackTimeoutMs = builder.getParallelCallbackTimeoutMs();
        mClearCache = builder.shouldClearCache();
    }

    public String getCallbackLooperId() {
        return mCallbackLooperId;
    }

    public long getPolicyTimeoutMs() {
        return mPolicyTimeoutMs;
    }

    public long getMaxCacheAgeMs() {
        return mMaxCacheAgeMs;
    }

    public JobPriority getJobPriority() {
        return mJobPriority;
    }

    public boolean isParallelBackgroundCallback() {
        return mParallelBackgroundCallback;
    }

    public boolean shouldBypassCache() {
        return mBypassCache;
    }

    public long getParallelCallbackTimeoutMs() {
        return mParallelCallbackTimeoutMs;
    }

    public boolean shouldClearCache() {
        return mClearCache;
    }
}
