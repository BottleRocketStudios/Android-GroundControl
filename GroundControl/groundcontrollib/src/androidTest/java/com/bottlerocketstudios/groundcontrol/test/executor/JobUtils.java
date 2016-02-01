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

import android.util.Log;

import com.bottlerocketstudios.groundcontrol.executor.IdSequence;
import com.bottlerocketstudios.groundcontrol.executor.Job;
import com.bottlerocketstudios.groundcontrol.executor.JobPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JobUtils {

    public static List<Job> createRandomJobs(IdSequence idSequence, int jobCount, JobPriority priority, long minimumRunnableTimeMs, int maxRunnableDeviationMs, long maxProcessingTime) {
        List<Job> jobList = new ArrayList<>(jobCount);
        for (int i = 0; i < jobCount; i++) {
            Job job = new Job(idSequence.getNext(), new JunkRunnable(minimumRunnableTimeMs, maxRunnableDeviationMs), maxProcessingTime);
            job.setPriority(priority);
            jobList.add(job);
        }
        return jobList;
    }

    public static class JunkRunnable implements Runnable {
        private static final String TAG = JunkRunnable.class.getSimpleName();

        static Random sRandomSource = new Random();
        private final int mMaxRunnableDeviationMs;
        private final long mMinimumRunnableTimeMs;

        public JunkRunnable(long minimumRunnableTimeMs, int maxRunnableDeviationMs) {
            mMinimumRunnableTimeMs = minimumRunnableTimeMs;
            mMaxRunnableDeviationMs = maxRunnableDeviationMs;
        }

        @Override
        public void run() {
            try {
                long sleepIntervalMs = mMinimumRunnableTimeMs + sRandomSource.nextInt(mMaxRunnableDeviationMs);
                Log.d(TAG, "Sleeping " + sleepIntervalMs);
                Thread.sleep(sleepIntervalMs);
            } catch (InterruptedException e) {
                Log.e(TAG, "Caught java.lang.InterruptedException", e);
            }
        }
    }

}
