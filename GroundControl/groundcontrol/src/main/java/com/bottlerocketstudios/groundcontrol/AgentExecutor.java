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

import android.text.TextUtils;
import android.util.Log;

import com.bottlerocketstudios.groundcontrol.agent.Agent;
import com.bottlerocketstudios.groundcontrol.cache.AgentResultCache;
import com.bottlerocketstudios.groundcontrol.cache.CacheCheckRunnable;
import com.bottlerocketstudios.groundcontrol.cache.CacheCheckRunnableListener;
import com.bottlerocketstudios.groundcontrol.executor.Job;
import com.bottlerocketstudios.groundcontrol.executor.PriorityQueueingPoolExecutorService;
import com.bottlerocketstudios.groundcontrol.inactivity.InactivityCleanupListener;
import com.bottlerocketstudios.groundcontrol.inactivity.InactivityCleanupRunnable;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.looper.HandlerCache;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.request.AgentRequest;
import com.bottlerocketstudios.groundcontrol.request.AgentRequestController;
import com.bottlerocketstudios.groundcontrol.tether.AgentTether;
import com.bottlerocketstudios.groundcontrol.tether.AgentTetherBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinates execution, caching, and coalescing of Agents.
 */
public class AgentExecutor implements InactivityCleanupListener {

    private static final String TAG = AgentExecutor.class.getSimpleName();

    public static final String DEFAULT_AGENT_EXECUTOR_ID = "<def>";

    private static final ConcurrentHashMap<String, AgentExecutor> sAgentExecutorMap = new ConcurrentHashMap<>();

    private final String mId;
    private final AgentResultCache mAgentResultCache;
    private final AgentTetherBuilder mAgentTetherBuilder;
    private final AgentPolicy mDefaultAgentPolicy;
    private final PriorityQueueingPoolExecutorService mCacheExecutorService;
    private final PriorityQueueingPoolExecutorService mAgentExecutorService;
    private final AgentRequestController mAgentRequestController;
    private final Map<String, StartedAgent> mStartedAgentMap;
    private final InactivityCleanupRunnable mInactivityCleanupRunnable;
    private final AbandonedCacheController mAbandonedCacheController;
    private final HandlerCache mHandlerCache;
    private final String mBackgroundLooperId;

    //Object is used for a synchronize lock to prevent other Threads from scheduling the same agent twice.
    @SuppressWarnings("FieldCanBeLocal")
    private final String mExecutionLock = "executionLock";

    /**
     * Obtain the default instance of the AgentExecutor. This is typically all you need.
     */
    public static AgentExecutor getDefault() {
        AgentExecutor agentExecutor = sAgentExecutorMap.get(DEFAULT_AGENT_EXECUTOR_ID);
        if (agentExecutor == null) {
            agentExecutor = createDefaultInstance();
        }
        return agentExecutor;
    }

    /**
     * Guarantee that only one instance of the AgentExecutor is built.
     */
    private static AgentExecutor createDefaultInstance() {
        synchronized (sAgentExecutorMap) {
            AgentExecutor agentExecutor = sAgentExecutorMap.get(DEFAULT_AGENT_EXECUTOR_ID);
            if (agentExecutor == null) {
                agentExecutor = builder(DEFAULT_AGENT_EXECUTOR_ID).build();
                Log.d(TAG, "Built Default " + String.valueOf(agentExecutor));
                sAgentExecutorMap.put(DEFAULT_AGENT_EXECUTOR_ID, agentExecutor);
            }
            return agentExecutor;
        }
    }

    /**
     * Obtain a different instance of the AgentExecutor created with the Builder.
     *
     * <strong>NOTE: This will have a separate instance of cache and a separate thread pool. Use multiple AgentExecutors with caution.</strong>
     *
     * @param identifier Globally unique identifier for the AgentExecutor
     */
    public static AgentExecutor getInstance(String identifier) {
        if (DEFAULT_AGENT_EXECUTOR_ID.equals(identifier)) {
            return getDefault();
        }
        return sAgentExecutorMap.get(identifier);
    }

    /**
     * Store a reference to a new AgentExecutor in the static map.
     */
    protected static void setInstance(String identifier, AgentExecutor agentExecutor) {
        sAgentExecutorMap.put(identifier, agentExecutor);
    }

