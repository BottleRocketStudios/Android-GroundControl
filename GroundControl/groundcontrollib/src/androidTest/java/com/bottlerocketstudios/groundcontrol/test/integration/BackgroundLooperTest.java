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

import android.os.Looper;
import android.os.Process;
import android.test.AndroidTestCase;
import android.util.Log;
import android.util.Pair;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.test.TestUtils;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.policy.StandardAgentPolicyBuilder;

public class BackgroundLooperTest extends AndroidTestCase {
    private static final String TAG = BackgroundLooperTest.class.getSimpleName();

    public void testBackgroundLooper() {
        OsThreadPriorityAgent looperTestAgentA = new OsThreadPriorityAgent("bgLooperA");
        OsThreadPriorityAgent looperTestAgentB = new OsThreadPriorityAgent("bgLooperB");

        AgentPolicy backgroundPolicy = (new StandardAgentPolicyBuilder()).setCallbackLooperId(AgentExecutor.getDefault().getBackgroundLooperId()).build();

        final TestUtils.Container<Pair<Integer,Boolean>> testResultA = new TestUtils.Container<>(null);
        final TestUtils.Container<Pair<Integer,Boolean>> testResultB = new TestUtils.Container<>(null);

        AgentExecutor.getDefault().runAgent(looperTestAgentA, backgroundPolicy, new AgentListener<Integer, Void>() {
            @Override
            public void onCompletion(String agentIdentifier, Integer result) {
                testResultA.setValue(new Pair<>(Process.myTid(), Process.getThreadPriority(Process.myTid()) == Process.THREAD_PRIORITY_BACKGROUND));
                Log.d(TAG, "Looper A: " + Looper.myLooper().toString());
            }

            @Override
            public void onProgress(String agentIdentifier, Void progress) {
            }
        });

        AgentExecutor.getDefault().runAgent(looperTestAgentB, backgroundPolicy, new AgentListener<Integer, Void>() {
            @Override
            public void onCompletion(String agentIdentifier, Integer result) {
                testResultB.setValue(new Pair<>(Process.myTid(), Process.getThreadPriority(Process.myTid()) == Process.THREAD_PRIORITY_BACKGROUND));
                Log.d(TAG, "Looper B: " + Looper.myLooper().toString());
            }

            @Override
            public void onProgress(String agentIdentifier, Void progress) {
            }
        });

        for(int i = 0; i < 60; i++) {
            TestUtils.safeSleep(10);
        }

        assertTrue("Thread priority was incorrect", testResultA.getValue().second);
        assertEquals("Callback did not happen on same thread", testResultA.getValue().first, testResultB.getValue().first);
        assertEquals("Callbacks did not both have correct thread priority", testResultA.getValue().second, testResultB.getValue().second);
    }

}
