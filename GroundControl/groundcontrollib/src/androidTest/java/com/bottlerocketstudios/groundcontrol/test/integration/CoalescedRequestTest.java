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

import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.executor.ThreadPoolExecutorWithExceptions;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.policy.StandardAgentPolicyBuilder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Fire off tons of requests from multiple threads for identical operations all bypassing cache and
 * verify that execution only happens once per agentIdentifier.
 */
public class CoalescedRequestTest extends AndroidTestCase {

    private static final String TAG = CoalescedRequestTest.class.getSimpleName();

    private static final long EXECUTION_TIME_MS = 1000L;
    private static final int SIMULTANEOUS_REQUESTS = 10;
    private static final int TEST_ITERATIONS = 5;

    private SynchronousTimeAgent mTimeAgent;
    private AgentPolicy mBypassCacheBackgroundPolicy;

    public void testCoalescedRequests() {
        mTimeAgent = new SynchronousTimeAgent(CoalescedRequestTest.class.getCanonicalName(), EXECUTION_TIME_MS);
        mBypassCacheBackgroundPolicy = (new StandardAgentPolicyBuilder())
                .setBypassCache(true)
                .setMaxCacheAgeMs(0)
                .setParallelBackgroundCallback(true)
                .build();

        long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            Log.d(TAG, "Starting iteration " + i);
            ExecutorService executorService = ThreadPoolExecutorWithExceptions.newCachedThreadPool();
            ConcurrentAgentTask concurrentAgentTask1 = new ConcurrentAgentTask("1-" + String.valueOf(i), mTimeAgent, mBypassCacheBackgroundPolicy, SIMULTANEOUS_REQUESTS);
            ConcurrentAgentTask concurrentAgentTask2 = new ConcurrentAgentTask("2-" + String.valueOf(i), mTimeAgent, mBypassCacheBackgroundPolicy, SIMULTANEOUS_REQUESTS);
            ConcurrentAgentTask concurrentAgentTask3 = new ConcurrentAgentTask("3-" + String.valueOf(i), mTimeAgent, mBypassCacheBackgroundPolicy, SIMULTANEOUS_REQUESTS);

            Future future1 = executorService.submit(concurrentAgentTask1);
            Future future2 = executorService.submit(concurrentAgentTask2);
            Future future3 = executorService.submit(concurrentAgentTask3);

            try {
                future1.get();
                future2.get();
                future3.get();
            } catch (InterruptedException e) {
                Log.e(TAG, "Caught java.lang.InterruptedException", e);
            } catch (ExecutionException e) {
                Log.e(TAG, "Caught java.util.concurrent.ExecutionException", e);
            }
        }
        Log.d(TAG, "Test took " + String.valueOf(SystemClock.uptimeMillis() - startTime) + "ms");
    }

    private static class ConcurrentAgentTask extends Thread {
        private final SynchronousTimeAgent mAgent;
        private final AgentPolicy mAgentPolicy;
        private final String mName;
        private final int mRequestsToIssue;
        private CountDownLatch mCountdownLatch;
        private Long mLastResult;

        public ConcurrentAgentTask (String name, SynchronousTimeAgent agent, AgentPolicy agentPolicy, int requestsToIssue) {
            mName = name;
            mAgent = agent;
            mAgentPolicy = agentPolicy;
            mLastResult = 0L;
            mRequestsToIssue = requestsToIssue;
        }

        @Override
        public void run() {
            super.run();
            mCountdownLatch = new CountDownLatch(mRequestsToIssue);

            for (int i = 0; i < mRequestsToIssue; i++) {
                AgentExecutor.getDefault().runAgent(mAgent, mAgentPolicy, mAgentListener);
            }
            try {
                long startTime = SystemClock.uptimeMillis();
                boolean timeExceeded = false;
                while (!mCountdownLatch.await(250, TimeUnit.MILLISECONDS) && !timeExceeded) {
                    timeExceeded = ((SystemClock.uptimeMillis() - startTime) > (EXECUTION_TIME_MS + 5000));
                }

                assertFalse(mName + " did not complete in time " + String.valueOf(SystemClock.uptimeMillis() - startTime) + "ms", timeExceeded);
            } catch (InterruptedException e) {
                Log.e(TAG, mName + " caught java.lang.InterruptedException", e);
                assertTrue(mName + " interrupted before completion", false);
            }
        }

        private AgentListener<Long, Float> mAgentListener = new AgentListener<Long, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, Long result) {
                mCountdownLatch.countDown();
                //Log.d(TAG, mName + " countdown " + mCountdownLatch.getCount());
                if (mLastResult == 0) {
                    mLastResult = result;
                } else {
                    assertEquals(mName + " different result received for an agent that should have happened only once.", mLastResult, result);
                }
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        };
    }

}