    /**
     * The only constructor requires a builder instance to be created.
     */
    protected AgentExecutor(AgentExecutorBuilder builder) {
        mId = builder.getId();
        mAgentResultCache = builder.getAgentResultCache();
        mAgentTetherBuilder = builder.getAgentTetherBuilder();
        mDefaultAgentPolicy = builder.getDefaultAgentPolicy();
        mCacheExecutorService = builder.getCacheExecutorService();
        mAgentExecutorService = builder.getAgentExecutorService();
        mAgentRequestController = builder.getAgentRequestController();
        mInactivityCleanupRunnable = builder.getInactivityCleanupRunnable();
        mHandlerCache = builder.getHandlerCache();

        mStartedAgentMap = Collections.synchronizedMap(new HashMap<String, StartedAgent>());
        mAbandonedCacheController = new AbandonedCacheController(mAgentResultCache, builder.getAbandonedCacheTimeoutMs());
        mBackgroundLooperId = UUID.randomUUID().toString();
    }

    /**
     * Create a builder instance with the supplied ID. This ID must be globally unique for the
     * application, but does not need to be the same across future application instances.
     */
    public static AgentExecutorBuilder builder(String agentExecutorId) {
        return new AgentExecutorBuilder(agentExecutorId);
    }

    /**
     * Globally unique identifier for this AgentExecutor.
     */
    public String getId() {
        return mId;
    }

    /**
     * Globally unique background looper associated with background delivery via Handler. Use this
     * in your AgentPolicy if you desire notification on a background thread serially.
     */
    public String getBackgroundLooperId() {
        return mBackgroundLooperId;
    }

    /**
     * Enqueue a new Agent for execution with the default policy
     */
    public <ResultType, ProgressType> AgentTether runAgent(Agent<ResultType, ProgressType> agent, AgentListener<ResultType, ProgressType> agentListener) {
        return runAgent(agent, mDefaultAgentPolicy, agentListener);
    }

    /**
     * Enqueue a new Agent for execution.
     */
    public <ResultType, ProgressType> AgentTether runAgent(Agent<ResultType, ProgressType> agent, AgentPolicy agentPolicy, AgentListener<ResultType, ProgressType> agentListener) {
        mInactivityCleanupRunnable.restartTimer();

        String agentIdentifier = agent.getUniqueIdentifier();
        if (TextUtils.isEmpty(agentIdentifier)) {
            throw new IllegalArgumentException("Agent " + agent.getClass().getCanonicalName() + " returned '" + String.valueOf(agentIdentifier) + "' from getUniqueIdentifier(). The identifier must be a non-empty String.");
        }

        AgentTether agentTether = createAgentTether(agentListener, agentIdentifier);

        AgentRequest<ResultType, ProgressType> agentRequest = new AgentRequest<>(agent, agentListener, agentPolicy);

        if (agentRequest.shouldClearCache()) {
            mAgentResultCache.removeCache(agentIdentifier);
        }

        if (agentRequest.shouldBypassCache() || agentRequest.shouldClearCache()) {
            startAgentRequest(agentRequest);
        } else {
            startCacheRequest(agentRequest, false);
        }

        return agentTether;
    }

    private <ResultType, ProgressType> AgentTether createAgentTether(AgentListener<ResultType, ProgressType> agentListener, String agentIdentifier) {
        AgentTether agentTether;
        synchronized (mAgentTetherBuilder) {
            agentTether = mAgentTetherBuilder
                    .clear()
                    .setAgentExecutor(this)
                    .setAgentIdentifier(agentIdentifier)
                    .setAgentListener(agentListener)
                    .build();
            mAgentTetherBuilder.clear();
        }
        mAbandonedCacheController.addWeakTether(agentIdentifier, agentTether);
        return agentTether;
    }

    /**
     * Attach for delivery of an already running agent or hit cache only, do not re-run the agent with the default policy.
     */
    public <ResultType, ProgressType> AgentTether reattachToOneTimeAgent(String agentIdentifier, AgentListener<ResultType, ProgressType> agentListener) {
        return reattachToOneTimeAgent(agentIdentifier, mDefaultAgentPolicy, agentListener);
    }

    /**
     * Attach for delivery of an already running agent or hit cache only, do not re-run the agent.
     */
    public <ResultType, ProgressType> AgentTether reattachToOneTimeAgent(String agentIdentifier, AgentPolicy agentPolicy, AgentListener<ResultType, ProgressType> agentListener) {
        AgentTether agentTether = createAgentTether(agentListener, agentIdentifier);

        HollowAgent<ResultType, ProgressType> hollowAgent = new HollowAgent<>(agentIdentifier);

        AgentRequest<ResultType, ProgressType> agentRequest = new AgentRequest<>(hollowAgent, agentListener, agentPolicy);

        StartedAgent startedAgent = getStartedAgent(agentIdentifier);
        if (startedAgent != null) {
            mAgentRequestController.addAgentRequest(agentRequest);
            updatePendingAgentExecution(startedAgent, agentRequest);
        } else {
            if (agentPolicy.shouldBypassCache()) {
                Log.i(TAG, "Policy bypassCache directive ignored when reattaching to one-time agents.");
            }
            startCacheRequest(agentRequest, true);
        }

        return agentTether;
    }

