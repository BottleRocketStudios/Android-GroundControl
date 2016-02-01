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
import com.bottlerocketstudios.groundcontrol.test.TestUtils;
import com.bottlerocketstudios.groundcontrol.executor.ThreadPoolExecutorWithExceptions;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.looper.LooperController;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.policy.StandardAgentPolicyBuilder;
import com.bottlerocketstudios.groundcontrol.tether.AgentTether;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Simulate random UI configuration changes at random intervals while an agent is running and redelivering results.
 *
 * 1. No request is unanswered for still existing UI once the work should be completed.
 * 2. Cache hits do not continue after policy specified cache lifetime.
 * 3. Cache hits do occur during policy specified cache lifetime.
 * 4. No concurrency related crashes.
 */
public class RandomUiTest extends AndroidTestCase {
    private static final String TAG = RandomUiTest.class.getSimpleName();

    private static final long EXECUTION_TIME_MS = 1000;
    private static final long MAX_CACHE_AGE_MS = 3000;
    private static final float PRE_EXECUTION_DITHER_PROBABILITY = 0.5f;
    private static final int PRE_EXECUTION_DITHER = 30;
    private static final int ROTATION_MIN_TIME_MS = 50;
    private static final int ROTATION_MAX_TIME_MS = 4000;
    private static final int TEST_ITERATIONS = 30;
    private static final int SIMULTANEOUS_TESTS = 3;

    public void testAsRandomUi() {

        ExecutorService executorService = ThreadPoolExecutorWithExceptions.newCachedThreadPool();
        AgentPolicy agentPolicy = (new StandardAgentPolicyBuilder())
                .setBypassCache(false)
                .setCallbackLooperId(LooperController.UI_LOOPER_ID)
                .setMaxCacheAgeMs(MAX_CACHE_AGE_MS)
                .build();

        SynchronousTimeAgent synchronousTimeAgent = new SynchronousTimeAgent(RandomUiTask.class.getCanonicalName(), EXECUTION_TIME_MS);

        Future futureList[] = new Future[SIMULTANEOUS_TESTS];

        for (int i = 0; i < TEST_ITERATIONS; i += SIMULTANEOUS_TESTS) {
            for (int j = 0; j < SIMULTANEOUS_TESTS; j++) {
                futureList[j] = executorService.submit(new RandomUiTask("Iteration " + String.valueOf(i), synchronousTimeAgent, agentPolicy));
            }
            try {
                for (int j = 0; j < SIMULTANEOUS_TESTS; j++) {
                    futureList[j].get();
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Caught java.lang.InterruptedException", e);
                assertTrue("Test was interrupted", false);
            } catch (ExecutionException e) {
                Log.e(TAG, "Caught java.util.concurrent.ExecutionException", e);
                assertTrue("Test threw an exception", false);
            }
        }

    }

    public static boolean isTimeAcceptable(long time) {
        return Math.abs(SystemClock.uptimeMillis() - time) < MAX_CACHE_AGE_MS + 250;
    }

    private static class RandomUiTask extends Thread {

        private final SynchronousTimeAgent mAgent;
        private final AgentPolicy mAgentPolicy;
        private final String mName;
        private final Random mRandom;
        public boolean mResultReceived;
        private boolean mReleased;

        public RandomUiTask(String name, SynchronousTimeAgent agent, AgentPolicy agentPolicy) {
            mName = name;
            mAgent = agent;
            mAgentPolicy = agentPolicy;
            mRandom = new Random();
        }

        @Override
        public void run() {
            super.run();

            if (mRandom.nextFloat() < PRE_EXECUTION_DITHER_PROBABILITY) {
                TestUtils.safeSleep(mRandom.nextInt(PRE_EXECUTION_DITHER));
            }

            Long startTime = SystemClock.uptimeMillis();

            AgentTether agentTether = AgentExecutor.getDefault().runAgent(mAgent, mAgentPolicy, mAgentListener);

            TestUtils.safeSleep(ROTATION_MIN_TIME_MS + mRandom.nextInt(ROTATION_MAX_TIME_MS));

            if (SystemClock.uptimeMillis() - startTime > EXECUTION_TIME_MS + 100) {
                assertTrue(mName + "Should have definitely received a result, but did not", mResultReceived);
            }

            agentTether.release();
            mReleased = true;
        }

        private AgentListener<Long, Float> mAgentListener = new AgentListener<Long, Float>() {

            @Override
            public void onCompletion(String agentIdentifier, Long result) {
                assertTrue(mName + " result was too old.", isTimeAcceptable(result));
                assertFalse(mName + " result delivered after release", mReleased);
                mResultReceived = true;
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        };

    }
}
