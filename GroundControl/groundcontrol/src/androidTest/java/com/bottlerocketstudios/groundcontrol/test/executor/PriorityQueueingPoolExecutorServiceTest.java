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

import com.bottlerocketstudios.groundcontrol.executor.IdSequence;
import com.bottlerocketstudios.groundcontrol.executor.Job;
import com.bottlerocketstudios.groundcontrol.executor.JobPriority;
import com.bottlerocketstudios.groundcontrol.executor.PriorityQueueingPoolExecutorService;
import com.bottlerocketstudios.groundcontrol.executor.StandardPriorityQueueingPoolExecutorService;
import com.bottlerocketstudios.groundcontrol.test.TestUtils;
import com.bottlerocketstudios.groundcontrol.inactivity.StandardInactivityCleanupRunnable;

import java.util.List;

public class PriorityQueueingPoolExecutorServiceTest extends AndroidTestCase {

    private static final String TAG = PriorityQueueingPoolExecutorServiceTest.class.getSimpleName();

    public static final int FIRST_BATCH_JOB_COUNT = 50;
    public static final int SECOND_BATCH_JOB_COUNT = 20;
    public static final int MINIMUM_RUNNABLE_TIME_MS = 1000;
    public static final int MAX_RUNNABLE_DEVIATION_MS = 100;
    public static final int MAX_SIMULTANEOUS_JOBS = 10;
    public static final long IDLE_TIMEOUT_MS = 5000L;
    public static final long TEST_INTERVAL_MS = 1000;

    //Limit execution to approximately the absolute minimum time required.
    private static final long MAXIMUM_PROCESSING_TIME =
            (FIRST_BATCH_JOB_COUNT * (MINIMUM_RUNNABLE_TIME_MS + MAX_RUNNABLE_DEVIATION_MS)) / MAX_SIMULTANEOUS_JOBS
            + (SECOND_BATCH_JOB_COUNT * (MINIMUM_RUNNABLE_TIME_MS + MAX_RUNNABLE_DEVIATION_MS)) / MAX_SIMULTANEOUS_JOBS
            + 2 * IDLE_TIMEOUT_MS
            + 4 * TEST_INTERVAL_MS;

    private long mTestStart;

    public void testQueueingExecution() {
        mTestStart = SystemClock.uptimeMillis();
        //Create an executor service that will go idle after 5 seconds of no work.
        PriorityQueueingPoolExecutorService priorityQueueingPoolExecutorService =
                StandardPriorityQueueingPoolExecutorService.builder()
                        .setJobCleanupRunnable(new StandardInactivityCleanupRunnable(IDLE_TIMEOUT_MS, 100L))
                        .setMaxSimultaneousJobs(MAX_SIMULTANEOUS_JOBS)
                        .setEnableLogging(false)
                        .build();
        assertNotNull("Builder returned empty object", priorityQueueingPoolExecutorService);

        IdSequence idSequence = new IdSequence();

        List<Job> jobListA = JobUtils.createRandomJobs(idSequence, FIRST_BATCH_JOB_COUNT, JobPriority.NORMAL, MINIMUM_RUNNABLE_TIME_MS, MAX_RUNNABLE_DEVIATION_MS, MAXIMUM_PROCESSING_TIME);
        priorityQueueingPoolExecutorService.enqueue(jobListA);

        waitOnCompletionThenIdle(priorityQueueingPoolExecutorService);

        //Run some more jobs after it enters idle.
        List<Job> jobListB = JobUtils.createRandomJobs(idSequence, SECOND_BATCH_JOB_COUNT, JobPriority.NORMAL, MINIMUM_RUNNABLE_TIME_MS, MAX_RUNNABLE_DEVIATION_MS, MAXIMUM_PROCESSING_TIME);
        priorityQueueingPoolExecutorService.enqueue(jobListB);

        waitOnCompletionThenIdle(priorityQueueingPoolExecutorService);

        Log.d(TAG, "Execution took " + String.valueOf(MAXIMUM_PROCESSING_TIME - getExecutionDuration()) + "ms less than limit");
    }

    private boolean isOverMaxTime() {
        return getExecutionDuration() > MAXIMUM_PROCESSING_TIME;
    }

    private long getExecutionDuration() {
        return SystemClock.uptimeMillis() - mTestStart;
    }

    private void waitOnCompletionThenIdle(PriorityQueueingPoolExecutorService priorityQueueingPoolExecutorService) {
        while (priorityQueueingPoolExecutorService.hasRunningJobs() && !isOverMaxTime()) {
            TestUtils.safeSleep(TEST_INTERVAL_MS);
        }

        assertNotOverTime();
        assertFalse("Went into idle too soon.", priorityQueueingPoolExecutorService.isIdle());

        while (!priorityQueueingPoolExecutorService.isIdle() && !isOverMaxTime()) {
            TestUtils.safeSleep(TEST_INTERVAL_MS);
        }

        assertNotOverTime();
        assertTrue("Failed to enter idle.", priorityQueueingPoolExecutorService.isIdle());
    }

    private void assertNotOverTime() {
        assertFalse("Went over time limit " + String.valueOf(MAXIMUM_PROCESSING_TIME) + "ms by taking " + String.valueOf(getExecutionDuration()) + "ms", isOverMaxTime());
    }



}
