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

package com.bottlerocketstudios.groundcontrol.test.convenience;

import android.os.SystemClock;
import android.test.AndroidTestCase;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.test.TestUtils;
import com.bottlerocketstudios.groundcontrol.test.integration.SynchronousAgent;
import com.bottlerocketstudios.groundcontrol.listener.FunctionalAgentListener;

public class TestConvenientListeners extends AndroidTestCase {

    private static final String TAG = TestConvenientListeners.class.getSimpleName();

    private static final String TEST_ID_1 = TAG + ".testId1";
    private static final long EXECUTION_TIME_MS = 200;
    private static final long FUDGE_TIME_MS = 200;

    public void testFunctionalListener() {
        SynchronousAgent synchronousAgent = new SynchronousAgent(TEST_ID_1, EXECUTION_TIME_MS);

        final TestUtils.Container<Boolean> testCompleted = new TestUtils.Container<>(false);

        AgentExecutor.getDefault().runAgent(synchronousAgent, new FunctionalAgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                testCompleted.setValue(true);
            }
        });

        long startTime = SystemClock.uptimeMillis();
        long deadline = startTime + EXECUTION_TIME_MS + FUDGE_TIME_MS;

        while(!testCompleted.getValue() && SystemClock.uptimeMillis() < deadline) {
            TestUtils.safeSleep(50);
        }
        assertTrue("Test was not completed on time gave up after " + String.valueOf(SystemClock.uptimeMillis() - startTime) + "ms", testCompleted.getValue());
    }

}
