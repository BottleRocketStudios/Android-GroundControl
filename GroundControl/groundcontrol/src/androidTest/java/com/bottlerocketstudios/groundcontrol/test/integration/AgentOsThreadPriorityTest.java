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

import android.os.Process;
import android.test.AndroidTestCase;

import com.bottlerocketstudios.groundcontrol.AgentExecutor;
import com.bottlerocketstudios.groundcontrol.test.TestUtils;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;

public class AgentOsThreadPriorityTest extends AndroidTestCase {

    public void testOsThreadPriority() {
        final TestUtils.Container<Integer> mPriorityContainer = new TestUtils.Container<>(-1000);
        AgentExecutor.getDefault().runAgent(new OsThreadPriorityAgent(AgentOsThreadPriorityTest.class.getCanonicalName()), new AgentListener<Integer, Void>() {
            @Override
            public void onCompletion(String agentIdentifier, Integer result) {
                mPriorityContainer.setValue(result);
            }

            @Override
            public void onProgress(String agentIdentifier, Void progress) {

            }
        });
        TestUtils.safeSleep(500);
        assertEquals("OS Thread priority was unexpected", Process.THREAD_PRIORITY_BACKGROUND, (int) mPriorityContainer.getValue());
    }

}
