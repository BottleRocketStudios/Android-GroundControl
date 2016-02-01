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

package com.bottlerocketstudios.groundcontrol.dependency;

import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;
import com.bottlerocketstudios.groundcontrol.convenience.ExecutionBuilder;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.tether.AgentTether;

import java.util.List;

/**
 * Extend this Agent to get automatic handling of the execution of dependencies
 * and notification when all of them have completed.
 *
 * <ul>
 *     <li>
 *          Once all of the dependencies have executed your implementation will get a callback via
 *          {@link DependencyHandlingAgent#onDependenciesCompleted()}
 *     </li>
 *     <li>
 *          It is possible to add dependencies and call executeDependencies multiple times, though
 *          you should typically add all of them then call executeDependencies once. The onDependenciesCompleted method
 *          will be called as many times as the number of outstanding executions hits zero which may
 *          be sooner than is expected for short duration work. If your process has multiple dependent
 *          phases it should be split into multiple agents.
 *      </li>
 *      <li>
 *          Mixing {@link DependencyHandlingAgent#addParallelDependency(ExecutionBuilder, AgentListener)} and
 *          {@link DependencyHandlingAgent#addSerialDependency(ExecutionBuilder, AgentListener)} should be
 *          done with care. The {@link DependencyHandlingAgent#onDependenciesCompleted()} will execute
 *          on whichever Thread delivers last in an indeterminate way, which is probably not what you
 *          expect if you are using serial callbacks.
 *      </li>
 *      <li>
 *          Only call {@link DependencyHandlingAgent#addDependency(ExecutionBuilder)} if you have fully
 *          read and understood the documentation on that method. It has been left protected instead of
 *          private to allow for some unforeseen use case where it is desired to call back on the UI thread
 *          or some equally deplorable situation.
 *      </li>
 * </ul>
 */
public abstract class DependencyHandlingAgent<ResultType, ProgressType> extends AbstractAgent<ResultType, ProgressType> implements DependencyHandler.DependencyHandlerListener {

    private final DependencyHandler mDependencyHandler = new DependencyHandler(this);

    /**
     * Add ExecutionBuilder built without a listener and the supplied listener. This will execute the
     * callback on a thread pool. Mixing with serial dependencies is
     * possible but sticking to one or the other is easier to reason about.
     */
    protected <R, P> void addParallelDependency(ExecutionBuilder<R, P> executionBuilder, AgentListener<R, P> agentListener) {
        mDependencyHandler.addParallelDependency(executionBuilder, agentListener);
    }

    /**
     * Add ExecutionBuilder built without a listener and the supplied listener.
     * This will execute the callback on a background Looper. Mixing with parallel dependencies is
     * possible but sticking to one or the other is easier to reason about.
     */
    protected <R, P> void addSerialDependency(ExecutionBuilder<R, P> executionBuilder, AgentListener<R, P> agentListener) {
        mDependencyHandler.addSerialDependency(executionBuilder, agentListener);
    }

    /**
     * Add an ExecutionBuilder with a {@link DependencyHandler.WrappedListener}
     * already associated with it. Using something other than a WrappedListener with this instance
     * as its callback listener will never call onDependenciesCompleted()
     */
    protected <R, P> void addDependency(ExecutionBuilder<R, P> executionBuilder) {
        mDependencyHandler.addDependency(executionBuilder);
    }

    /**
     * Call this after adding dependencies. It will execute any pending dependencies and increment
     * the AtomicInteger used to keep count of pending callbacks.
     */
    protected List<AgentTether> executeDependencies() {
        return mDependencyHandler.executeDependencies();
    }

    /**
     * Cancel all of the AgentTethers collected by calls to executeDependencies().
     */
    protected void cancelDependencies() {
        mDependencyHandler.cancel();
    }

    @Override
    public void cancel() {
        cancelDependencies();
    }

}
