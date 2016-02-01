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

import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;

public class OsThreadPriorityAgent extends AbstractAgent<Integer, Void> {

    private final String mId;
    private String mUniqueIdentifier;

    public OsThreadPriorityAgent(String id) {
        mId = id;
    }

    @Override
    public String getUniqueIdentifier() {
        if (mUniqueIdentifier == null) {
            mUniqueIdentifier = OsThreadPriorityAgent.class.getCanonicalName() + mId;
        }
        return mUniqueIdentifier;
    }

    @Override
    public void cancel() {

    }

    @Override
    public void onProgressUpdateRequested() {

    }

    @Override
    public void run() {
        getAgentListener().onCompletion(getUniqueIdentifier(), android.os.Process.getThreadPriority(android.os.Process.myTid()));
    }
}
