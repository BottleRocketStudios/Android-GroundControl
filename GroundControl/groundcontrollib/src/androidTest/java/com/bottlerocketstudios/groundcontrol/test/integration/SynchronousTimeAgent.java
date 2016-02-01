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

import com.bottlerocketstudios.groundcontrol.test.TestUtils;
import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;

public class SynchronousTimeAgent extends AbstractAgent<Long, Float> {

    private final String mUniqueIdentifier;
    private final long mExecutionTimeMs;
    private boolean mCanceled;
    private float mProgress = 0.0f;

    public SynchronousTimeAgent(String uniqueIdentifier, long executionTimeMs) {
        mUniqueIdentifier = uniqueIdentifier;
        mExecutionTimeMs = executionTimeMs;
    }

    @Override
    public String getUniqueIdentifier() {
        return mUniqueIdentifier;
    }

    @Override
    public void cancel() {
        mCanceled = true;
    }

    @Override
    public void onProgressUpdateRequested() {
        sendProgressUpdate();
    }

    private void sendProgressUpdate() {
        getAgentListener().onProgress(getUniqueIdentifier(), mProgress);
    }

    @Override
    public void run() {
        long startTime = SystemClock.uptimeMillis();
        boolean complete = false;
        while (!complete && !mCanceled) {
            TestUtils.safeSleep(10);
            long elapsedTime = SystemClock.uptimeMillis() - startTime;
            mProgress = (float) elapsedTime / (float) mExecutionTimeMs;
            sendProgressUpdate();
            complete = elapsedTime > mExecutionTimeMs;
        }

        if (!mCanceled) {
            getAgentListener().onCompletion(getUniqueIdentifier(), SystemClock.uptimeMillis());
        }
    }
}
