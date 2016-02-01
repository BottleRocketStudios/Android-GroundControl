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

package com.bottlerocketstudios.groundcontrol.test.cache;

import android.test.AndroidTestCase;

import com.bottlerocketstudios.groundcontrol.cache.AgentResultCache;
import com.bottlerocketstudios.groundcontrol.cache.StandardAgentResultCache;
import com.bottlerocketstudios.groundcontrol.test.TestUtils;
import com.bottlerocketstudios.groundcontrol.inactivity.StandardInactivityCleanupRunnable;

public class StandardAgentResultCacheTest extends AndroidTestCase {

    private final static String CACHE_ID_1 = "cacheId1";
    private final static String CACHE_ID_2 = "cacheId2";

    private final static String CACHED_VALUE_1 = "cachedValue1";
    private final static String CACHED_VALUE_2 = "cachedValue2";

    private final static long CACHE_TIMEOUT_1 = 1000;
    private final static long CACHE_TIMEOUT_2 = 2000;
    private final static long CACHE_GONE_TIMEOUT = 100;

    private AgentResultCache getTestingAgentResultCache() {
        return StandardAgentResultCache.builder()
                .setInactivityCleanupRunnable(new StandardInactivityCleanupRunnable(5000, CACHE_GONE_TIMEOUT / 2))
                .build();
    }

    public void testCacheExpiration() {
        AgentResultCache agentResultCache = getTestingAgentResultCache();

        agentResultCache.put(CACHE_ID_1, CACHED_VALUE_1, CACHE_TIMEOUT_1);
        TestUtils.safeSleep(CACHE_TIMEOUT_1 / 2);
        //Test basic storage after waiting half of timeout.
        assertEquals("Cached value was incorrect", CACHED_VALUE_1, agentResultCache.get(CACHE_ID_1, CACHE_TIMEOUT_1));

        //Test early expiration from requester.
        assertNull("Cached value was returned when it was too old", agentResultCache.get(CACHE_ID_1, CACHE_TIMEOUT_1 / 3));

        //Test that early expiration response did not actually remove cached value.
        assertEquals("Cached value was incorrect", CACHED_VALUE_1, agentResultCache.get(CACHE_ID_1, CACHE_TIMEOUT_1));

        //Test full expiration time by waiting second half of expiration time plus some fudge factor for sweep time.
        TestUtils.safeSleep(CACHE_TIMEOUT_1 / 2 + CACHE_GONE_TIMEOUT);
        assertNull("Cached value was returned when it was too old", agentResultCache.get(CACHE_ID_1, CACHE_TIMEOUT_1));
    }

    public void testCacheSeparation() {
        AgentResultCache agentResultCache = getTestingAgentResultCache();
        agentResultCache.put(CACHE_ID_1, CACHED_VALUE_1, CACHE_TIMEOUT_1);
        agentResultCache.put(CACHE_ID_2, CACHED_VALUE_2, CACHE_TIMEOUT_2);
        assertEquals("Cached value was incorrect", CACHED_VALUE_1, agentResultCache.get(CACHE_ID_1, CACHE_TIMEOUT_1));
        assertEquals("Cached value was incorrect", CACHED_VALUE_2, agentResultCache.get(CACHE_ID_2, CACHE_TIMEOUT_2));
    }

    public void testCacheTimeoutIncrease() {
        AgentResultCache agentResultCache = getTestingAgentResultCache();
        agentResultCache.put(CACHE_ID_1, CACHED_VALUE_1, CACHE_TIMEOUT_1);
        assertEquals("Cached value was incorrect", CACHED_VALUE_1, agentResultCache.get(CACHE_ID_1, CACHE_TIMEOUT_1));

        //Increase retention time beyond initial cache timeout
        assertTrue("Testing constants are not suitable", CACHE_TIMEOUT_1 + CACHE_GONE_TIMEOUT < CACHE_TIMEOUT_2);
        agentResultCache.get(CACHE_ID_1, CACHE_TIMEOUT_2);

        //Wait beyond initial timeout and try to retrieve with new, higher timeout.
        TestUtils.safeSleep(CACHE_TIMEOUT_1 + CACHE_GONE_TIMEOUT);
        assertEquals("Cached value was incorrect", CACHED_VALUE_1, agentResultCache.get(CACHE_ID_1, CACHE_TIMEOUT_2));
    }
}
