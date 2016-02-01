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

package com.bottlerocketstudios.groundcontrol.test.looper;

import android.os.Handler;
import android.os.SystemClock;

import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;
import com.bottlerocketstudios.groundcontrol.looper.LooperController;


public class LooperTestAgent extends AbstractAgent<LooperTestResult, Void> {

    private final String mLooperId;
    private final int mTestId;
    private final LooperTestAction mLooperTestAction;
    private final long mCreationTime;
    private final int mTestIteration;
    private String mUniqueIdentifier;



    public LooperTestAgent(int testId, int testIteration, String looperId, LooperTestAction looperTestAction) {
        mTestId = testId;
        mTestIteration = testIteration;
        mLooperId = looperId;
        mLooperTestAction = looperTestAction;
        mCreationTime = SystemClock.uptimeMillis();
    }

    @Override
    public String getUniqueIdentifier() {
        if (mUniqueIdentifier == null) {
            mUniqueIdentifier = LooperTestAgent.class.getCanonicalName() + String.valueOf(mTestId) + mLooperId;
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
        switch (mLooperTestAction) {
            case TEST_EXECUTION:
                execute();
                break;
            case TEST_STOP:
                stop();
                break;
        }
    }

    private void execute() {
        Handler handler = new Handler(LooperController.getLooper(mLooperId));
        handler.post(new Runnable() {
            @Override
            public void run() {
                getAgentListener().onCompletion(getUniqueIdentifier(), new LooperTestResult(mTestId, mTestIteration, mLooperId, android.os.Process.myTid(), getLifetimeDuration(), mLooperTestAction));
            }
        });
    }

    private void stop() {
        LooperController.stopLooper(mLooperId);
        getAgentListener().onCompletion(getUniqueIdentifier(), new LooperTestResult(mTestId, mTestIteration, mLooperId, 0, getLifetimeDuration(), mLooperTestAction));
    }

    private long getLifetimeDuration() {
        return SystemClock.uptimeMillis() - mCreationTime;
    }
}
