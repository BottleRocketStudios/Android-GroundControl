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

package com.bottlerocketstudios.groundcontrol.executor;

import android.os.SystemClock;
import android.util.Log;

import com.bottlerocketstudios.groundcontrol.inactivity.InactivityCleanupListener;
import com.bottlerocketstudios.groundcontrol.inactivity.InactivityCleanupRunnable;
import com.bottlerocketstudios.groundcontrol.inactivity.StandardInactivityCleanupRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Standard implementation of the {@link PriorityQueueingPoolExecutorService}. This will execute queued
 * jobs based on their priority and discard overdue jobs.
 */
public class StandardPriorityQueueingPoolExecutorService implements PriorityQueueingPoolExecutorService, InactivityCleanupListener {

    private static final JobPriorityAndIdComparator JOB_PRIORITY_COMPARATOR = new JobPriorityAndIdComparator();
    private static final int DEFAULT_QUEUE_SIZE = 10;

    private final int mMaxSimultaneousJobs;
    private final PriorityBlockingQueue<Job> mJobQueue;
    private final List<RunningJob> mRunningJobs;
    private final boolean mEnableLogging;
    private final ThreadFactory mThreadFactory;
    private final String mLogTag;
    private final int mHighSpeedQueueThreshold;
    private final IdSequence mIdSequence;
    private final InactivityCleanupRunnable mJobCleanupRunnable;
    private final int mOsThreadPriority;

    private ExecutorService mExecutorService;
    private boolean mIdle;

    /**
     * Enforce Builder usage.
     */
    private StandardPriorityQueueingPoolExecutorService(Builder builder) {
        mMaxSimultaneousJobs = builder.maxSimultaneousJobs;
        mJobQueue = new PriorityBlockingQueue<>(DEFAULT_QUEUE_SIZE, JOB_PRIORITY_COMPARATOR);
        mRunningJobs = Collections.synchronizedList(new ArrayList<RunningJob>());
        mJobCleanupRunnable = builder.jobCleanupRunnable;
        mEnableLogging = builder.enableLogging;
        mThreadFactory = builder.threadFactory;
        mLogTag = builder.logTag;
        mHighSpeedQueueThreshold = builder.highSpeedQueueThreshold;
        mOsThreadPriority = builder.osThreadPriority;

        mIdSequence = new IdSequence();
    }

    @Override
    public void enqueue(List<Job> jobList) {
        for (Job job: jobList) {
            if (mEnableLogging) Log.i(mLogTag, "Job entered Queue " + job.toString());
            if (job.getPriority().equals(JobPriority.IMMEDIATE)) {
                if (mEnableLogging) Log.i(mLogTag, "Executing immediate priority work " + job.toString());
                executeJob(job);
            } else {
                if (mEnableLogging) Log.i(mLogTag, "Queueing job " + job.toString());
                mJobQueue.offer(job);
            }
        }
        processQueue();
    }

    @Override
    public void enqueue(Job ... jobList) {
        enqueue(Arrays.asList(jobList));
    }

    private void processQueue() {
        synchronized (mJobQueue) {
            while (mJobQueue.size() > 0 && mRunningJobs.size() < mMaxSimultaneousJobs) {
                Job job = mJobQueue.remove();
                executeJob(job);
            }
        }
        if (mJobQueue.size() > mHighSpeedQueueThreshold && !mJobCleanupRunnable.isHighSpeedMode()) {
            if (mEnableLogging) Log.i(mLogTag, "The job queue is very large, entering high speed queue processing mode.");
            mJobCleanupRunnable.enterHighSpeedMode();
        } else if (mJobQueue.size() < mHighSpeedQueueThreshold && mJobCleanupRunnable.isHighSpeedMode()) {
            if (mEnableLogging) Log.i(mLogTag, "Exiting high speed queue processing mode.");
            mJobCleanupRunnable.exitHighSpeedMode();
        }
    }

    /**
     * Start job running on the ExecutorService.
     */
    private void executeJob(Job job) {
        if (mEnableLogging) Log.i(mLogTag, "Executing job " + job.toString());
        mIdle = false;
        mJobCleanupRunnable.restartTimer();
        mRunningJobs.add(new RunningJob(job, getExecutorService().submit(job.getRunnable()), getTime()));
        job.notifyJobExecuted();
    }

    private long getTime() {
        return SystemClock.uptimeMillis();
    }

    @Override
    public boolean isBusy() {
        return hasRunningJobs();
    }

    @Override
    public void enterIdleState() {
        mIdle = true;
        if(mExecutorService != null) {
            if (mEnableLogging) Log.i(mLogTag, "Entering idle state");
            mExecutorService.shutdown();
            mExecutorService = null;
        }
    }

    @Override
    public void performCleanup() {
        synchronized (mRunningJobs) {
            for (Iterator<RunningJob> runningJobIterator = mRunningJobs.iterator(); runningJobIterator.hasNext(); ) {
                RunningJob runningJob = runningJobIterator.next();
                long now = getTime();
                if (runningJob.isComplete()) {
                    if (mEnableLogging) Log.i(mLogTag, "Cleaning up completed job " + runningJob.toString());
                    runningJobIterator.remove();
                } else if (runningJob.isPastExecutionTimeLimit(now)) {
                    Log.w(mLogTag, "Killing overdue job " + runningJob.toString());
                    runningJob.cancel(true);
                    runningJobIterator.remove();
                }
            }
        }

        processQueue();
    }

    /**
     * Lazy load ExecutorService and reinitialize if we have been idled.
     */
    private ExecutorService getExecutorService() {
        if (mExecutorService == null) {
            if (mEnableLogging) Log.i(mLogTag, "Creating ExecutorService");
            mExecutorService = ThreadPoolExecutorWithExceptions.newCachedThreadPool(mThreadFactory, mOsThreadPriority);
        }
        return mExecutorService;
    }

