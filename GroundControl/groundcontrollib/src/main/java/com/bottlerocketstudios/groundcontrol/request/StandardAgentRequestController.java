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

import android.util.Log;

import com.bottlerocketstudios.groundcontrol.executor.Job;
import com.bottlerocketstudios.groundcontrol.executor.PriorityQueueingPoolExecutorService;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.listener.ListenerCompletionRunnable;
import com.bottlerocketstudios.groundcontrol.listener.ListenerProgressRunnable;
import com.bottlerocketstudios.groundcontrol.looper.HandlerCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Standard implementation of AgentRequestController that will serve as a conduit to notify AgentListeners.
 */
public class StandardAgentRequestController implements AgentRequestController {
    private static final String TAG = StandardAgentRequestController.class.getSimpleName();

    private final HandlerCache mHandlerCache;
    private final PriorityQueueingPoolExecutorService mListenerExecutorService;
    private final Map<String, List<AgentRequest>> mAgentRequestMap;

    public StandardAgentRequestController(PriorityQueueingPoolExecutorService listenerExecutorService, HandlerCache handlerCache) {
        mAgentRequestMap = Collections.synchronizedMap(new HashMap<String, List<AgentRequest>>());
        mListenerExecutorService = listenerExecutorService;
        mHandlerCache = handlerCache;
    }

    @Override
    public  <ResultType> void deliverCompletion(AgentRequest<ResultType, ?> agentRequest, ResultType result) {
        ListenerCompletionRunnable<ResultType> completionRunnable = new ListenerCompletionRunnable<>(
                agentRequest.getAgentIdentifier(),
                agentRequest.getAgentListener(),
                result);

        if (agentRequest.getCallbackLooperId() != null) {
            mHandlerCache.getHandler(agentRequest.getCallbackLooperId()).post(completionRunnable);
        } else {
            Job listenerCompletionJob = new Job(mListenerExecutorService.getNextJobId(), completionRunnable, agentRequest.getParallelCallbackTimeoutMs(), agentRequest.getJobPriority());
            mListenerExecutorService.enqueue(listenerCompletionJob);
        }
    }

    @Override
    public <ProgressType> void deliverProgress(AgentRequest<?, ProgressType> agentRequest, ProgressType progress) {
        ListenerProgressRunnable<ProgressType> completionRunnable = new ListenerProgressRunnable<>(
                agentRequest.getAgentIdentifier(),
                agentRequest.getAgentListener(),
                progress);

        if (agentRequest.getCallbackLooperId() != null) {
            mHandlerCache.getHandler(agentRequest.getCallbackLooperId()).post(completionRunnable);
        } else {
            Job listenerCompletionJob = new Job(mListenerExecutorService.getNextJobId(), completionRunnable, agentRequest.getParallelCallbackTimeoutMs(), agentRequest.getJobPriority());
            mListenerExecutorService.enqueue(listenerCompletionJob);
        }
    }

    @Override
    public void addAgentRequest(AgentRequest agentRequest) {
        synchronized (mAgentRequestMap) {
            List<AgentRequest> agentRequestList = mAgentRequestMap.get(agentRequest.getAgentIdentifier());
            if (agentRequestList == null) {
                agentRequestList = Collections.synchronizedList(new ArrayList<AgentRequest>());
                mAgentRequestMap.put(agentRequest.getAgentIdentifier(), agentRequestList);
            }
            agentRequestList.add(agentRequest);
        }
    }

    private void sortAgentRequestListByPriority(List<AgentRequest> agentRequestList) {
        Collections.sort(agentRequestList, new AgentRequestPriorityComparator());
    }

    @Override
    public <ResultType> void notifyAgentCompletion(String agentIdentifier, ResultType result) {
        List<AgentRequest> agentRequestList = mAgentRequestMap.remove(agentIdentifier);
        if (agentRequestList != null) {
            try {
                /*
                 * Deliver results in order of priority. If a Handler is being used, this matters
                 * as Handlers are FIFO and serial.
                 */
                sortAgentRequestListByPriority(agentRequestList);

                //We know that all AgentRequests for the specified agentIdentifier are of the expected type.
                //noinspection unchecked
                for (AgentRequest<ResultType, ?> agentRequest : agentRequestList) {
                    deliverCompletion(agentRequest, result);
                }
            } catch (ClassCastException e) {
                Log.e(TAG, "AgentRequest ResultType mismatch " + agentIdentifier, e);
            }
        }
    }

    @Override
    public <ProgressType> void notifyAgentProgress(String agentIdentifier, ProgressType progress) {
        List<AgentRequest> agentRequestList = mAgentRequestMap.get(agentIdentifier);
        if (agentRequestList != null) {
            try {
                /*
                 * Deliver progress in order of priority. If a Handler is being used, this matters
                 * as Handlers are FIFO and serial.
                 */
                List<AgentRequest> snapshot = new ArrayList<>(agentRequestList);
                sortAgentRequestListByPriority(snapshot);

                //We know that all AgentRequests for the specified agentIdentifier are of the expected type.
                //noinspection unchecked
                for (AgentRequest<?, ProgressType> agentRequest : snapshot) {
                    deliverProgress(agentRequest, progress);
                }
            } catch (ClassCastException e) {
                Log.e(TAG, "AgentRequest ProgressType mismatch " + agentIdentifier, e);
            }
        }
    }

    @Override
    public void removeRequestForAgent(String agentIdentifier, AgentListener agentListener) {
        List<AgentRequest> agentRequestList = mAgentRequestMap.get(agentIdentifier);
        if (agentRequestList != null) {
            //Find the associated request for this agentId, listener combo.
            for (Iterator<AgentRequest> agentRequestIterator = agentRequestList.iterator(); agentRequestIterator.hasNext();) {
                AgentRequest agentRequest = agentRequestIterator.next();
                if (agentRequest.getAgentListener().equals(agentListener)) {
                    agentRequestIterator.remove();
                }
            }

            //There are no more associated requests, remove the entire list.
            if (agentRequestList.size() == 0) {
                mAgentRequestMap.remove(agentIdentifier);
            }
        }
    }

    @Override
    public boolean hasActiveRequests(String agentIdentifier) {
        List<AgentRequest> agentRequestList = mAgentRequestMap.get(agentIdentifier);
        return (agentRequestList != null && agentRequestList.size() > 0);
    }

    @Override
    public void notifyPastDeadline() {
        synchronized (mAgentRequestMap) {
            for (String agentIdentifier : mAgentRequestMap.keySet()) {
                List<AgentRequest> agentRequestList = mAgentRequestMap.get(agentIdentifier);
                for (Iterator<AgentRequest> agentRequestIterator = agentRequestList.iterator(); agentRequestIterator.hasNext(); ) {
                    AgentRequest agentRequest = agentRequestIterator.next();
                    if (agentRequest.isPastDeadline()) {
                        //The type of the AgentRequest does not matter, we are delivering null.
                        //noinspection unchecked
                        deliverCompletion(agentRequest, null);
                        agentRequestIterator.remove();
                    }
                }
            }
        }
    }
}
