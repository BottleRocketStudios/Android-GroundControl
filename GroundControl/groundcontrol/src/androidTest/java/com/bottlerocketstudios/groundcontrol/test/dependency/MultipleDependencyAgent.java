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

package com.bottlerocketstudios.groundcontrol.test.dependency;

import com.bottlerocketstudios.groundcontrol.dependency.DependencyHandlingAgent;
import com.bottlerocketstudios.groundcontrol.convenience.GroundControl;
import com.bottlerocketstudios.groundcontrol.listener.FunctionalAgentListener;
import com.bottlerocketstudios.groundcontrol.test.integration.SynchronousTimeAgent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MultipleDependencyAgent extends DependencyHandlingAgent<List<Long>, Void> {

    private static final String AGENT_ID_FORMAT = MultipleDependencyAgent.class.getCanonicalName() + "baseid_%1$d";

    //Try to cause collisions with only 3 execution durations 50, 100, 150 ms.
    private static final int MIN_INTERVAL = 50;
    private static final int INTERVAL_RANGE = 2;
    private static final int INTERVAL_STEP = 50;

    private final List<Long> mTestContainerList = Collections.synchronizedList(new LinkedList<Long>());
    private final int mNumberOfTestDependencies;
    private final int mMaximumConcurrentExecutions;
    private final int mMinimumConcurrentExecutions;
    private final AtomicInteger mConcurrentExecutions = new AtomicInteger(0);
    private final AtomicInteger mTotalExecutions = new AtomicInteger(0);
    private final Random mRandom = new Random();

    public MultipleDependencyAgent(int numberOfTestDependencies, int maximumConcurrentExecutions, int minimumConcurrentExecutions) {
        mNumberOfTestDependencies = numberOfTestDependencies;
        mMaximumConcurrentExecutions = maximumConcurrentExecutions;
        mMinimumConcurrentExecutions = minimumConcurrentExecutions;
    }

    @Override
    public String getUniqueIdentifier() {
        return MultipleDependencyAgent.class.getCanonicalName();
    }

    @Override
    public void onProgressUpdateRequested() {}

    @Override
    public void run() {
        startTheLimit();
    }

    private void startTheLimit() {
        while (mConcurrentExecutions.get() < mMaximumConcurrentExecutions && mTotalExecutions.get() < mNumberOfTestDependencies) {
            mConcurrentExecutions.incrementAndGet();
            mTotalExecutions.incrementAndGet();
            String tempId = String.format(Locale.US, AGENT_ID_FORMAT, mTotalExecutions.get());
            long interval = mRandom.nextInt(INTERVAL_RANGE) * INTERVAL_STEP + MIN_INTERVAL;
            SynchronousTimeAgent synchronousTimeAgent = new SynchronousTimeAgent(tempId, interval);

            addParallelDependency(GroundControl.agent(synchronousTimeAgent), new FunctionalAgentListener<Long, Float>() {
                @Override
                public void onCompletion(String agentIdentifier, Long result) {
                    mTestContainerList.add(result);
                    if (mConcurrentExecutions.decrementAndGet() < mMinimumConcurrentExecutions) {
                        startTheLimit();
                    }
                }
            });
        }
        executeDependencies();
    }

    @Override
    public void onDependenciesCompleted() {
        getAgentListener().onCompletion(getUniqueIdentifier(), mTestContainerList);
    }
}
