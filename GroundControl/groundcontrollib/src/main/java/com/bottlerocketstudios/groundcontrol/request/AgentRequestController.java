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

import com.bottlerocketstudios.groundcontrol.listener.AgentListener;

/**
 * Coordinates delivery of progress and completion messages for AgentListeners as well as notifying
 * AgentListeners that are past their AgentPolicy deadline.
 */
public interface AgentRequestController {
    /**
     * Deliver an individual completion message using the specified LoaderId or by adding it to the
     * background execution queue.
     */
    <ResultType> void deliverCompletion(AgentRequest<ResultType, ?> agentRequest, ResultType result);

    /**
     * Deliver an individual progress message using the specified LoaderId or by adding it to the
     * background execution queue.
     */
    <ProgressType> void deliverProgress(AgentRequest<?, ProgressType> agentRequest, ProgressType progress);

    /**
     * Add a new AgentRequest to the collection that will be notified when the result is delivered.
     */
    void addAgentRequest(AgentRequest agentRequest);

    /**
     * Notify all AgentListeners associated with the agentIdentifier of the result in order of priority.
     */
    <ResultType> void notifyAgentCompletion(String agentIdentifier, ResultType result);

    /**
     * Notify all AgentListeners associated with the agentIdentifier of progress in order of priority.
     */
    <ProgressType> void notifyAgentProgress(String agentIdentifier, ProgressType progress);

    /**
     * Remove the AgentRequest associated with agentIdentifier and supplied AgentListener.
     */
    void removeRequestForAgent(String agentIdentifier, AgentListener agentListener);

    /**
     * Return true if there are AgentRequests pending delivery for the specified agentIdentifier.
     */
    boolean hasActiveRequests(String agentIdentifier);

    /**
     * Walk through collection of AgentRequests and notify those that are past their deadline with
     * a null completion message.
     */
    void notifyPastDeadline();
}
