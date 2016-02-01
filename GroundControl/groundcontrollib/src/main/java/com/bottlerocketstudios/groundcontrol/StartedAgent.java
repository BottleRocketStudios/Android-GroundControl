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

package com.bottlerocketstudios.groundcontrol;

import android.os.SystemClock;

import com.bottlerocketstudios.groundcontrol.agent.Agent;
import com.bottlerocketstudios.groundcontrol.executor.Job;
import com.bottlerocketstudios.groundcontrol.executor.JobExecutionListener;
import com.bottlerocketstudios.groundcontrol.request.AgentRequest;

/**
 * Container for Agents which have been sent to the Agent execution pool and not yet completed or
 * timed out.
 */
class StartedAgent implements JobExecutionListener {

    private final Agent mAgent;
    private final Job mJob;
    private long mMaximumDeadline;
    private long mCancellationDeadline;
    private long mInitialCacheAgeMs;
    private boolean mCancelled;

    private StartedAgent(Agent agent, Job job, long initialCacheAgeMs) {
        mAgent = agent;
        mJob = job;
        mInitialCacheAgeMs = initialCacheAgeMs;
    }

    public Agent getAgent() {
        return mAgent;
    }

    public Job getJob() {
        return mJob;
    }

    public long getInitialCacheAgeMs() {
        return mInitialCacheAgeMs;
    }

    public void setInitialCacheAgeMs(long initialCacheAgeMs) {
        mInitialCacheAgeMs = initialCacheAgeMs;
    }

    private long getTime() {
        return SystemClock.uptimeMillis();
    }

    public boolean isPastMaximumDeadline() {
        return mMaximumDeadline > 0 && getTime() > mMaximumDeadline;
    }

    public boolean isPastCancellationDeadline() {
        return mCancellationDeadline > 0 && getTime() > mCancellationDeadline;
    }

    @Override
    public void onJobExecuted(Job job) {
        mMaximumDeadline = getTime() + mAgent.getMaximumTimeoutMs();
        mCancellationDeadline = getTime() + mAgent.getCancelTimeoutMs();
    }

    public void requestProgressUpdate() {
        mAgent.onProgressUpdateRequested();
    }

    public void cancel() {
        mCancelled = true;
        mAgent.cancel();
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    @Override
    public String toString() {
        return mAgent.toString() + " " + super.toString();
    }

    /**
     * FactoryMethod to enforce requirements.
     */
    public static <ResultType, ProgressType> StartedAgent newStartedAgent(AgentRequest<ResultType, ProgressType> agentRequest, Agent<ResultType, ProgressType> agent, Job agentJob) {
        if (agent.getRunTimeoutMs() <= agent.getCancelTimeoutMs()) {
            throw new IllegalArgumentException("An Agent's runTimeout must be higher than the cancellation timeout.");
        }

        if (agent.getMaximumTimeoutMs() <= agent.getRunTimeoutMs() || agent.getMaximumTimeoutMs() < agent.getCancelTimeoutMs()) {
            throw new IllegalArgumentException("An Agent's maximumTimeout must be higher than both run and cancellation timeouts.");
        }

        return new StartedAgent(
                agent,
                agentJob,
                agentRequest.getMaxCacheAgeMs());
    }

}
