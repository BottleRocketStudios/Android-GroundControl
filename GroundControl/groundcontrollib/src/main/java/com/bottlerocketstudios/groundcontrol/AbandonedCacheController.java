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

import android.os.SystemClock;

import com.bottlerocketstudios.groundcontrol.cache.AgentResultCache;
import com.bottlerocketstudios.groundcontrol.tether.AgentTether;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class will ensure that data in the AgentResultCache without an externally held strong
 * reference to an associated Tether, will be removed from the cache. This will help clean up
 * data cached for components that no longer need it.
 */
class AbandonedCacheController {
    private final Map<String, List<WeakReference<AgentTether>>> mAgentTetherMap;
    private final AgentResultCache mAgentResultCache;
    private final long mAbandonedCacheLifetimeMs;
    private final Map<String, Long> mAbandonedCacheDeadlineMap;

    public AbandonedCacheController(AgentResultCache agentResultCache, long abandonedCacheLifetimeMs) {
        mAgentTetherMap = Collections.synchronizedMap(new HashMap<String, List<WeakReference<AgentTether>>>());
        mAgentResultCache = agentResultCache;
        mAbandonedCacheDeadlineMap = Collections.synchronizedMap(new HashMap<String, Long>());
        mAbandonedCacheLifetimeMs = abandonedCacheLifetimeMs;
    }

    /**
     * Create a WeakReference to the Tether for the supplied agentIdentifier.
     */
    public void addWeakTether(String agentIdentifier, AgentTether agentTether) {
        mAbandonedCacheDeadlineMap.remove(agentIdentifier);

        synchronized (mAgentTetherMap) {
            List<WeakReference<AgentTether>> tetherList = mAgentTetherMap.get(agentIdentifier);
            if (tetherList == null) {
                tetherList = Collections.synchronizedList(new ArrayList<WeakReference<AgentTether>>());
                mAgentTetherMap.put(agentIdentifier, tetherList);
            }
            tetherList.add(new WeakReference<>(agentTether));
        }
    }

    /**
     * Remove the specified Tether and any other dead references for the specified agentIdentifier.
     */
    public void removeWeakTether(String agentIdentifier, AgentTether tether) {
        List<WeakReference<AgentTether>> tetherList = mAgentTetherMap.get(agentIdentifier);
        if (tetherList != null) {

            //This instance of the list is the intended target for synchronization
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (tetherList) {
                for (Iterator<WeakReference<AgentTether>> tetherWeakReferenceIterator = tetherList.iterator(); tetherWeakReferenceIterator.hasNext(); ) {
                    WeakReference<AgentTether> tetherWeakReference = tetherWeakReferenceIterator.next();
                    //Remove missing weak references while we are at it.
                    if (tetherWeakReference.get() == null || tetherWeakReference.get().equals(tether)) {
                        tetherWeakReferenceIterator.remove();
                    }
                }
            }
        }
    }

    private long getTime() {
        return SystemClock.uptimeMillis();
    }

    /**
     * Once all weak references to Tethers for a particular cache entry have been removed add the cache
     * entry to the list for destruction. Unless a new Tether is associated before this runs a second
     * time, the cache entry will be removed.
     */
    public void cleanupUntetheredCache() {
        long now = getTime();

        synchronized (mAbandonedCacheDeadlineMap) {
            for (Iterator<String> abandonedCacheDeadlineMapKeyIterator = mAbandonedCacheDeadlineMap.keySet().iterator(); abandonedCacheDeadlineMapKeyIterator.hasNext(); ) {
                //Check if we have passed deadline and remove item if so.
                String agentIdentifier = abandonedCacheDeadlineMapKeyIterator.next();
                if (now > mAbandonedCacheDeadlineMap.get(agentIdentifier)) {
                    mAgentResultCache.removeCache(agentIdentifier);
                    abandonedCacheDeadlineMapKeyIterator.remove();
                }
            }
        }


        long deadline = getTime() + mAbandonedCacheLifetimeMs;
        synchronized (mAgentTetherMap) {
            for (Iterator<String> agentTetherMapKeyIterator = mAgentTetherMap.keySet().iterator(); agentTetherMapKeyIterator.hasNext(); ) {
                String agentIdentifier = agentTetherMapKeyIterator.next();
                List<WeakReference<AgentTether>> tetherList = mAgentTetherMap.get(agentIdentifier);

                //This instance of the list is the intended target for synchronization
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (tetherList) {
                    for (Iterator<WeakReference<AgentTether>> tetherWeakReferenceIterator = tetherList.iterator(); tetherWeakReferenceIterator.hasNext(); ) {
                        WeakReference<AgentTether> tetherWeakReference = tetherWeakReferenceIterator.next();
                        if (tetherWeakReference.get() == null) {
                            tetherWeakReferenceIterator.remove();
                        }
                    }
                    if (tetherList.size() == 0) {
                        agentTetherMapKeyIterator.remove();
                        mAbandonedCacheDeadlineMap.put(agentIdentifier, deadline);
                    }
                }
            }
        }
    }
}
