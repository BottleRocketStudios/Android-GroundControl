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

package com.bottlerocketstudios.groundcontrol.cache;

import com.bottlerocketstudios.groundcontrol.request.AgentRequest;

/**
 * Runnable to check the cache for an item and return a the item or null.
 */
public class CacheCheckRunnable<ResultType, ProgressType> implements Runnable {

    private final AgentResultCache mAgentResultCache;
    private final AgentRequest<ResultType, ProgressType> mAgentRequest;
    private final CacheCheckRunnableListener<ResultType, ProgressType> mCacheCheckRunnableListener;

    public CacheCheckRunnable(AgentResultCache agentResultCache, AgentRequest<ResultType, ProgressType> request, CacheCheckRunnableListener<ResultType, ProgressType> cacheCheckRunnableListener) {
        mAgentResultCache = agentResultCache;
        mAgentRequest = request;
        mCacheCheckRunnableListener = cacheCheckRunnableListener;
    }

    @Override
    public void run() {
        ResultType result = mAgentResultCache.get(mAgentRequest.getAgentIdentifier(), mAgentRequest.getMaxCacheAgeMs());
        mCacheCheckRunnableListener.onCacheResult(mAgentRequest, result);
    }

}
