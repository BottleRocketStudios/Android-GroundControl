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

import com.bottlerocketstudios.groundcontrol.cache.AgentResultCache;
import com.bottlerocketstudios.groundcontrol.cache.StandardAgentResultCache;
import com.bottlerocketstudios.groundcontrol.executor.PriorityQueueingPoolExecutorService;
import com.bottlerocketstudios.groundcontrol.executor.StandardPriorityQueueingPoolExecutorService;
import com.bottlerocketstudios.groundcontrol.inactivity.InactivityCleanupRunnable;
import com.bottlerocketstudios.groundcontrol.inactivity.StandardInactivityCleanupRunnable;
import com.bottlerocketstudios.groundcontrol.looper.HandlerCache;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.policy.StandardAgentPolicyBuilder;
import com.bottlerocketstudios.groundcontrol.request.AgentRequestController;
import com.bottlerocketstudios.groundcontrol.request.StandardAgentRequestController;
import com.bottlerocketstudios.groundcontrol.tether.AgentTetherBuilder;
import com.bottlerocketstudios.groundcontrol.tether.StandardAgentTetherBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Builder for AgentExecutor. Required for creation of new instances of AgentExecutor.
 */
public class AgentExecutorBuilder {

    private static final long DEFAULT_INACTIVITY_IDLE_MS = TimeUnit.MINUTES.toMillis(2);
    private static final long DEFAULT_CLEANUP_INTERVAL_MS = 250;
    private static final long DEFAULT_ABANDONED_CACHE_TIMEOUT_MS = 500;

    private final String mId;
    private AgentResultCache mAgentResultCache;
    private AgentTetherBuilder mAgentTetherBuilder;
    private AgentPolicy mDefaultAgentPolicy;
    private PriorityQueueingPoolExecutorService mCacheExecutorService;
    private PriorityQueueingPoolExecutorService mAgentExecutorService;
    private AgentRequestController mAgentRequestController;
    private HandlerCache mHandlerCache;
    private InactivityCleanupRunnable mInactivityCleanupRunnable;
    private long mAbandonedCacheTimeoutMs;

