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

package com.bottlerocketstudios.groundcontrol.test.integration;

import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.test.TestUtils;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;

public class AgentExecutorIntegrationTest extends AndroidTestCase {
    private static final String TAG = AgentExecutorIntegrationTest.class.getSimpleName();

    public static final int EXECUTION_TIME_MS = 5000;
    public static final int FUDGE_TIME_MS = 1000;
    public static final String TEST_ID_1 = "testId1";

    public void testNormalOperation() {
        SynchronousAgent synchronousAgent = new SynchronousAgent(TEST_ID_1, EXECUTION_TIME_MS);

        final TestUtils.Container<Boolean> testCompleted = new TestUtils.Container<>(false);

        AgentExecutor.getDefault().runAgent(synchronousAgent, new AgentListener<String, Float>() {

            boolean receivedProgressUpdate;

            @Override
            public void onCompletion(String agentIdentifier, String result) {
                assertEquals("Incorrect agent identifier", TEST_ID_1, agentIdentifier);
                assertNotNull("Result was null", result);
                assertTrue("Result did not contain expected value", result.contains(agentIdentifier));
                assertTrue("Did not receive progress updates", receivedProgressUpdate);
                testCompleted.setValue(true);
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {
                assertEquals("Incorrect agent identifier", TEST_ID_1, agentIdentifier);
                assertTrue("Progress was 0", progress > 0.0f);
                Log.d(TAG, "Progress Update: " + progress);
                receivedProgressUpdate = true;
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
