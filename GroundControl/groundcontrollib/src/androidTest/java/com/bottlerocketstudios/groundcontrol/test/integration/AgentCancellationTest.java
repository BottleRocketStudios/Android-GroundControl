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

import android.test.AndroidTestCase;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.test.TestUtils;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.bottlerocketstudios.groundcontrol.policy.AgentPolicy;
import com.bottlerocketstudios.groundcontrol.policy.StandardAgentPolicyBuilder;
import com.bottlerocketstudios.groundcontrol.tether.AgentTether;

import java.util.concurrent.TimeUnit;

public class AgentCancellationTest extends AndroidTestCase {

    public void testCancelRun() {
        final SynchronousAgent synchronousAgent = new SynchronousAgent("testCancelRun", TimeUnit.HOURS.toMillis(5));
        synchronousAgent.setCancelTimeoutMs(5000);
        synchronousAgent.setRunTimeoutMs(6000);
        synchronousAgent.setMaximumTimeoutMs(8000);
        AgentExecutor.getDefault().runAgent(synchronousAgent, new AgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                assertTrue("Completion did not indicate failure", result != null && result.contains("Failed") && result.endsWith(synchronousAgent.getUniqueIdentifier()));
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        TestUtils.safeSleep(5500);
        assertTrue("Agent was not cancelled in time.", synchronousAgent.isCanceled());
    }

    public void testInterruptRun() {
        final SynchronousAgent synchronousAgent = new SynchronousAgent("testInterruptRun", TimeUnit.HOURS.toMillis(5), true, false);
        synchronousAgent.setCancelTimeoutMs(5000);
        synchronousAgent.setRunTimeoutMs(6000);
        synchronousAgent.setMaximumTimeoutMs(8000);
        AgentExecutor.getDefault().runAgent(synchronousAgent, new AgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                assertTrue("Completion did not indicate failure", result != null && result.contains("Failed") && result.endsWith(synchronousAgent.getUniqueIdentifier()));
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        TestUtils.safeSleep(6500);
        assertTrue("Agent was not interrupted in time.", synchronousAgent.wasInterrupted());
        assertFalse("Agent request was not cleaned up.", AgentExecutor.getDefault().hasStartedAgent(synchronousAgent.getUniqueIdentifier()));
    }

    public void testUnresponsiveInterrupt() {
        final SynchronousAgent synchronousAgent = new SynchronousAgent("testUnresponsiveInterrupt", TimeUnit.HOURS.toMillis(5), true, true);
        synchronousAgent.setCancelTimeoutMs(5000);
        synchronousAgent.setRunTimeoutMs(6000);
        synchronousAgent.setMaximumTimeoutMs(8000);
        AgentTether tether = AgentExecutor.getDefault().runAgent(synchronousAgent, new AgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                assertTrue("Completion did not indicate failure", result != null && result.contains("Failed") && result.endsWith(synchronousAgent.getUniqueIdentifier()));
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        TestUtils.safeSleep(6500);
        assertTrue("Agent was not interrupted in time.", synchronousAgent.wasInterrupted());
        //Check that abandoned request exists.
        assertTrue("Agent request was cleaned up early.", AgentExecutor.getDefault().hasStartedAgent(synchronousAgent.getUniqueIdentifier()));
        TestUtils.safeSleep(3000);
        //Check that abandoned request is removed.
        assertFalse("Agent request was not cleaned up.", AgentExecutor.getDefault().hasStartedAgent(synchronousAgent.getUniqueIdentifier()));
    }

    public void testTetherCancellation() {
        final SynchronousAgent synchronousAgent = new SynchronousAgent("testTetherCancellation", TimeUnit.SECONDS.toMillis(60));
        AgentTether tether1 = AgentExecutor.getDefault().runAgent(synchronousAgent, new AgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                assertTrue("Completion did not indicate failure", result != null && result.contains("Failed") && result.endsWith(synchronousAgent.getUniqueIdentifier()));
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        AgentTether tether2 = AgentExecutor.getDefault().runAgent(synchronousAgent, new AgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                assertTrue("Completion did not indicate failure", result != null && result.contains("Failed") && result.endsWith(synchronousAgent.getUniqueIdentifier()));
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        TestUtils.safeSleep(1000);
        tether1.cancel();

        TestUtils.safeSleep(1000);
        assertFalse("Agent was cancelled with uncancelled tether.", synchronousAgent.isCanceled());

        tether2.cancel();
        TestUtils.safeSleep(1000);
        assertTrue("Agent was not cancelled in time.", synchronousAgent.isCanceled());
    }

    public void testPolicyTimeout() {
        final SynchronousAgent synchronousAgent = new SynchronousAgent("testPolicyTimeout", TimeUnit.SECONDS.toMillis(5));
        AgentPolicy policy = (new StandardAgentPolicyBuilder()).setPolicyTimeoutMs(1000).build();

        final TestUtils.Container<Boolean> wasCompleted = new TestUtils.Container<>(false);

        AgentExecutor.getDefault().runAgent(synchronousAgent, policy, new AgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                assertNull("Completion was not null", result);
                wasCompleted.setValue(true);
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        //Allow test completion.
        TestUtils.safeSleep(1500);
        assertTrue("Completion did not fire.", wasCompleted.getValue());
        //Wait to be sure that second delivery of successful result does not happen after 5 seconds which will cause assertion failure in completion block above.
        TestUtils.safeSleep(4000);
    }

    public void testTetherRelease() {
        final SynchronousAgent synchronousAgent = new SynchronousAgent("testTetherRelease", TimeUnit.SECONDS.toMillis(5));
        AgentTether tether = AgentExecutor.getDefault().runAgent(synchronousAgent, new AgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                assertTrue("This listener should not have been notified, it's tether was released.", false);
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        TestUtils.safeSleep(1000);
        tether.release();

        final TestUtils.Container<Boolean> wasCompleted = new TestUtils.Container<>(false);

        AgentExecutor.getDefault().runAgent(synchronousAgent, new AgentListener<String, Float>() {
            @Override
            public void onCompletion(String agentIdentifier, String result) {
                wasCompleted.setValue(true);
            }

            @Override
            public void onProgress(String agentIdentifier, Float progress) {}
        });

        TestUtils.safeSleep(5000);
        assertTrue("Completion for unreleased tether was not called.", wasCompleted.getValue());
    }

    public void testWrappedCancellation() {
        final TestUtils.Container<Boolean> wasCanceled = new TestUtils.Container<>(false);
        AgentExecutor.getDefault().runAgent(new CancellationWrapperAgent(), new AgentListener<Boolean, Void>() {
            @Override
            public void onCompletion(String agentIdentifier, Boolean result) {
                wasCanceled.setValue(result);
            }

            @Override
            public void onProgress(String agentIdentifier, Void progress) {}
        });

        TestUtils.safeSleep(CancellationAgent.COMPLETION_WAIT_TIME_MS);
        assertTrue("Was not cancelled", wasCanceled.getValue());
    }

}
