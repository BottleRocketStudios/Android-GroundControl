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

import com.bottlerocketstudios.groundcontrol.agent.Agent;
import com.bottlerocketstudios.groundcontrol.executor.JobPriority;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.tether.AgentTether;

/**
 * An object to help with policy enforcement and modification before execution of an Agent.
 *
 * You <strong>must</strong> call one of the policy methods before calling other methods or not at all.
 */
public interface ExecutionBuilder<ResultType, ProgressType> {
    /**
     * Set a specific AgentPolicy for this builder. You must set the policy before other policy-affecting
     * methods in the builder so it is recommended that you call this method first.
     * <p>
     *     If no policy is set, the default policy from the AgentPolicyBuilder this class was constructed with will be used.
     *     It is recommended that you use one of the convenience methods {@link ExecutionBuilder#uiPolicy()},
     *     {@link ExecutionBuilder#bgParallelPolicy()}, or {@link ExecutionBuilder#bgSerialPolicy()}.
     * </p>
     * <p>
     *     An appropriate default policy will also be selected when you supply your callback method via {@link ExecutionBuilder#uiCallback(AgentListener)},
     *     {@link ExecutionBuilder#bgParallelCallback(AgentListener)}, or {@link ExecutionBuilder#bgSerialCallback(AgentListener)}.
     * </p>
     * <p><strong>NOTE:</strong> You may only set policy once and must set it before other policy-modifying operations.</p>
     */
    ExecutionBuilder<ResultType, ProgressType> policy(AgentPolicy agentPolicy);

    /**
     * Select a baseline policy for this ExecutionBuilder from the AgentPolicyCache this ExecutionBuilder was
     * constructed with.
     *
     * @see ExecutionBuilder#policy(AgentPolicy)
     */
    ExecutionBuilder<ResultType, ProgressType> policy(String agentPolicyIdentifier);

    /**
     * Select the default UI Policy as a baseline policy for this ExecutionBuilder from the AgentPolicyCache.
     *
     * @see ExecutionBuilder#policy(AgentPolicy)
     */
    ExecutionBuilder<ResultType, ProgressType> uiPolicy();

    /**
     * Select the default Background Parallel Policy as a baseline policy for this ExecutionBuilder from the AgentPolicyCache.
     *
     * @see ExecutionBuilder#policy(AgentPolicy)
     */
    ExecutionBuilder<ResultType, ProgressType> bgParallelPolicy();

    /**
     * Select the default Background Serial Policy as a baseline policy for this ExecutionBuilder from the AgentPolicyCache.
     *
     * @see ExecutionBuilder#policy(AgentPolicy)
     */
    ExecutionBuilder<ResultType, ProgressType> bgSerialPolicy();

    /**
     * Provide a listener to be called back on the UI thread Looper. This will modify any existing policy
     * to set that callback looper. If no policy method has been called, it should call {@link ExecutionBuilder#uiPolicy()}.
     * If a policy has been set, it should ensure the policy will callback on the UI looper.
     *
     * <p><strong>Note:</strong> It is recommended to call this method before other methods if you do not explicitly set a policy.</p>
     */
    ExecutionBuilder<ResultType, ProgressType> uiCallback(AgentListener<ResultType, ProgressType> agentListener);

    /**
     * Provide a listener to be called back on a background thread in a thread pool concurrently with other
     * callback operations for this AgentExecutor. If no policy method has been called, it should call {@link ExecutionBuilder#bgParallelPolicy()}.
     * If a policy has been set, it should ensure the policy will callback in parallel on the background thread pool.
     *
     * <p><strong>Note:</strong> It is recommended to call this method before other methods if you do not explicitly set a policy.</p>
     */
    ExecutionBuilder<ResultType, ProgressType> bgParallelCallback(AgentListener<ResultType, ProgressType> agentListener);

