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

package com.bottlerocketstudios.groundcontrol.test.looper;

import android.os.Process;
import android.test.AndroidTestCase;
import android.util.Log;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.executor.JobPriority;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.looper.LooperController;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.policy.StandardAgentPolicyBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LooperControllerTest extends AndroidTestCase {
    private static final String TAG = LooperControllerTest.class.getSimpleName();

    private static final int TESTS_PER_LOOPER = 20;
    private static final int TEST_ITERATIONS = 5;
    private static final long MAX_LIFETIME_DURATION_MS = TimeUnit.SECONDS.toMillis(2);
    private static final long MAX_TOTAL_EXECUTION_MS = TimeUnit.SECONDS.toMillis(30);
    private static final long MAX_ITERATION_TIME_MS = TimeUnit.SECONDS.toMillis(5);

    List<Map<String, Integer>> mIterationLooperThreadList = Collections.synchronizedList(new ArrayList<Map<String, Integer>>());
    Map<String, CountDownLatch> mLooperIterationLatch = Collections.synchronizedMap(new HashMap<String, CountDownLatch>());
    Map<String, Integer> mStoppedLooperIterations = Collections.synchronizedMap(new HashMap<String, Integer>());

    private CountDownLatch mAllTestCountDownLatch;

    public void testPriorityBounds() {
        boolean exceptionThrown = false;
        try {
            LooperController.getLooper("TooHigh", Process.THREAD_PRIORITY_URGENT_AUDIO - 1);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue("Did not throw an exception with too high of a priority", exceptionThrown);

        exceptionThrown = false;
        try {
            LooperController.getLooper("TooLow", Process.THREAD_PRIORITY_LOWEST + 1);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue("Did not throw an exception with too low of a priority", exceptionThrown);
    }

    public void testThreadSafety() {
        String[] looperIds = {
                "LCTID1",
                "LCTID2",
                "LCTID3",
                "LCTID4",
        };

        mAllTestCountDownLatch = new CountDownLatch(looperIds.length * TESTS_PER_LOOPER * TEST_ITERATIONS);

        for (int i = 0; i < TEST_ITERATIONS; i++) {
            mIterationLooperThreadList.add(Collections.synchronizedMap(new HashMap<String, Integer>()));
        }

        //Start first iteration, remaining iterations will be started when the Stop agent callback occurs.
        for (String looperId : looperIds) {
            spawnTests(looperId, 0);
        }

        assertTrue("MAX_TOTAL_EXECUTION_MS exceeded", awaitLatch(mAllTestCountDownLatch, MAX_TOTAL_EXECUTION_MS));
    }

    /**
     * Only stop a looper once per iteration.
     */
    private boolean canStopIteration(String looperId, int testIteration) {
        Integer stoppedIteration = mStoppedLooperIterations.get(looperId);
        return stoppedIteration == null || stoppedIteration < testIteration;
    }

    private synchronized void stopLooper(String looperId, int testIteration) {
        if (canStopIteration(looperId, testIteration)) {
            mStoppedLooperIterations.put(looperId, testIteration);
            AgentPolicy agentPolicy = (new StandardAgentPolicyBuilder()).setJobPriority(JobPriority.IMMEDIATE).setParallelBackgroundCallback(true).build();
            AgentExecutor.getDefault().runAgent(new LooperTestAgent(TESTS_PER_LOOPER, testIteration, looperId, LooperTestAction.TEST_STOP), agentPolicy, mLooperTestAgentListener);
        }
    }

    private synchronized void spawnTests(String looperId, int testIteration) {
        mLooperIterationLatch.put(looperId, new CountDownLatch(TESTS_PER_LOOPER - 1));
        AgentPolicy agentPolicy = (new StandardAgentPolicyBuilder()).setParallelBackgroundCallback(true).build();
        for (int i = 0; i < TESTS_PER_LOOPER - 1; i++) {
            AgentExecutor.getDefault().runAgent(new LooperTestAgent(i, testIteration, looperId, LooperTestAction.TEST_EXECUTION), agentPolicy, mLooperTestAgentListener);
        }
    }

    private boolean awaitLatch(CountDownLatch latch, long timeoutMs) {
        try {
            return latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Caught java.lang.InterruptedException", e);
            assertTrue("Interrupted while waiting on latch", false);
        }
        return false;
    }

    private AgentListener<LooperTestResult, Void> mLooperTestAgentListener = new AgentListener<LooperTestResult, Void>() {

        @Override
        public void onCompletion(String agentIdentifier, LooperTestResult result) {
            assertTrue("Agent took too long to be executed", result.durationMs < MAX_LIFETIME_DURATION_MS);
            CountDownLatch looperIterationLatch = mLooperIterationLatch.get(result.looperId);

            switch (result.looperTestAction) {
                case TEST_EXECUTION:
                    verifySameThread(result);
                    verifyNewThread(result);
                    looperIterationLatch.countDown();
                    if (awaitLatch(looperIterationLatch, 10)) {
                        stopLooper(result.looperId, result.testIteration);
                    }
                    break;
                case TEST_STOP:
                    //Tests are over, wait for them all to complete then start next iteration.
                    assertTrue("Iteration for looper " + result.looperId + " took too long.",
                            awaitLatch(looperIterationLatch, MAX_ITERATION_TIME_MS));
                    if (result.testIteration < TEST_ITERATIONS - 1) {
                        spawnTests(result.looperId, result.testIteration + 1);
                    }
                    break;
            }

            mAllTestCountDownLatch.countDown();
        }

        private void verifyNewThread(LooperTestResult result) {
            for (int i = 0; i < result.testIteration; i++) {
                int threadId = getThreadId(i, result.looperId, 0);
                assertTrue("Execution happened on the same thread across iterations " + i + " and " + result.testIteration, threadId != result.threadId);
            }
        }

        private void verifySameThread(LooperTestResult result) {
            int threadId = getThreadId(result.testIteration, result.looperId, result.threadId);
            assertEquals("Execution of iteration " + result.testIteration + " looper " + result.looperId + " test " + result.testId + " happened on the wrong thread", threadId, result.threadId);
        }

        private synchronized int getThreadId(int testIteration, String looperId, int currentThreadId) {
            Map<String, Integer> looperThreadMap = getLooperThreadMap(testIteration);
            Integer threadId = looperThreadMap.get(looperId);
            if (threadId == null || threadId.equals(0)) {
                threadId = currentThreadId;
                looperThreadMap.put(looperId, threadId);
            }
            return threadId;
        }

        private Map<String, Integer> getLooperThreadMap(int testIteration) {
            return mIterationLooperThreadList.get(testIteration);
        }

        @Override
        public void onProgress(String agentIdentifier, Void progress) {}
    };

}