    /**
     * Begin an asynchronous cache check. This will be off of the UI thread, but cache implementations should be very few clock cycles.
     * It is primarily taken off of the UI thread to ensure that delivery is asynchronous.
     */
    private <ResultType, ProgressType> void startCacheRequest(AgentRequest<ResultType, ProgressType> agentRequest, final boolean failOnCacheMiss) {
        CacheCheckRunnable<ResultType, ProgressType> cacheCheckRunnable = new CacheCheckRunnable<>(
                mAgentResultCache,
                agentRequest,
                new CacheCheckRunnableListener<ResultType, ProgressType>() {
                    @Override
                    public void onCacheResult(AgentRequest<ResultType, ProgressType> agentRequest, ResultType result) {
                        try {
                            if (result != null) {
                                mAgentRequestController.deliverCompletion(agentRequest, result);
                            } else if (failOnCacheMiss) {
                                mAgentRequestController.deliverCompletion(agentRequest, null);
                            } else {
                                startAgentRequest(agentRequest);
                            }
                        } catch (ClassCastException e) {
                            Log.e(TAG, "Cache result was of an unexpected type");
                        }
                    }
                });

        Job cacheCheckJob = new Job(mCacheExecutorService.getNextJobId(), cacheCheckRunnable, agentRequest.getPolicyTimeoutMs(), agentRequest.getJobPriority());
        mCacheExecutorService.enqueue(cacheCheckJob);
    }

    /**
     * Begin an agent request or add a redundant request to the list of waiting requests.
     */
    private <ResultType, ProgressType> void startAgentRequest(AgentRequest<ResultType, ProgressType> agentRequest) {
        synchronized (mExecutionLock) {
            mAgentRequestController.addAgentRequest(agentRequest);

            StartedAgent startedAgent = getStartedAgent(agentRequest.getAgentIdentifier());
            if (startedAgent == null) {
                addNewPendingAgentExecution(agentRequest);
            } else {
                updatePendingAgentExecution(startedAgent, agentRequest);
            }
        }
    }

    /**
     * Request is not already in progress. Enqueue it.
     */
    private <ResultType, ProgressType> void addNewPendingAgentExecution(AgentRequest<ResultType, ProgressType> agentRequest) {
        //Store the StartedAgent data.
        Agent<ResultType, ProgressType> agent = agentRequest.getAgent();
        Job agentJob = new Job(mAgentExecutorService.getNextJobId(), agent, agent.getRunTimeoutMs(), agentRequest.getJobPriority());
        StartedAgent startedAgent = StartedAgent.newStartedAgent(agentRequest, agent, agentJob);
        agentJob.setJobExecutionListener(startedAgent);
        addStartedAgent(agentRequest.getAgentIdentifier(), startedAgent);

        //Set the agent executor to this instance.
        agent.setAgentExecutor(this);

        //Set the listener to an anonymous instance.
        agent.setAgentListener(new AgentListener<ResultType, ProgressType>() {
            @Override
            public void onCompletion(String agentIdentifier, ResultType result) {
                StartedAgent startedAgent = removeStartedAgent(agentIdentifier);
                mAgentRequestController.notifyAgentCompletion(agentIdentifier, result);
                //The startedAgent may be null if an agent is cancelled then expunged by exceeding time limits and sends completion later anyway.
                if (startedAgent != null) {
                    mAgentResultCache.put(agentIdentifier, result, startedAgent.getInitialCacheAgeMs());
                }
            }

            @Override
            public void onProgress(String agentIdentifier, ProgressType progress) {
                mAgentRequestController.notifyAgentProgress(agentIdentifier, progress);
            }
        });

        mAgentExecutorService.enqueue(agentJob);
    }

