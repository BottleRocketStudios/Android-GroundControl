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

package com.bottlerocketstudios.groundcontrol.test.integration;

import android.test.AndroidTestCase;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.test.TestUtils;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.policy.StandardAgentPolicyBuilder;

public class CacheClearingTest extends AndroidTestCase {

    private static final String TEST_ID_1 = "cacheClearingTest";

    public void testCacheClearing() {
        SynchronousTimeAgent synchronousAgent = new SynchronousTimeAgent(TEST_ID_1, 500);
        AgentPolicy agentPolicy = (new StandardAgentPolicyBuilder()).setMaxCacheAgeMs(10000).build();

        final TestUtils.Container<Long> firstResult = new TestUtils.Container<>(0L);

        AgentExecutor.getDefault().runAgent(synchronousAgent, agentPolicy, new AgentListener<Long, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, Long result) {
                firstResult.setValue(result);
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        while (firstResult.getValue() <= 0) {
            TestUtils.safeSleep(50);
        }

        TestUtils.safeSleep(3000);

        //Ensure cache hit on second request matches first.
        final TestUtils.Container<Long> secondResult = new TestUtils.Container<>(0L);
        AgentExecutor.getDefault().runAgent(synchronousAgent, agentPolicy, new AgentListener<Long, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, Long result) {
                secondResult.setValue(result);
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        while (secondResult.getValue() <= 0) {
            TestUtils.safeSleep(50);
        }

        assertEquals("Did not hit cache when it should have", firstResult.getValue(), secondResult.getValue());

        //Run again with a clear cache policy so that cache is cleared.
        AgentPolicy clearCachePolicy = (new StandardAgentPolicyBuilder()).buildUpon(agentPolicy).setClearCache(true).build();
        final TestUtils.Container<Long> thirdResult = new TestUtils.Container<>(0L);
        AgentExecutor.getDefault().runAgent(synchronousAgent, clearCachePolicy, new AgentListener<Long, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, Long result) {
                thirdResult.setValue(result);
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        //Run a subsequent request immediately with cache allowed. This should be blocked by the first request and not hit the cache.
        final TestUtils.Container<Long> fourthResult = new TestUtils.Container<>(0L);
        AgentExecutor.getDefault().runAgent(synchronousAgent, agentPolicy, new AgentListener<Long, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, Long result) {
                fourthResult.setValue(result);
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        //Small delay to ensure that the values would be different if queueing/cache were not working.
        TestUtils.safeSleep(50);

        //Run a subsequent request with slight delay and with cache allowed. This should be blocked by the first request and not hit the cache.
        final TestUtils.Container<Long> fifthResult = new TestUtils.Container<>(0L);
        AgentExecutor.getDefault().runAgent(synchronousAgent, agentPolicy, new AgentListener<Long, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, Long result) {
                fifthResult.setValue(result);
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        while (thirdResult.getValue() <= 0 || fourthResult.getValue() <= 0 || fifthResult.getValue() <= 0) {
            TestUtils.safeSleep(50);
        }

        assertFalse("Third result should be different from first result", thirdResult.getValue().equals(firstResult.getValue()));
        assertEquals("Third result and fourth result should be equal", thirdResult.getValue(), fourthResult.getValue());
        assertEquals("Third result and fifth result should be equal", thirdResult.getValue(), fifthResult.getValue());
    }

}
