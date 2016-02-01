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

import android.test.AndroidTestCase;
import android.util.Log;

import com.bottlerocketstudios.groundcontrol.convenience.GroundControl;
import com.bottlerocketstudios.groundcontrol.listener.FunctionalAgentListener;
import com.bottlerocketstudios.groundcontrol.test.TestUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DependencyHandlingAgentTest extends AndroidTestCase {

    private static final int NUMBER_OF_TEST_DEPENDENCIES = 30;
    private static final int MAX_CONCURRENT_AGENTS = 10;
    private static final int MIN_CONCURRENT_AGENTS = 5;
    private static final int TEST_ITERATIONS = 10;

    public void testDependencyHandlingAgent() {
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            runIteration();
        }
    }

    private void runIteration() {
        final TestUtils.Container<List<Long>> resultContainer = new TestUtils.Container<>(null);

        GroundControl.agent(new MultipleDependencyAgent(NUMBER_OF_TEST_DEPENDENCIES, MAX_CONCURRENT_AGENTS, MIN_CONCURRENT_AGENTS)).bgParallelCallback(new FunctionalAgentListener<List<Long>, Void>() {
            @Override
            public void onCompletion(String agentIdentifier, List<Long> result) {
                resultContainer.setValue(result);
            }
        }).execute();

        TestUtils.blockUntilNotNullOrTimeout(resultContainer, 10, TimeUnit.SECONDS.toMillis(NUMBER_OF_TEST_DEPENDENCIES));

        assertNotNull("Container had no value", resultContainer.getValue());
        assertEquals("Wrong number of results", NUMBER_OF_TEST_DEPENDENCIES, resultContainer.getValue().size());
        for (Long value: resultContainer.getValue()) {
            Log.d("DELETEME", "Execution completed at time " + value);
        }
    }

}
