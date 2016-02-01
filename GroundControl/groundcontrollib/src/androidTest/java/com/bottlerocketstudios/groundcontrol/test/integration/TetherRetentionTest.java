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
import com.bottlerocketstudios.groundcontrol.tether.AgentTether;

public class TetherRetentionTest extends AndroidTestCase {

    private static final String TEST_ID_1 = "tetherRetentionTest";

    public void testAsAbsenteeTether() {
        doWeakReferenceTetherRetention(false);
    }

    public void testAsUiTether() {
        doWeakReferenceTetherRetention(true);
    }

    public void doWeakReferenceTetherRetention(boolean actLikeUiClient) {
        SynchronousTimeAgent synchronousAgent = new SynchronousTimeAgent(TEST_ID_1, 500);
        AgentPolicy agentPolicy = (new StandardAgentPolicyBuilder()).setMaxCacheAgeMs(10000).build();

        final TestUtils.Container<Long> firstResult = new TestUtils.Container<>(0L);

        AgentTether tether = AgentExecutor.getDefault().runAgent(synchronousAgent, agentPolicy, new AgentListener<Long, Float>() {
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

        if (actLikeUiClient) tether.release();
        tether = null;
        System.gc();
        TestUtils.safeSleep(actLikeUiClient ? 300 : 3000);

        //Ensure cache hit on second request matches first.
        final TestUtils.Container<Long> thirdResult = new TestUtils.Container<>(0L);
        AgentExecutor.getDefault().runAgent(synchronousAgent, agentPolicy, new AgentListener<Long, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, Long result) {
                thirdResult.setValue(result);
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        while (thirdResult.getValue() <= 0) {
            TestUtils.safeSleep(50);
        }

        if (actLikeUiClient) {
            assertEquals("Cached value should have been the same.", firstResult.getValue(), thirdResult.getValue());
        } else {
            assertFalse("Cached value should have been removed, but was not.", thirdResult.getValue().equals(firstResult.getValue()));
        }
    }

}
