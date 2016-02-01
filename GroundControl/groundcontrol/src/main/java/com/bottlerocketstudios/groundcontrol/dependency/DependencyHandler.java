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

import com.bottlerocketstudios.groundcontrol.convenience.ExecutionBuilder;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.tether.AgentTether;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controls concurrent execution of multiple dependencies used by {@link DependencyHandlingAgent}
 */
public class DependencyHandler {

    private final List<ExecutionBuilder<?, ?>> mPendingExecutionList = new LinkedList<>();
    private final AtomicInteger mAtomicCountdown = new AtomicInteger(0);
    private final List<AgentTether> mAgentTetherList = new LinkedList<>();
    private final DependencyHandlerListener mDependencyHandlerListener;

    public DependencyHandler(DependencyHandlerListener listener) {
        mDependencyHandlerListener = listener;
    }

    public List<AgentTether> executeDependencies() {
        synchronized (mPendingExecutionList) {
            mAtomicCountdown.addAndGet(mPendingExecutionList.size());
            for (ExecutionBuilder<?, ?> executionBuilder : mPendingExecutionList) {
                mAgentTetherList.add(executionBuilder.execute());
            }
            mPendingExecutionList.clear();
        }
        return mAgentTetherList;
    }

    public <R, P> void addParallelDependency(ExecutionBuilder<R, P> executionBuilder, AgentListener<R, P> agentListener) {
        WrappedListener<R, P> wrappedListener = new WrappedListener<>(this, agentListener);
        executionBuilder.bgParallelCallback(wrappedListener);
        addDependency(executionBuilder);
    }

    public <R, P> void addSerialDependency(ExecutionBuilder<R, P> executionBuilder, AgentListener<R, P> agentListener) {
        WrappedListener<R, P> wrappedListener = new WrappedListener<>(this, agentListener);
        executionBuilder.bgSerialCallback(wrappedListener);
        addDependency(executionBuilder);
    }

    public <R, P> void addDependency(ExecutionBuilder<R, P> executionBuilder) {
        synchronized (mPendingExecutionList) {
            mPendingExecutionList.add(executionBuilder);
        }
    }

    public void cancel() {
        if (mAgentTetherList != null) {
            for (AgentTether agentTether: mAgentTetherList) {
                agentTether.cancel();
            }
        }
    }

    private void countdown() {
        if (mAtomicCountdown.decrementAndGet() == 0) {
            mDependencyHandlerListener.onDependenciesCompleted();
        }
    }

    public interface DependencyHandlerListener {
        void onDependenciesCompleted();
    }

    public static class WrappedListener<R, P> implements AgentListener<R, P> {
        private final DependencyHandler mDependencyHandler;
        private final AgentListener<R, P> mAgentListener;

        private WrappedListener(DependencyHandler dependencyHandler, AgentListener<R, P> agentListener) {
            mDependencyHandler = dependencyHandler;
            mAgentListener = agentListener;
        }

        @Override
        public void onCompletion(String agentIdentifier, R result) {
            mAgentListener.onCompletion(agentIdentifier, result);
            mDependencyHandler.countdown();
        }

        @Override
        public void onProgress(String agentIdentifier, P progress) {
            mAgentListener.onProgress(agentIdentifier, progress);
        }
    }
}
