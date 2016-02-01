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

import java.util.List;

/**
 * Wraps an ExecutorService and handles queueing and execution of jobs in a multithreaded fashion.
 */
public interface PriorityQueueingPoolExecutorService {

    /**
     * Add the list of jobs to the work queue
     */
    void enqueue(List<Job> jobList);

    /**
     * Add the job(s) to the work queue
     */
    void enqueue(Job ... jobList);

    /**
     * Update the priority on a queued job identified by the jobId to the supplied priority.
     * If the priority changes to {@link com.bottlerocketstudios.groundcontrol.executor.JobPriority#IMMEDIATE}
     * then the job will be executed immediately.
     */
    void updateJobPriority(long jobId, JobPriority priority);

    /**
     * Return a number unique to this instance of the service which can be used for the next job.
     */
    long getNextJobId();

    /**
     * Return true if there are any jobs queued or executing currently.
     */
    boolean hasRunningJobs();

    /**
     * Return true if the service has been put into idle state due to inactivity.
     */
    boolean isIdle();
}
