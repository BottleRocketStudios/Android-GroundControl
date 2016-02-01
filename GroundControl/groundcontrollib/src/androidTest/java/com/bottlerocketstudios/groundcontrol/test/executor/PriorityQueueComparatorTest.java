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

import android.test.AndroidTestCase;

import com.bottlerocketstudios.groundcontrol.executor.IdSequence;
import com.bottlerocketstudios.groundcontrol.executor.Job;
import com.bottlerocketstudios.groundcontrol.executor.JobPriority;
import com.bottlerocketstudios.groundcontrol.executor.JobPriorityAndIdComparator;

import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public class PriorityQueueComparatorTest extends AndroidTestCase {

    private static final String TAG = PriorityQueueComparatorTest.class.getSimpleName();

    private static final int JOBS_PER_PRIORITY = 10;

    public void testJobListOrdering() {
        PriorityBlockingQueue<Job> jobQueue = new PriorityBlockingQueue<>(JobPriority.values().length * JOBS_PER_PRIORITY, new JobPriorityAndIdComparator());

        IdSequence idSequence = new IdSequence();

        List<Job> normalJobs = JobUtils.createRandomJobs(idSequence, JOBS_PER_PRIORITY, JobPriority.NORMAL, 10, 10, 10);
        List<Job> highJobs = JobUtils.createRandomJobs(idSequence, JOBS_PER_PRIORITY, JobPriority.HIGH, 10, 10, 10);
        List<Job> lowJobs = JobUtils.createRandomJobs(idSequence, JOBS_PER_PRIORITY, JobPriority.LOW, 10, 10, 10);
        List<Job> immediateJobs = JobUtils.createRandomJobs(idSequence, JOBS_PER_PRIORITY, JobPriority.IMMEDIATE, 10, 10, 10);

        jobQueue.addAll(normalJobs);
        jobQueue.addAll(highJobs);
        jobQueue.addAll(lowJobs);
        jobQueue.addAll(immediateJobs);

        JobPriority expectedPriority = JobPriority.IMMEDIATE;
        int counter = 0;
        long lastId = 0;
        while (!jobQueue.isEmpty()) {
            counter++;
            Job job = jobQueue.remove();
            assertEquals("Unexpected Priority", expectedPriority, job.getPriority());

            if (lastId > 0) {
                assertTrue("Id Sequence out of order for equal priority " + String.valueOf(lastId) + " > " + String.valueOf(job.getId()), lastId < job.getId());
            }
            lastId = job.getId();

            if (counter >= JOBS_PER_PRIORITY) {
                counter = 0;
                lastId = 0;
                if (expectedPriority.ordinal() < JobPriority.values().length - 1) {
                    expectedPriority = JobPriority.values()[expectedPriority.ordinal() + 1];
                }
            }
        }
    }
}
