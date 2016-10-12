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

public interface AgentPolicyBuilder {

    /**
     * <p>
     * Set the LooperId to use for callback operations. This should be one of two standard looperIds
     * {@link com.bottlerocketstudios.groundcontrol.looper.LooperController#UI_LOOPER_ID} or
     * {@link com.bottlerocketstudios.groundcontrol.AgentExecutor#getBackgroundLooperId()}.
     * Managing lifecycle on your own Looper is highly discouraged as it will only die when the OS cleans up the app.
     * The AgentExecutor's Looper is managed and corresponds to a lack of work for the AgentExecutor.
     * </p><p>
     * Using the {@link com.bottlerocketstudios.groundcontrol.AgentExecutor#getBackgroundLooperId()} is recommended when
     * the listener will perform database operations using the resulting data. This prevents multiple threads from
     * accessing the database simultaneously which may result in inconsistent data.
     * </p><p>
     * Messages posted to a Handler will be executed FIFO serially. If you require parallel background thread callbacks use
     * {@link com.bottlerocketstudios.groundcontrol.policy.AgentPolicyBuilder#setParallelBackgroundCallback(boolean)}
     * </p>
     * <strong>
     * You cannot both set a callback Looper ID and call setParallelBackgroundCallback on the same policy.
     * </strong>
     */
    AgentPolicyBuilder setCallbackLooperId(String callbackLooperId);

    String getCallbackLooperId();

    /**
     * <p>
     * Callback operations will occur in parallel within the limitations of the Thread pool on multiple
     * background threads.
     * </p><p>
     * If you require serial callbacks on a background thread use setCallbackLooperId with
     * {@link com.bottlerocketstudios.groundcontrol.AgentExecutor#getBackgroundLooperId()}.
     * </p>
     * <strong>
     * You cannot both set a callback Looper ID and call setParallelBackgroundCallback on the same policy.
     * </strong>
     */
    AgentPolicyBuilder setParallelBackgroundCallback(boolean parallelBackgroundCallback);

    boolean isParallelBackgroundCallback();

    /**
     * Timeout in milliseconds before the listener will be notified with a null completion. This timeout
     * will start running as soon as a Policy is submitted.
     */
    AgentPolicyBuilder setPolicyTimeoutMs(long policyTimeoutMs);

    long getPolicyTimeoutMs();

    /**
     * Maximum age in milliseconds for a cached response to be considered valid by this Policy.
     * <ul>
     *     <li>The highest number submitted will become the cached item's new maximum age. That
     *     does not affect cached item creation time, however it will increase its cached lifetime.</li>
     *     <li>Other Policies may specify a shorter time and cause a cache miss which will fire the Agent. The
     *     Policies with shorter valid ages will receive a cache hit.</li>
     *     <li>A CacheAge of 0 will not be stored unless another Policy specifies a higher value. </li>
     * </ul>
     *
     * <strong>SUPER IMPORTANT</strong>Cached items are only retained while a Tether is held for the
     * associated agentIdentifier. It is designed for use with UI components to keep results in
     * memory across destroy/create actions in the UI. If you need to have in-memory long-term caching,
     * implement those outside of this tool.
     *
     * @see com.bottlerocketstudios.groundcontrol.policy.AgentPolicyBuilder#setBypassCache(boolean)
     */
    AgentPolicyBuilder setMaxCacheAgeMs(long maxCacheAgeMs);

    long getMaxCacheAgeMs();

    /**
     * This parameter determines the execution priority of the Agent as well as cache and listener callbacks. However,
     * listeners which are fired on a Handler are serially executed without attention to priority. If a Policy is
     * submitted for an Agent with a higher priority, that priority becomes the new priority. Agents with IMMEDIATE
     * priority skip the queue and are executed immediately. Use IMMEDIATE priority with caution hoss.
     */
    AgentPolicyBuilder setJobPriority(JobPriority jobPriority);

    JobPriority getJobPriority();

    /**
     * Do not attempt to read from cache despite cache age.
     */
    AgentPolicyBuilder setBypassCache(boolean bypassCache);

    boolean shouldBypassCache();

    /**
     * Set the amount of time in milliseconds for a parallelBackgroundCallback operation to last before
     * being interrupted.
     */
    AgentPolicyBuilder setParallelCallbackTimeoutMs(long timeoutMs);

    long getParallelCallbackTimeoutMs();

    /**
     * Set whether the invocation of this agent should clear the cache so that future requests cannot
     * hit a cached value until this invocation completes. i.e. The old data is no longer valid.
     * <strong>Clearing cache includes bypassing cache.</strong>
     */
    AgentPolicyBuilder setClearCache(boolean clearCache);

    boolean shouldClearCache();

    /**
     * Clear this builder to build a new instance.
     */
    AgentPolicyBuilder clear();

    /**
     * Clear then start with the provided policy as baseline values for this build.
     */
    AgentPolicyBuilder buildUpon(AgentPolicy agentPolicy);

    /**
     * Set both cache lifetime to 0 and bypass cache to true.
     */
    AgentPolicyBuilder disableCache();

    /**
     * Perform validation, set defaults, and deliver built instance of AgentPolicy.
     */
    AgentPolicy build();

}