    @Override
    public boolean isIdle() {
        return mIdle;
    }

    @Override
    public long getNextJobId() {
        return mIdSequence.getNext();
    }

    @Override
    public boolean hasRunningJobs() {
        return !(mRunningJobs.size() == 0 && mJobQueue.size() == 0);
    }

    @Override
    public void updateJobPriority(long jobId, JobPriority priority) {

        synchronized (mRunningJobs) {
            for (RunningJob runningJob : mRunningJobs) {
                //First determine if job is already running and skip it if it is.
                if (runningJob.getJobId() == jobId) {
                    return;
                }
            }
        }

        synchronized (mJobQueue) {
            for (Iterator<Job> jobIterator = mJobQueue.iterator(); jobIterator.hasNext(); ) {
                Job job = jobIterator.next();
                if (job.getId() == jobId) {
                    //Found it in the queue.
                    if (priority.equals(JobPriority.IMMEDIATE)) {
                        //If it is immediate priority, remove from queue and execute.
                        jobIterator.remove();
                        executeJob(job);
                    } else {
                        //Otherwise, just change the priority. It will dequeue appropriately.
                        job.setPriority(priority);
                    }
                    return;
                }
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private static final long DEFAULT_INACTIVITY_IDLE_MS = TimeUnit.MINUTES.toMillis(2);
        private static final long DEFAULT_NORMAL_CLEANUP_INTERVAL_MS = 100;
        private static final long DEFAULT_HIGH_SPEED_CLEANUP_INTERVAL_MS = 5;
        private static final int DEFAULT_MAX_SIMULTANEOUS_JOBS = 10;
        private static final int DEFAULT_HIGH_SPEED_QUEUE_THRESHOLD = 20;
        private static final int DEFAULT_OS_THREAD_PRIORITY = android.os.Process.THREAD_PRIORITY_BACKGROUND;

        private InactivityCleanupRunnable jobCleanupRunnable;
        private int maxSimultaneousJobs;
        private Boolean enableLogging;
        private ThreadFactory threadFactory;
        private String logTag;
        private int highSpeedQueueThreshold;
        private Integer osThreadPriority;

        public PriorityQueueingPoolExecutorService build() {
            //Configure defaults if unspecified

            if (logTag == null) {
                logTag = StandardPriorityQueueingPoolExecutorService.class.getSimpleName();
            }

            if (enableLogging == null) {
                enableLogging = false;
            }

            if (enableLogging) {
                Log.w(logTag, "Logging is enabled. This will reduce performance.");
            }

            if (jobCleanupRunnable == null) {
                setJobCleanupRunnable(new StandardInactivityCleanupRunnable(DEFAULT_INACTIVITY_IDLE_MS, DEFAULT_NORMAL_CLEANUP_INTERVAL_MS, DEFAULT_HIGH_SPEED_CLEANUP_INTERVAL_MS));
            }

            if (maxSimultaneousJobs <= 0) {
                setMaxSimultaneousJobs(DEFAULT_MAX_SIMULTANEOUS_JOBS);
            }

            if (threadFactory == null) {
                setThreadFactory(Executors.defaultThreadFactory());
            }

            if (highSpeedQueueThreshold <= 0) {
                setHighSpeedQueueThreshold(DEFAULT_HIGH_SPEED_QUEUE_THRESHOLD);
            }

            if (osThreadPriority == null) {
                setOsThreadPriority(DEFAULT_OS_THREAD_PRIORITY);
            }

            //Finish creation and linking.

            StandardPriorityQueueingPoolExecutorService priorityQueueingPoolExecutorService = new StandardPriorityQueueingPoolExecutorService(this);
            jobCleanupRunnable.setListener(priorityQueueingPoolExecutorService);

            return priorityQueueingPoolExecutorService;
        }

        /**
         * Runnable that will handle notifying the service to cleanup expired items and enter idle state.
         */
        public Builder setJobCleanupRunnable(InactivityCleanupRunnable jobCleanupRunnable) {
            this.jobCleanupRunnable = jobCleanupRunnable;
            return this;
        }

        /**
         * Maximum number of prioritized jobs to execute simultaneously. Immediate priority jobs will
         * be executed immediately, but will count toward the total when scheduling the next job.
         */
        public Builder setMaxSimultaneousJobs(int maxSimultaneousJobs) {
            this.maxSimultaneousJobs = maxSimultaneousJobs;
            return this;
        }

        /**
         * Enable logging for debugging purposes.
         */
        public Builder setEnableLogging(boolean enableLogging) {
            this.enableLogging = enableLogging;
            return this;
        }

        /**
         * Provide a thread factory to use for threads executed by this service. 
         */
        public Builder setThreadFactory(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
            return this;
        }

        /**
         * Set the log tag to be used with this service if logging is enabled.
         */
        public Builder setLogTag(String logTag) {
            this.logTag = logTag;
            return this;
        }

        /**
         * Set the threshold size of the queue at which the queue will be processed at the InactivityCleanupRunnable high speed interval.
         */
        public Builder setHighSpeedQueueThreshold(int highSpeedQueueThreshold) {
            this.highSpeedQueueThreshold = highSpeedQueueThreshold;
            return this;
        }

        /**
         * Set the Linux OS Thread priority that thread pool executions will run with.
         * Use priority values from android.os.Process.
         */
        public Builder setOsThreadPriority(int osThreadPriority) {
            this.osThreadPriority = osThreadPriority;
            return this;
        }
    }
}