    /**
     * Request is already in progress. Update any escalations, add this request to the delivery list and request a progress update.
     */
    private <ResultType, ProgressType> void updatePendingAgentExecution(StartedAgent startedAgent, AgentRequest<ResultType, ProgressType> agentRequest) {
        //Check job priority against queued/running job to promote if necessary.
        Job agentJob = startedAgent.getJob();
        if (agentJob != null && agentJob.getPriority().compareTo(agentRequest.getJobPriority()) < 0) {
            mAgentExecutorService.updateJobPriority(agentJob.getId(), agentRequest.getJobPriority());
        }

        //Adjust max cache age for coalesced requests before completion.
        startedAgent.setInitialCacheAgeMs(Math.max(startedAgent.getInitialCacheAgeMs(), agentRequest.getMaxCacheAgeMs()));

        //Request progress update
        startedAgent.requestProgressUpdate();
    }

    /**
     * Release this tether then cancel a running agent associated with the tether if no more listeners exist.
     */
    public void tetherCancel(AgentTether tether, String agentIdentifier, AgentListener agentListener) {
        tetherRelease(tether, agentIdentifier, agentListener);

        //There aren't any other AgentRequests associated with this agentIdentifier, cancel the associated agent.
        if (!mAgentRequestController.hasActiveRequests(agentIdentifier)) {
            cancelAgent(agentIdentifier);
        }
    }

    /**
     * Release this tether and allow the agent to continue execution.
     */
    public void tetherRelease(AgentTether tether, String agentIdentifier, AgentListener agentListener) {
        //Remove this listener
        mAgentRequestController.removeRequestForAgent(agentIdentifier, agentListener);
        mAbandonedCacheController.removeWeakTether(agentIdentifier, tether);
    }

    /**
     * Notify an agent that it should cancel.
     */
    private void cancelAgent(String agentIdentifier) {
        StartedAgent startedAgent = getStartedAgent(agentIdentifier);
        if (startedAgent != null) {
            startedAgent.cancel();
        }
    }

    @Override
    public boolean isBusy() {
        return mStartedAgentMap.size() > 0;
    }

    @Override
    public void enterIdleState() {
        mHandlerCache.stopHandler(getBackgroundLooperId());
    }

    @Override
    public void performCleanup() {
        mAgentRequestController.notifyPastDeadline();
        cancelExpiredAgents();
        cleanCache();
    }

    /**
     * Clean up any untethered cache entries.
     */
    private void cleanCache() {
        mAbandonedCacheController.cleanupUntetheredCache();
    }

    /**
     * Clean up and cancel agents that are not running or tell overdue agents to cancel.
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void cancelExpiredAgents() {
        synchronized (mStartedAgentMap) {
            /*
             * Take a snapshot of the key Set. If the Agent notifies of failure immediately upon
             * cancellation, Agent will be removed from mStartedAgentMap in a re-entrant way on the
             * current thread which modifies the key Set without being blocked by synchronization.
             *
             * We perform a null check on the value referenced by the key to avoid a potential
             * mismatch between the snapshot key set and the mStartedAgentMap.
             */
            Set<String> snapshotKeySet = new HashSet<>(mStartedAgentMap.keySet());
            for (Iterator<String> startedAgentMapKeyIterator = snapshotKeySet.iterator(); startedAgentMapKeyIterator.hasNext(); ) {
                String agentIdentifier = startedAgentMapKeyIterator.next();
                StartedAgent startedAgent = getStartedAgent(agentIdentifier);
                if (startedAgent != null) {
                    if (startedAgent.isPastMaximumDeadline()) {
                        Log.w(TAG, "Giving up on overdue agent " + startedAgent);
                        removeStartedAgent(agentIdentifier);
                    } else if (!startedAgent.isCancelled() && startedAgent.isPastCancellationDeadline()) {
                        Log.w(TAG, "Cancelling overdue agent " + startedAgent);
                        cancelAgent(agentIdentifier);
                    }
                }
            }
        }
    }

    private StartedAgent removeStartedAgent(String agentIdentifier) {
        return mStartedAgentMap.remove(agentIdentifier);
    }

    private void addStartedAgent(String agentIdentifier, StartedAgent startedAgent) {
        synchronized (mStartedAgentMap) {
            if (mStartedAgentMap.containsKey(agentIdentifier)) {
                throw new RuntimeException("More than one agent started for agentIdentifier: " + agentIdentifier);
            }
            mStartedAgentMap.put(agentIdentifier, startedAgent);
        }
    }

    private StartedAgent getStartedAgent(String agentIdentifier) {
        return mStartedAgentMap.get(agentIdentifier);
    }

    public boolean hasStartedAgent(String agentIdentifier) {
        return getStartedAgent(agentIdentifier) != null;
    }

}

