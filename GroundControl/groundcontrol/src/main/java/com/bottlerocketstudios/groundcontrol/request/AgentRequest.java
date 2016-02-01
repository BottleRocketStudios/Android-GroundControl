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

package com.bottlerocketstudios.groundcontrol.request;

import android.os.SystemClock;

import com.bottlerocketstudios.groundcontrol.agent.Agent;
import com.bottlerocketstudios.groundcontrol.executor.JobPriority;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;

/**
 * A client's request details collected into a single object. This houses all data associated with
 * the single Agent submission associated with that client.
 */
public class AgentRequest<ResultType, ProgressType> {
    private final Agent<ResultType, ProgressType> mAgent;
    private final AgentListener<ResultType, ProgressType> mAgentListener;
    private final AgentPolicy mAgentPolicy;
    private final long mDeadline;

    public AgentRequest(Agent<ResultType, ProgressType> agent, AgentListener<ResultType, ProgressType> agentListener, AgentPolicy agentPolicy) {
        mAgent = agent;
        mAgentListener = agentListener;
        mAgentPolicy = agentPolicy;
        mDeadline = getTime() + mAgentPolicy.getPolicyTimeoutMs();
    }

    private long getTime() {
        return SystemClock.uptimeMillis();
    }

    public String getAgentIdentifier() {
        return getAgent().getUniqueIdentifier();
    }

    public Agent<ResultType, ProgressType> getAgent() {
        return mAgent;
    }

    public AgentListener<ResultType, ProgressType> getAgentListener() {
        return mAgentListener;
    }

    public boolean isPastDeadline() {
        return mDeadline < getTime();
    }

    /********* Agent Policy Proxy **********/

    public String getCallbackLooperId() {
        return mAgentPolicy.getCallbackLooperId();
    }

    public long getPolicyTimeoutMs() {
        return mAgentPolicy.getPolicyTimeoutMs();
    }

    public long getMaxCacheAgeMs() {
        return mAgentPolicy.getMaxCacheAgeMs();
    }

    public JobPriority getJobPriority() {
        return mAgentPolicy.getJobPriority();
    }

    public boolean shouldBypassCache() {
        return mAgentPolicy.shouldBypassCache();
    }

    public long getParallelCallbackTimeoutMs() {
        return mAgentPolicy.getParallelCallbackTimeoutMs();
    }

    public boolean shouldClearCache() {
        return mAgentPolicy.shouldClearCache();
    }
}
