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

package com.bottlerocketstudios.groundcontrol.test.executor;

import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

import com.bottlerocketstudios.groundcontrol.executor.Job;
import com.bottlerocketstudios.groundcontrol.executor.PriorityQueueingPoolExecutorService;
import com.bottlerocketstudios.groundcontrol.executor.StandardPriorityQueueingPoolExecutorService;
import com.bottlerocketstudios.groundcontrol.test.TestUtils;

public class JobTimeoutTest extends AndroidTestCase {
    private static final String TAG = JobTimeoutTest.class.getSimpleName();

    private static final int MAXIMUM_RUNNABLE_TIME_MS = 2000;
    private static final int POLLING_INTERVAL_MS = 53;
    private static final int MAXIMUM_TOTAL_TIME_MS = MAXIMUM_RUNNABLE_TIME_MS + 4 * POLLING_INTERVAL_MS;

    public void testTimeoutEnforcement() {
        PriorityQueueingPoolExecutorService priorityQueueingPoolExecutorService = StandardPriorityQueueingPoolExecutorService.builder().build();

        InterruptedRunnable interruptedRunnable = new InterruptedRunnable();
        Job testJob = new Job(1, interruptedRunnable, MAXIMUM_RUNNABLE_TIME_MS);
        priorityQueueingPoolExecutorService.enqueue(testJob);

        long startTime = SystemClock.uptimeMillis();
        while(priorityQueueingPoolExecutorService.hasRunningJobs()) {
            TestUtils.safeSleep(POLLING_INTERVAL_MS);
        }
        long interruptionDuration = SystemClock.uptimeMillis() - startTime;

        assertTrue("Interruption did not happen after " + interruptionDuration + "ms", interruptedRunnable.isInterrupted());
        assertTrue("Interruption did not happen quickly enough after " + interruptionDuration + "ms", interruptionDuration < MAXIMUM_TOTAL_TIME_MS);
        assertTrue("Interruption happened early @ " + interruptedRunnable.getRunningDuration() + "ms", interruptedRunnable.getRunningDuration() > MAXIMUM_RUNNABLE_TIME_MS);
    }

    public static class InterruptedRunnable implements Runnable {
        private static final String TAG = InterruptedRunnable.class.getSimpleName();
        private volatile boolean mInterrupted;
        private volatile long mRunningDuration;

        @Override
        public void run() {
            long startTime = SystemClock.uptimeMillis();
            try {
                long sleepIntervalMs = MAXIMUM_RUNNABLE_TIME_MS * 2;
                Log.d(TAG, "Sleeping " + sleepIntervalMs);
                Thread.sleep(sleepIntervalMs);
            } catch (InterruptedException e) {
                mInterrupted = true;
                Log.i(TAG, "Caught java.lang.InterruptedException as expected");
            }
            mRunningDuration = SystemClock.uptimeMillis() - startTime;
        }

        public boolean isInterrupted() {
            return mInterrupted;
        }

        public long getRunningDuration() {
            return mRunningDuration;
        }
    }

}
