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

import com.bottlerocketstudios.groundcontrol.HashBuilder;

/**
 * A unit of work to be used by the {@link PriorityQueueingPoolExecutorService}
 */
public class Job {
    private final long mId;
    private JobPriority mPriority;
    private final Runnable mRunnable;
    private final long mMaximumExecutionTimeMs;
    private JobExecutionListener mJobExecutionListener;

    private volatile int mHashCode;

    public Job(long id, Runnable runnable, long maximumExecutionTimeMs, JobPriority jobPriority) {
        mId = id;
        mRunnable = runnable;
        mPriority = jobPriority;
        mMaximumExecutionTimeMs = maximumExecutionTimeMs;
    }

    public Job(long id, Runnable runnable, long maximumExecutionTimeMs) {
        this(id, runnable, maximumExecutionTimeMs, JobPriority.NORMAL);
    }

    public JobPriority getPriority() {
        return mPriority;
    }

    public void setPriority(JobPriority priority) {
        mPriority = priority;
    }

    public Runnable getRunnable() {
        return mRunnable;
    }

    public long getId() {
        return mId;
    }

    public long getMaximumExecutionTimeMs() {
        return mMaximumExecutionTimeMs;
    }

    public void setJobExecutionListener(JobExecutionListener jobExecutionListener) {
        mJobExecutionListener = jobExecutionListener;
    }

    public void notifyJobExecuted() {
        if (mJobExecutionListener != null) {
            mJobExecutionListener.onJobExecuted(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Job) {
            return this.getId() == ((Job) o).getId();
        } else if (o instanceof RunningJob) {
            return this.getId() == ((RunningJob) o).getJobId();
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = mHashCode;
        if (result == 0) {
            result = (new HashBuilder()).addHashLong(getId()).build();
            mHashCode = result;
        }
        return result;
    }
}