    /**
     * Create a builder instance with the supplied ID. This ID must be globally unique for the
     * application, but does not need to be the same across future application instances.
     */
    public AgentExecutorBuilder(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    /**
     * Provide your own AgentResultCache or use the StandardAgentResultCache.Builder to create a special
     * instance of that cache.
     */
    public AgentExecutorBuilder setAgentResultCache(AgentResultCache agentResultCache) {
        mAgentResultCache = agentResultCache;
        return this;
    }

    public AgentResultCache getAgentResultCache() {
        return mAgentResultCache;
    }

    protected AgentResultCache createDefaultAgentResultCache() {
        return StandardAgentResultCache.builder().build();
    }

    /**
     * Provide your own AgentTetherBuilder if you want to build your own special type of Tethers.
     */
    public void setAgentTetherBuilder(AgentTetherBuilder agentTetherBuilder) {
        mAgentTetherBuilder = agentTetherBuilder;
    }

    public AgentTetherBuilder getAgentTetherBuilder() {
        return mAgentTetherBuilder;
    }

    protected AgentTetherBuilder createDefaultAgentTetherBuilder() {
        return new StandardAgentTetherBuilder();
    }

    public AgentPolicy getDefaultAgentPolicy() {
        return mDefaultAgentPolicy;
    }

    /**
     * Set the default AgentPolicy used for requests that do not specify their own.
     */
    public AgentExecutorBuilder setDefaultAgentPolicy(AgentPolicy defaultAgentPolicy) {
        mDefaultAgentPolicy = defaultAgentPolicy;
        return this;
    }

    protected AgentPolicy createDefaultAgentPolicy() {
        return (new StandardAgentPolicyBuilder()).build();
    }

    public PriorityQueueingPoolExecutorService getCacheExecutorService() {
        return mCacheExecutorService;
    }

    /**
     * Provide your own instance of a PriorityQueueingPoolExecutorService to be used for caching.
     */
    public AgentExecutorBuilder setCacheExecutorService(PriorityQueueingPoolExecutorService cacheExecutorService) {
        mCacheExecutorService = cacheExecutorService;
        return this;
    }

    protected PriorityQueueingPoolExecutorService createDefaultCacheExecutorService() {
        return StandardPriorityQueueingPoolExecutorService.builder().setLogTag("CacheExecutorService").build();
    }

    public PriorityQueueingPoolExecutorService getAgentExecutorService() {
        return mAgentExecutorService;
    }

    /**
     * Provide your own instance of a PriorityQueueingPoolExecutorService to be used for agent execution.
     */
    public AgentExecutorBuilder setAgentExecutorService(PriorityQueueingPoolExecutorService agentExecutorService) {
        mAgentExecutorService = agentExecutorService;
        return this;
    }

    protected PriorityQueueingPoolExecutorService createDefaultAgentExecutorService() {
        return StandardPriorityQueueingPoolExecutorService.builder().setLogTag("AgentExecutorService").build();
    }

    protected PriorityQueueingPoolExecutorService createDefaultListenerExecutorService() {
        return StandardPriorityQueueingPoolExecutorService.builder().setLogTag("ListenerExecutorService").build();
    }

    public AgentRequestController getAgentRequestController() {
        return mAgentRequestController;
    }

    /**
     * Provide your own instance of a AgentRequestController to be used for result delivery.
     */
    public AgentExecutorBuilder setAgentRequestController(AgentRequestController agentRequestController) {
        mAgentRequestController = agentRequestController;
        return this;
    }

    protected AgentRequestController createDefaultAgentRequestController() {
        if (getHandlerCache() == null) throw new IllegalStateException("Cannot createDefaultAgentRequestController before setting HandlerCache");
        return new StandardAgentRequestController(createDefaultListenerExecutorService(), getHandlerCache());
    }

    public HandlerCache getHandlerCache() {
        return mHandlerCache;
    }

    /**
     * Provide an alternate HandlerCache.
     */
    public AgentExecutorBuilder setHandlerCache(HandlerCache handlerCache) {
        mHandlerCache = handlerCache;
        return this;
    }

    protected HandlerCache createDefaultHandlerCache() {
        return new HandlerCache();
    }


    public InactivityCleanupRunnable getInactivityCleanupRunnable() {
        return mInactivityCleanupRunnable;
    }

    /**
     * Provide your own instance of an InactivityCleanupRunnable.
     */
    public AgentExecutorBuilder setInactivityCleanupRunnable(InactivityCleanupRunnable inactivityCleanupRunnable) {
        mInactivityCleanupRunnable = inactivityCleanupRunnable;
        return this;
    }

    protected InactivityCleanupRunnable createDefaultInactivityCleanupRunnable() {
        return new StandardInactivityCleanupRunnable(DEFAULT_INACTIVITY_IDLE_MS, DEFAULT_CLEANUP_INTERVAL_MS);
    }

    /**
     * Set the amount of time that a cache entry will be allowed to live when nobody retains a
     * tether to it.
     */
    public AgentExecutorBuilder setAbandonedCacheTimeoutMs(long abandonedCacheTimeoutMs) {
        mAbandonedCacheTimeoutMs = abandonedCacheTimeoutMs;
        return this;
    }

    public long getAbandonedCacheTimeoutMs() {
        return mAbandonedCacheTimeoutMs;
    }

    /**
     * Build and register a new instance of an AgentExecutor providing defaults for all unspecified values.
     */
    public AgentExecutor build() {
        if (getAgentResultCache() == null) {
            setAgentResultCache(createDefaultAgentResultCache());
        }

        if (getAgentTetherBuilder() == null) {
            setAgentTetherBuilder(createDefaultAgentTetherBuilder());
        }

        if (getDefaultAgentPolicy() == null) {
            setDefaultAgentPolicy(createDefaultAgentPolicy());
        }

        if (getCacheExecutorService() == null) {
            setCacheExecutorService(createDefaultCacheExecutorService());
        }

        if (getAgentExecutorService() == null) {
            setAgentExecutorService(createDefaultAgentExecutorService());
        }

        if (getHandlerCache() == null) {
            setHandlerCache(createDefaultHandlerCache());
        }

        if (getAgentRequestController() == null) {
            setAgentRequestController(createDefaultAgentRequestController());
        }

        if (getInactivityCleanupRunnable() == null) {
            setInactivityCleanupRunnable(createDefaultInactivityCleanupRunnable());
        }

        if (getAbandonedCacheTimeoutMs() <= 0) {
            setAbandonedCacheTimeoutMs(DEFAULT_ABANDONED_CACHE_TIMEOUT_MS);
        }

        AgentExecutor agentExecutor = new AgentExecutor(this);
        AgentExecutor.setInstance(agentExecutor.getId(), agentExecutor);

        getInactivityCleanupRunnable().setListener(agentExecutor);

        return agentExecutor;
    }

}
