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

import android.util.Log;

import com.bottlerocketstudios.groundcontrol.inactivity.InactivityCleanupListener;
import com.bottlerocketstudios.groundcontrol.inactivity.InactivityCleanupRunnable;
import com.bottlerocketstudios.groundcontrol.inactivity.StandardInactivityCleanupRunnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Standard implementation of cache for Agent delivered values. Will clean the cache on a defined
 * interval and shutdown threads after a timeout period.
 */
public class StandardAgentResultCache implements AgentResultCache, InactivityCleanupListener {
    private static final String TAG = StandardAgentResultCache.class.getSimpleName();

    private static final long DEFAULT_INTERVAL_MS = 250;
    private static final long DEFAULT_IDLE_MS = TimeUnit.SECONDS.toMillis(30);

    private final InactivityCleanupRunnable mInactivityCleanupRunnable;
    private final Map<String, CachedAgentResult> mCacheMap;

    private StandardAgentResultCache(Builder builder) {
        mCacheMap = Collections.synchronizedMap(new HashMap<String, CachedAgentResult>());
        mInactivityCleanupRunnable = builder.inactivityCleanupRunnable;
    }

    public <ResultType> void put(String agentIdentifier, ResultType value, long initialCacheLifetimeMs) {
        mInactivityCleanupRunnable.restartTimer();
        if (initialCacheLifetimeMs > 0) {
            CachedAgentResult<ResultType> cachedAgentResult = new CachedAgentResult<>(value, initialCacheLifetimeMs);
            mCacheMap.put(agentIdentifier, cachedAgentResult);
        }
    }

    @Override
    public <ResultType> ResultType get(String agentIdentifier, long requestCacheLifetimeMs) {
        mInactivityCleanupRunnable.restartTimer();
        ResultType result = null;
        try {
            //This isn't unchecked, we know that it should be the type we expect. We catch the exception anyway.
            @SuppressWarnings("unchecked")
            CachedAgentResult<ResultType> cachedAgentResult = mCacheMap.get(agentIdentifier);

            if (cachedAgentResult != null) {
                //Increase maximum lifetime if this lifetime is greater.
                cachedAgentResult.setMaximumLifetimeMs(requestCacheLifetimeMs);

                //Use client specified lifetime to determine if this is a cache hit.
                if (!cachedAgentResult.isExpiredForSpecifiedLifetime(requestCacheLifetimeMs)) {
                    result = cachedAgentResult.getValue();
                }
            }
        } catch (ClassCastException e) {
            Log.e(TAG, "Cached object was an unexpected type", e);
        }
        return result;
    }

    public void cleanStaleCache() {
        synchronized (mCacheMap) {
            for (Iterator<String> cacheMapKeyIterator = mCacheMap.keySet().iterator(); cacheMapKeyIterator.hasNext(); ) {
                String agentIdentifier = cacheMapKeyIterator.next();
                CachedAgentResult cachedAgentResult = mCacheMap.get(agentIdentifier);
                if (cachedAgentResult != null && cachedAgentResult.isExpiredForMaxLifetime()) {
                    cacheMapKeyIterator.remove();
                }
            }
        }
    }

    @Override
    public void removeCache(String agentIdentifier) {
        mCacheMap.remove(agentIdentifier);
    }

    @Override
    public boolean isBusy() {
        return !mCacheMap.isEmpty();
    }

    @Override
    public void enterIdleState() {
        //Nothing to do here.
    }

    @Override
    public void performCleanup() {
        cleanStaleCache();
    }

    /**
     * Create a new builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder implementation required to compose a StandardAgentResultCache.
     */
    public static class Builder {

        private InactivityCleanupRunnable inactivityCleanupRunnable;

        public Builder setInactivityCleanupRunnable(InactivityCleanupRunnable inactivityCleanupRunnable) {
            this.inactivityCleanupRunnable = inactivityCleanupRunnable;
            return this;
        }

        public StandardAgentResultCache build() {

            if (inactivityCleanupRunnable == null) {
                setInactivityCleanupRunnable(new StandardInactivityCleanupRunnable(DEFAULT_IDLE_MS, DEFAULT_INTERVAL_MS));
            }

            StandardAgentResultCache standardAgentResultCache = new StandardAgentResultCache(this);
            inactivityCleanupRunnable.setListener(standardAgentResultCache);

            return new StandardAgentResultCache(this);
        }
    }
}