    /**
     * Provide a listener to be called back on a background thread Looper serially after other callback
     * operations for this AgentExecutor. This is most useful for database operations or other operations
     * where modifying a shared resource concurrently could fail or be non-deterministic. If no
     * policy method has been called, it should call {@link ExecutionBuilder#bgSerialPolicy()}.
     * If a policy has been set, it should ensure the policy will callback in parallel on the background thread pool.
     *
     * <p><strong>Note:</strong> It is recommended to call this method before other methods if you do not explicitly set a policy.</p>
     */
    ExecutionBuilder<ResultType, ProgressType> bgSerialCallback(AgentListener<ResultType, ProgressType> agentListener);

    /**
     * Set the priority for this operation and its callbacks.
     *
     * @see com.bottlerocketstudios.groundcontrol.policy.AgentPolicyBuilder#setJobPriority(JobPriority)
     */
    ExecutionBuilder<ResultType, ProgressType> priority(JobPriority jobPriority);

    /**
     * Configure the AgentPolicy to bypass cache.
     *
     * <p><strong>You should not bypass cache when reattaching to one-time operations.</strong> {@link #oneTime(String)}</p>
     *
     * @see com.bottlerocketstudios.groundcontrol.policy.AgentPolicyBuilder#setBypassCache(boolean)
     */
    ExecutionBuilder<ResultType, ProgressType> bypassCache(boolean bypassCache);

    /**
     * Configure the AgentPolicy to clear the cache.
     *
     * @see com.bottlerocketstudios.groundcontrol.policy.AgentPolicyBuilder#setClearCache(boolean)
     */
    ExecutionBuilder<ResultType, ProgressType> clearCache(boolean clearCache);

    /**
     * Set the maximum age in milliseconds which a cache-hit is acceptable for this individual agent
     * execution.
     *
     * @see com.bottlerocketstudios.groundcontrol.policy.AgentPolicyBuilder#setMaxCacheAgeMs(long)
     */
    ExecutionBuilder<ResultType, ProgressType> cacheAgeMs(long cacheAgeMs);

    /**
     * Simultaneously call bypassCache(true) and cacheAgeMs(0)
     */
    ExecutionBuilder<ResultType, ProgressType> disableCache();

    /**
     * Set the maximum time in milliseconds before the associated listener is called back with a null
     * failure response if a real response has not been delivered.
     *
     * @see com.bottlerocketstudios.groundcontrol.policy.AgentPolicyBuilder#setPolicyTimeoutMs(long)
     */
    ExecutionBuilder<ResultType, ProgressType> timeout(long timeoutMs);

    /**
     * Set the maximum time expected for the execution of a parallel callback.
     *
     * @see ExecutionBuilder#bgParallelCallback(AgentListener)
     * @see com.bottlerocketstudios.groundcontrol.policy.AgentPolicyBuilder#setParallelCallbackTimeoutMs(long)
     */
    ExecutionBuilder<ResultType, ProgressType> parallelCallbackTimeout(long timeoutMs);

    /**
     * Set the one-time execution identifier for this operation. Calling this method has side-effects,
     * when building, it will update the policy such that it will not bypass cache, not clear cache and has
     * a non-zero cache timeout.
     *
     * @see GroundControl#onOneTimeCompletion(String)
     * @see GroundControl#reattachToOneTime(Object, String, AgentListener)
     */
    ExecutionBuilder<ResultType, ProgressType> oneTime(String oneTimeIdentifier);

    /**
     * Sets the associated UI object for this operation. If you used {@link GroundControl#uiAgent(Object, Agent)}
     * you do not need to call this method. You do however need to call {@link GroundControl#onDestroy(Object)} when
     * your UI object is destroyed.
     */
    ExecutionBuilder<ResultType, ProgressType> ui(Object object);

    /**
     * Call this method last, when you are ready to execute your agent with the supplied policy and
     * other parameters. It will return an AgentTether which you can use to release or cancel ongoing
     * operations
     */
    AgentTether execute();
}
