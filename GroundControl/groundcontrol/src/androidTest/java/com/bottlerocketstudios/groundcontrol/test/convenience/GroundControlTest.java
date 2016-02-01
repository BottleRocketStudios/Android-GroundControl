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

import com.bottlerocketstudios.groundcontrol.convenience.GroundControl;
import com.bottlerocketstudios.groundcontrol.test.TestUtils;
import com.bottlerocketstudios.groundcontrol.test.integration.SynchronousAgent;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.listener.FunctionalAgentListener;

public class GroundControlTest extends AndroidTestCase {

    private static final String TAG = GroundControlTest.class.getSimpleName();

    private static final String TEST_ID_1 = TAG + ".testId1";
    private static final String TEST_ID_2 = TAG + ".testId2";

    private static final long EXECUTION_TIME_MS = 200;
    private static final long FUDGE_TIME_MS = 500;

    private static final long ONE_TIME_EXECUTION_DURATION_MS = 2000;
    private static final String ONE_TIME_ID = "oneTimeId";
    private static final Float PHASE_ONE_PROGRESS_LIMIT = 0.25f;
    private static final Float PHASE_TWO_PROGRESS_LIMIT = 0.75f;

    private TestUtils.Container<Boolean> mPhaseThreeCompleted = new TestUtils.Container<>(false);
    private TestUtils.Container<Boolean> mPhaseFourCompleted = new TestUtils.Container<>(false);

    public void testGroundControlHappyPath() {
        SynchronousAgent synchronousAgent = new SynchronousAgent(TEST_ID_1, EXECUTION_TIME_MS);

        final TestUtils.Container<Boolean> testCompleted = new TestUtils.Container<>(false);

        GroundControl.agent(synchronousAgent).bgParallelCallback(new FunctionalAgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                testCompleted.setValue(true);
            }
        }).execute();

        long startTime = SystemClock.uptimeMillis();
        long deadline = startTime + EXECUTION_TIME_MS + FUDGE_TIME_MS;

        while(!testCompleted.getValue() && SystemClock.uptimeMillis() < deadline) {
            TestUtils.safeSleep(50);
        }
        assertTrue("Test was not completed on time gave up after " + String.valueOf(SystemClock.uptimeMillis() - startTime) + "ms", testCompleted.getValue());
    }

    public void testGroundControlOneTimeOperation() {

        SynchronousAgent synchronousAgent = new SynchronousAgent(TEST_ID_2, ONE_TIME_EXECUTION_DURATION_MS);

        final TestUtils.Container<Float> progressPhaseOne = new TestUtils.Container<>(0.0f);

        //Simulate UI attachment for later onDestroy call using the object we have handy.
        GroundControl.uiAgent(progressPhaseOne, synchronousAgent)
                .oneTime(ONE_TIME_ID)
                .uiCallback(new AgentListener<String, Float>() {
                    @Override
                    public void onCompletion(String agentIdentifier, String result) {
                        assertTrue("This completion listener should not have been called.", false);
                    }

                    @Override
                    public void onProgress(String agentIdentifier, Float progress) {
                        assertTrue("Progress went beyond limit", progress < PHASE_TWO_PROGRESS_LIMIT);
                        if (progress > PHASE_ONE_PROGRESS_LIMIT) {
                            //Release this listener
                            GroundControl.onDestroy(progressPhaseOne);
                            startPhaseTwo();
                        }
                        progressPhaseOne.setValue(progress);
                    }
                })
                .bypassCache(true)
                .execute();

        TestUtils.safeSleep(ONE_TIME_EXECUTION_DURATION_MS + FUDGE_TIME_MS);
        assertTrue("Phase 3 did not complete", mPhaseThreeCompleted.getValue());
        assertTrue("Phase 4 did not complete", mPhaseFourCompleted.getValue());
    }

    /**
     * Simulate that the device was rotated between PHASE_ONE_PROGRESS_LIMIT and PHASE_TWO_PROGRESS_LIMIT
     */
    private void startPhaseTwo() {
        final TestUtils.Container<Float> progressPhaseTwo = new TestUtils.Container<>(0.0f);
        //Simulate UI reattachment for later onDestroy call using the object we have handy.
        GroundControl.reattachToOneTime(progressPhaseTwo, ONE_TIME_ID, new AgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                assertTrue("This completion listener should not have been called.", false);
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {
                assertTrue("Progress went backward", progress > PHASE_ONE_PROGRESS_LIMIT);
                if (progress > PHASE_TWO_PROGRESS_LIMIT) {
                    //Release this listener
                    GroundControl.onDestroy(progressPhaseTwo);
                    startPhaseThree();
                }
                progressPhaseTwo.setValue(progress);
            }
        });
    }

    /**
     * Simulate that the device was rotated between PHASE_TWO_PROGRESS_LIMIT and completion
     */
    private void startPhaseThree() {
        //Simulate UI reattachment for later onDestroy call using the object we have handy.
        GroundControl.reattachToOneTime(mPhaseThreeCompleted, ONE_TIME_ID, new AgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                mPhaseThreeCompleted.setValue(true);
                GroundControl.onDestroy(mPhaseThreeCompleted);
                //Typically we would call GroundControl#onOneTimeCompletion here, but we want to have a result for phase 4.
                startPhaseFour();
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {
                assertTrue("Progress went backward", progress > PHASE_TWO_PROGRESS_LIMIT);
            }
        });
    }

    /**
     * Simulate that the phase three listener went out of scope before completion was delivered.
     * so that this new UI reattaches after agent completion and still gets the final result from cache.
     */
    private void startPhaseFour() {
        //Simulate UI reattachment for later onDestroy call using the object we have handy.
        GroundControl.reattachToOneTime(mPhaseFourCompleted, ONE_TIME_ID, new AgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                mPhaseFourCompleted.setValue(true);
                GroundControl.onOneTimeCompletion(ONE_TIME_ID);
                GroundControl.onDestroy(mPhaseFourCompleted);
                startPhaseFive();
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {
                assertTrue("Phase four should not result in progress", false);
            }
        });
    }

    /**
     * Simulate a failed reattach after onOneTimeCompletion has been called. A tether should not return, no work should occur.
     */
    private void startPhaseFive() {
        assertNull(GroundControl.reattachToOneTime(mPhaseFourCompleted, ONE_TIME_ID, new AgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                assertTrue("Phase five should not result in execution", false);
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {
                assertTrue("Phase five should not result in progress", false);
            }
        }));
    }

}
