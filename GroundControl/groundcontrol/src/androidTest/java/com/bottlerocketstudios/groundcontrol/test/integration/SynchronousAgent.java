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
import android.util.Log;

import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;

public class SynchronousAgent extends AbstractAgent<String, Float> {

    private static final String TAG = SynchronousAgent.class.getSimpleName();

    private final String mUniqueIdentifier;
    private final long mExecutionTimeMs;
    private final boolean mIgnoreCancel;
    private final boolean mSkipNotifyOnFailure;
    private boolean mCanceled;
    private float mProgress = 0.0f;
    private boolean mInterrupted;

    public SynchronousAgent(String uniqueIdentifier, long executionTimeMs) {
        this(uniqueIdentifier, executionTimeMs, false, false);
    }

    public SynchronousAgent(String uniqueIdentifier, long executionTimeMs, boolean ignoreCancel, boolean skipNotifyOnFailure) {
        mUniqueIdentifier = uniqueIdentifier;
        mExecutionTimeMs = executionTimeMs;
        mIgnoreCancel = ignoreCancel;
        mSkipNotifyOnFailure = skipNotifyOnFailure;
    }

    @Override
    public String getUniqueIdentifier() {
        return mUniqueIdentifier;
    }

    @Override
    public void cancel() {
        mCanceled = true;
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    public boolean wasInterrupted() {
        return mInterrupted;
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
        mInterrupted = false;
        try {
            while (!complete && (!isCanceled() || mIgnoreCancel)) {
                Thread.sleep(50);
                long elapsedTime = SystemClock.uptimeMillis() - startTime;
                mProgress = (float) elapsedTime / (float) mExecutionTimeMs;
                sendProgressUpdate();
                complete = elapsedTime > mExecutionTimeMs;
            }
        } catch (InterruptedException e) {
            Log.i(TAG, "Interrupted");
            mInterrupted = true;
        }

        if (!isCanceled() && !mInterrupted) {
            getAgentListener().onCompletion(getUniqueIdentifier(), "Finished SynchronousAgent ID: " + getUniqueIdentifier());
        } else if (!mSkipNotifyOnFailure) {
            getAgentListener().onCompletion(getUniqueIdentifier(), "Failed SynchronousAgent ID: " + getUniqueIdentifier());
        }
    }

}
