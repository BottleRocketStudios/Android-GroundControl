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
 *
 */

package com.bottlerocketstudios.groundcontrol.agent;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;

/**
 * Unit of execution which knows how to obtain its data either synchronously or asynchronously and
 * deliver that data to a listener with optional progress updates.
 */
public interface Agent<ResultType, ProgressType> extends Runnable {
    /**
     * Provide a globally unique string to identify this agent. All identical agents will
     * be executed one at a time. If available, cached values for Agents with the same unique
     * identifier will be delivered.
     */
    String getUniqueIdentifier();

    /**
     * Stop executing ASAP, your result is no longer needed. If this is a meta-Agent cancel any
     * associated AgentTethers you have created. This will also be called after cancelTimeoutMs
     * has elapsed.
     */
    void cancel();

    /**
     * Maximum amount of time in milliseconds to allow an Agent's run operation to run before being interrupted.
     * Using a very large number with a repeated task that fails to complete may result in exhausted Thread
     * pool. Must be greater than getCancelTimeoutMs(). <strong>This value will be read before execution.</strong>
     */
    long getRunTimeoutMs();

    /**
     * Maximum amount of time in milliseconds starting at run() before cancel() is called if incomplete.
     * <strong>This value will be read before execution.</strong>
     */
    long getCancelTimeoutMs();

    /**
     * Maximum amount of time in milliseconds starting at run() to being discarded even if incomplete. Must be
     * greater than getCancelTimeoutMs() and getRunTimeoutMs(). <strong>This value will be read before execution.</strong>
     */
    long getMaximumTimeoutMs();

    /**
     * Set the listener for the agent. This listener must be called when the agent has completed or has
     * progress to deliver.
     */
    void setAgentListener(AgentListener<ResultType, ProgressType> agentListener);

    /**
     * Set the agentExecutor that will be running this agent. This is guaranteed to be populated before run().
     * This should be used to spawn further Agent executions and ensures that an Agent could be used with
     * multiple executors and not be tightly coupled to any specific instance.
     *
     * <p><strong>You must not perform work in this operation other than storing the reference.</strong></p>
     */
    void setAgentExecutor(AgentExecutor agentExecutor);

    /**
     * At the Agent's next convenience, deliver a progress update if applicable. This will be called when
     * an already running Agent has a new AgentRequest queued.
     */
    void onProgressUpdateRequested();
}
