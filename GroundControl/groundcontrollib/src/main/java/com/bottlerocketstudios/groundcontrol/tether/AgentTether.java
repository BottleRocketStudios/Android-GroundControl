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

/**
 * Provide a means to abort execution of a task.
 */
public interface AgentTether {
    /**
     * Stop the Agent now, interrupting it if it is running and no other AgentListeners are registered for
     * identical Agents.
     */
    void cancel();

    /**
     * Unregister interest in the result of the Agent, but let it complete. This should be used in
     * most cases of short-running requests that wouldn't hurt to complete or in the event of
     * device rotation.
     */
    void release();

    /**
     * Return the agent identifier associated with this agent tether
     */
    String getAgentIdentifier();
}
