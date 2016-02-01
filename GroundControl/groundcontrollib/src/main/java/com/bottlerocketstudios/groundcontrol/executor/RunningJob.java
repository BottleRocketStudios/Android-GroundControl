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

import java.util.concurrent.Future;

/**
 * Encapsulates a Job that has been executed along with associated time values.
 */
public class RunningJob {
    private final Job mJob;
    private final Future mFuture;
    private long mDeadlineTimestamp;

    private volatile int mHashCode;

    public RunningJob(Job job, Future future, long startTime) {
        mJob = job;
        mFuture = future;
        trackJobStarted(startTime);
    }

    public long getJobId() {
        return mJob.getId();
    }

    public boolean isComplete() {
        return mFuture.isDone();
    }

    public void cancel(boolean interrupt) {
        mFuture.cancel(interrupt);
    }

    private void trackJobStarted(long currentTime) {
        mDeadlineTimestamp = currentTime + mJob.getMaximumExecutionTimeMs();
    }

    public boolean isPastExecutionTimeLimit(long currentTime) {
        return currentTime > mDeadlineTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RunningJob) {
            return this.getJobId() == ((RunningJob) o).getJobId();
        } else if (o instanceof Job) {
            return this.getJobId() == ((Job) o).getId();
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = mHashCode;
        if (result == 0) {
            result = (new HashBuilder()).addHashLong(getJobId()).build();
            mHashCode = result;
        }
        return result;
    }
}
