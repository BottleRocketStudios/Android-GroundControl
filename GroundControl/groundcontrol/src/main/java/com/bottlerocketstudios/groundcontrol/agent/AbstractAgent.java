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

package com.bottlerocketstudios.groundcontrol.agent;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;

import java.util.concurrent.TimeUnit;

/**
 * Simple abstract implementation with timeout and listener storage.
 */
public abstract class AbstractAgent<ResultType, ProgressType> implements Agent<ResultType, ProgressType> {

    protected static final long DEFAULT_CANCEL_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(2);
    protected static final long DEFAULT_RUN_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(3);
    protected static final long DEFAULT_MAXIMUM_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(5);

    private AgentListener<ResultType, ProgressType> mAgentListener;
    private long mRunTimeoutMs;
    private long mCancelTimeoutMs;
    private long mMaximumTimeoutMs;
    private AgentExecutor mAgentExecutor;

    protected AbstractAgent() {
        mRunTimeoutMs = DEFAULT_RUN_TIMEOUT_MS;
        mCancelTimeoutMs = DEFAULT_CANCEL_TIMEOUT_MS;
        mMaximumTimeoutMs = DEFAULT_MAXIMUM_TIMEOUT_MS;
    }

    @Override
    public long getRunTimeoutMs() {
        return mRunTimeoutMs;
    }

    /**
     * Specify the run timeout for this Agent in milliseconds. Refer to {@link Agent#getRunTimeoutMs()} and {@link AbstractAgent#DEFAULT_RUN_TIMEOUT_MS}
     */
    public void setRunTimeoutMs(long runTimeoutMs) throws IllegalArgumentException {
        if (runTimeoutMs <= 0) throw new IllegalArgumentException("Timeout must be > 0");
        mRunTimeoutMs = runTimeoutMs;
    }

    @Override
    public long getCancelTimeoutMs() {
        return mCancelTimeoutMs;
    }

    /**
     * Specify the cancel timeout for this Agent in milliseconds. Refer to {@link Agent#getCancelTimeoutMs()} and {@link AbstractAgent#DEFAULT_CANCEL_TIMEOUT_MS}
     */
    public void setCancelTimeoutMs(long cancelTimeoutMs) throws IllegalArgumentException {
        if (cancelTimeoutMs <= 0) throw new IllegalArgumentException("Timeout must be > 0");
        mCancelTimeoutMs = cancelTimeoutMs;
    }

    @Override
    public long getMaximumTimeoutMs() {
        return mMaximumTimeoutMs;
    }

    /**
     * Specify the maximum timeout for this Agent in milliseconds. Refer to {@link Agent#getMaximumTimeoutMs()} and {@link AbstractAgent#DEFAULT_MAXIMUM_TIMEOUT_MS}
     */
    public void setMaximumTimeoutMs(long maximumTimeoutMs) throws IllegalArgumentException {
        if (maximumTimeoutMs <= 0) throw new IllegalArgumentException("Timeout must be > 0");
        mMaximumTimeoutMs = maximumTimeoutMs;
    }

    /**
     * Get the AgentListener associated with this Agent's execution.
     */
    protected AgentListener<ResultType, ProgressType> getAgentListener() {
        return mAgentListener;
    }

    @Override
    public void setAgentListener(AgentListener<ResultType, ProgressType> agentListener) throws NullPointerException {
        if (agentListener == null) throw new NullPointerException("AgentListener cannot be null");
        mAgentListener = agentListener;
    }

    /**
     * Get the AgentExecutor associated with this Agent's execution. Use this when starting other Agents from this Agent.
     */
    public AgentExecutor getAgentExecutor() {
        return mAgentExecutor;
    }

    @Override
    public void setAgentExecutor(AgentExecutor agentExecutor) throws NullPointerException {
        if (agentExecutor == null) throw new NullPointerException("AgentExecutor cannot be null");
        mAgentExecutor = agentExecutor;
    }

    /**
     * Call to notify the listener of completion.
     */
    protected void notifyCompletion(ResultType result) {
        getAgentListener().onCompletion(getUniqueIdentifier(), result);
    }

    /**
     * Call to notify the listener of progress.
     */
    protected void notifyProgress(ProgressType progress) {
        getAgentListener().onProgress(getUniqueIdentifier(), progress);
    }
}
